from pathlib import Path
import random
import shutil

SOURCE = Path("raw_dataset")
OUTPUT = Path("dataset_large")

TRAIN_RATIO = 0.70
VAL_RATIO = 0.15
MIN_IMAGES = 10
SEED = 42

IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp"}

random.seed(SEED)

if not SOURCE.exists():
    raise FileNotFoundError("找不到 raw_dataset 資料夾")

# 避免重複執行時混入舊檔案
if OUTPUT.exists() and any(OUTPUT.iterdir()):
    raise RuntimeError(
        "dataset_large 已經存在而且不是空的，請先確認內容，避免重複分割。"
    )

for split in ["train", "val", "test"]:
    (OUTPUT / split).mkdir(parents=True, exist_ok=True)

included_classes = 0
skipped_classes = 0
total_train = 0
total_val = 0
total_test = 0

species_folders = sorted(
    folder for folder in SOURCE.iterdir() if folder.is_dir()
)

for species_folder in species_folders:
    images = [
        path for path in species_folder.rglob("*")
        if path.is_file() and path.suffix.lower() in IMAGE_EXTENSIONS
    ]

    if len(images) < MIN_IMAGES:
        print(
            f"略過：{species_folder.name}，只有 {len(images)} 張圖片"
        )
        skipped_classes += 1
        continue

    random.shuffle(images)

    total = len(images)

    val_count = max(1, round(total * VAL_RATIO))
    test_count = max(1, round(total * (1 - TRAIN_RATIO - VAL_RATIO)))
    train_count = total - val_count - test_count

    train_images = images[:train_count]
    val_images = images[train_count:train_count + val_count]
    test_images = images[train_count + val_count:]

    split_images = {
        "train": train_images,
        "val": val_images,
        "test": test_images,
    }

    for split_name, image_list in split_images.items():
        destination = OUTPUT / split_name / species_folder.name
        destination.mkdir(parents=True, exist_ok=True)

        for index, source_image in enumerate(image_list):
            new_filename = (
                f"{species_folder.name}_{index:05d}"
                f"{source_image.suffix.lower()}"
            )
            destination_path = destination / new_filename
            destination_path.hardlink_to(source_image.resolve())

    included_classes += 1
    total_train += len(train_images)
    total_val += len(val_images)
    total_test += len(test_images)

    print(
        f"{species_folder.name}: "
        f"train={len(train_images)}, "
        f"val={len(val_images)}, "
        f"test={len(test_images)}"
    )

print("\n分割完成")
print(f"使用物種數：{included_classes}")
print(f"略過物種數：{skipped_classes}")
print(f"Train 圖片：{total_train}")
print(f"Validation 圖片：{total_val}")
print(f"Test 圖片：{total_test}")
print(f"輸出位置：{OUTPUT.resolve()}")