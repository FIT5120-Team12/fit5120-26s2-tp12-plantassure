import os
from collections import Counter

import open_clip
import torch
import torch.nn as nn
from torch.utils.data import DataLoader, WeightedRandomSampler
from torchvision import datasets, transforms


# =========================================================
# 設定
# =========================================================

DEVICE = "mps" if torch.backends.mps.is_available() else "cpu"
MODEL_NAME = "hf-hub:imageomics/bioclip-2"

BATCH_SIZE = 8
EPOCHS = 20
LEARNING_RATE = 1e-3
EARLY_STOPPING_PATIENCE = 5

OUTPUT_PATH = "models/bioclip_classifier_611.pth"

print("使用裝置:", DEVICE)


# =========================================================
# 載入 BioCLIP
# =========================================================

model, _, preprocess = open_clip.create_model_and_transforms(
    MODEL_NAME,
    device=DEVICE
)

# 第一階段：凍結 BioCLIP，只訓練分類器
for parameter in model.parameters():
    parameter.requires_grad = False

model.eval()


# =========================================================
# 資料增強
# =========================================================

train_transform = transforms.Compose([
    transforms.RandomHorizontalFlip(p=0.5),
    transforms.RandomRotation(degrees=10),
    transforms.ColorJitter(
        brightness=0.15,
        contrast=0.15,
        saturation=0.15,
        hue=0.02
    ),
    preprocess
])


# =========================================================
# 載入資料集
# =========================================================

train_dataset = datasets.ImageFolder(
    "dataset_large/train",
    transform=train_transform
)

val_dataset = datasets.ImageFolder(
    "dataset_large/val",
    transform=preprocess
)

if train_dataset.classes != val_dataset.classes:
    raise RuntimeError("Train 和 Validation 的類別順序不一致")

num_classes = len(train_dataset.classes)

print("分類數量:", num_classes)
print("Train 圖片:", len(train_dataset))
print("Validation 圖片:", len(val_dataset))


# =========================================================
# 平衡類別
# =========================================================

class_counts = Counter(train_dataset.targets)

sample_weights = [
    1.0 / class_counts[label]
    for label in train_dataset.targets
]

sampler = WeightedRandomSampler(
    weights=sample_weights,
    num_samples=len(sample_weights),
    replacement=True
)

train_loader = DataLoader(
    train_dataset,
    batch_size=BATCH_SIZE,
    sampler=sampler,
    num_workers=0
)

val_loader = DataLoader(
    val_dataset,
    batch_size=BATCH_SIZE,
    shuffle=False,
    num_workers=0
)


# =========================================================
# 建立分類器
# =========================================================

classifier = nn.Linear(
    model.visual.output_dim,
    num_classes
).to(DEVICE)

loss_function = nn.CrossEntropyLoss(
    label_smoothing=0.1
)

optimizer = torch.optim.AdamW(
    classifier.parameters(),
    lr=LEARNING_RATE,
    weight_decay=1e-4
)

scheduler = torch.optim.lr_scheduler.CosineAnnealingLR(
    optimizer,
    T_max=EPOCHS
)


# =========================================================
# 訓練
# =========================================================

os.makedirs("models", exist_ok=True)

best_val_accuracy = 0.0
epochs_without_improvement = 0

for epoch in range(EPOCHS):

    # -------------------------
    # Train
    # -------------------------

    classifier.train()

    train_loss = 0.0
    train_correct = 0
    train_total = 0

    for images, labels in train_loader:
        images = images.to(DEVICE)
        labels = labels.to(DEVICE)

        with torch.no_grad():
            features = model.encode_image(images)

            if isinstance(features, tuple):
                features = features[0]

        outputs = classifier(features)
        loss = loss_function(outputs, labels)

        optimizer.zero_grad()
        loss.backward()
        optimizer.step()

        train_loss += loss.item() * labels.size(0)

        predictions = outputs.argmax(dim=1)
        train_correct += (predictions == labels).sum().item()
        train_total += labels.size(0)

    train_loss /= train_total
    train_accuracy = train_correct / train_total

    # -------------------------
    # Validation
    # -------------------------

    classifier.eval()

    val_loss = 0.0
    val_correct = 0
    val_total = 0

    with torch.no_grad():
        for images, labels in val_loader:
            images = images.to(DEVICE)
            labels = labels.to(DEVICE)

            features = model.encode_image(images)

            if isinstance(features, tuple):
                features = features[0]

            outputs = classifier(features)
            loss = loss_function(outputs, labels)

            val_loss += loss.item() * labels.size(0)

            predictions = outputs.argmax(dim=1)
            val_correct += (predictions == labels).sum().item()
            val_total += labels.size(0)

    val_loss /= val_total
    val_accuracy = val_correct / val_total

    current_lr = optimizer.param_groups[0]["lr"]

    print(
        f"Epoch {epoch + 1:02d}/{EPOCHS} | "
        f"LR: {current_lr:.6f} | "
        f"Train Loss: {train_loss:.4f} | "
        f"Train Acc: {train_accuracy * 100:.2f}% | "
        f"Val Loss: {val_loss:.4f} | "
        f"Val Acc: {val_accuracy * 100:.2f}%"
    )

    # -------------------------
    # 儲存最佳模型
    # -------------------------

    if val_accuracy > best_val_accuracy:
        best_val_accuracy = val_accuracy
        epochs_without_improvement = 0

        torch.save(
            {
                "classifier": classifier.state_dict(),
                "classes": train_dataset.classes,
                "model_name": MODEL_NAME,
                "best_val_accuracy": best_val_accuracy,
                "num_classes": num_classes
            },
            OUTPUT_PATH
        )

        print(
            f"已儲存最佳模型：{OUTPUT_PATH} "
            f"(Val Acc: {best_val_accuracy * 100:.2f}%)"
        )

    else:
        epochs_without_improvement += 1

    scheduler.step()

    # -------------------------
    # Early stopping
    # -------------------------

    if epochs_without_improvement >= EARLY_STOPPING_PATIENCE:
        print("Validation Accuracy 連續多次沒有改善，提前停止。")
        break


print("\n訓練完成")
print(f"最佳 Validation Accuracy: {best_val_accuracy * 100:.2f}%")
print(f"模型位置: {OUTPUT_PATH}")