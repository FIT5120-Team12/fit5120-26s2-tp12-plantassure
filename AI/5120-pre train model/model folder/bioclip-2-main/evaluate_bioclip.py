# import torch
# import torch.nn as nn
# from torchvision import datasets
# from torch.utils.data import DataLoader
# import open_clip

# DEVICE = "mps" if torch.backends.mps.is_available() else "cpu"
# MODEL_NAME = "hf-hub:imageomics/bioclip-2"

# checkpoint = torch.load(
#     "models/bioclip_classifier.pth",
#     map_location=DEVICE
# )

# classes = checkpoint["classes"]

# model, _, preprocess = open_clip.create_model_and_transforms(
#     MODEL_NAME,
#     device=DEVICE
# )

# for parameter in model.parameters():
#     parameter.requires_grad = False

# classifier = nn.Linear(
#     model.visual.output_dim,
#     len(classes)
# ).to(DEVICE)

# classifier.load_state_dict(checkpoint["classifier"])
# classifier.eval()
# model.eval()

# test_dataset = datasets.ImageFolder(
#     "dataset_large/test",
#     transform=preprocess
# )

# test_loader = DataLoader(
#     test_dataset,
#     batch_size=1,
#     shuffle=False,
#     num_workers=0
# )

# correct = 0
# total = 0

# with torch.no_grad():
#     for images, labels in test_loader:
#         images = images.to(DEVICE)
#         labels = labels.to(DEVICE)

#         features = model.encode_image(images)

#         if isinstance(features, tuple):
#             features = features[0]

#         outputs = classifier(features)
#         predictions = outputs.argmax(dim=1)

#         for i in range(len(images)):
#             actual = classes[labels[i].item()]
#             predicted = classes[predictions[i].item()]

#             print(f"實際: {actual} | 預測: {predicted}")

#         correct += (predictions == labels).sum().item()
#         total += labels.size(0)

# accuracy = correct / total if total > 0 else 0

# print(f"\nTest Accuracy: {accuracy * 100:.2f}%")




from pathlib import Path

import open_clip
import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from torchvision import datasets


# 1. 基本設定
if torch.backends.mps.is_available():
    DEVICE = "mps"
elif torch.cuda.is_available():
    DEVICE = "cuda"
else:
    DEVICE = "cpu"

MODEL_NAME = "hf-hub:imageomics/bioclip-2"
MODEL_PATH = Path("models/bioclip_classifier.pth")
TEST_PATH = Path("dataset_large/test")

print(f"使用裝置：{DEVICE}")


# 2. 檢查路徑
if not MODEL_PATH.exists():
    raise FileNotFoundError(
        f"找不到模型：{MODEL_PATH.resolve()}"
    )

if not TEST_PATH.exists():
    raise FileNotFoundError(
        f"找不到測試資料：{TEST_PATH.resolve()}"
    )


# 3. 載入訓練完成的分類器
checkpoint = torch.load(
    MODEL_PATH,
    map_location=DEVICE,
    weights_only=False
)

classes = checkpoint["classes"]

print(f"模型分類數量：{len(classes)}")


# 4. 載入 BioCLIP 2
model, _, preprocess = open_clip.create_model_and_transforms(
    MODEL_NAME,
    device=DEVICE
)

for parameter in model.parameters():
    parameter.requires_grad = False

model.eval()


# 5. 建立分類器
classifier = nn.Linear(
    model.visual.output_dim,
    len(classes)
).to(DEVICE)

classifier.load_state_dict(checkpoint["classifier"])
classifier.eval()


# 6. 載入測試資料
test_dataset = datasets.ImageFolder(
    TEST_PATH,
    transform=preprocess
)

test_loader = DataLoader(
    test_dataset,
    batch_size=16,
    shuffle=False,
    num_workers=0
)

print(f"測試圖片數量：{len(test_dataset)}")
print(f"測試資料分類數量：{len(test_dataset.classes)}")


# 7. 確認測試集與模型分類一致
checkpoint_class_to_index = {
    class_name: index
    for index, class_name in enumerate(classes)
}

missing_classes = [
    class_name
    for class_name in test_dataset.classes
    if class_name not in checkpoint_class_to_index
]

if missing_classes:
    raise ValueError(
        "測試資料包含模型沒有的分類："
        + ", ".join(missing_classes[:10])
    )


# 8. 開始測試
correct = 0
total = 0
displayed = 0

with torch.no_grad():
    for images, dataset_labels in test_loader:
        images = images.to(DEVICE)

        # 將 ImageFolder 標籤轉成 checkpoint 的分類編號
        labels = torch.tensor(
            [
                checkpoint_class_to_index[
                    test_dataset.classes[label]
                ]
                for label in dataset_labels.tolist()
            ],
            dtype=torch.long,
            device=DEVICE
        )

        features = model.encode_image(images)

        if isinstance(features, tuple):
            features = features[0]

        outputs = classifier(features)
        predictions = outputs.argmax(dim=1)

        # 只顯示前 30 張，避免 Terminal 輸出太多
        for index in range(len(images)):
            if displayed < 30:
                actual = classes[labels[index].item()]
                predicted = classes[predictions[index].item()]

                print(
                    f"實際：{actual} | 預測：{predicted}"
                )
                displayed += 1

        correct += (predictions == labels).sum().item()
        total += labels.size(0)


# 9. 顯示結果
accuracy = correct / total if total > 0 else 0

print("\n測試完成")
print(f"正確數量：{correct}")
print(f"測試總數：{total}")
print(f"Test Accuracy：{accuracy * 100:.2f}%")