from pathlib import Path
import shutil

from PIL import Image


DATASET_ROOT = Path("dataset_large")
QUARANTINE_ROOT = Path("invalid_images")

IMAGE_EXTENSIONS = {
    ".jpg",
    ".jpeg",
    ".png",
    ".webp",
    ".bmp"
}

invalid_images = []
checked_images = 0

for image_path in DATASET_ROOT.rglob("*"):
    if not image_path.is_file():
        continue

    if image_path.suffix.lower() not in IMAGE_EXTENSIONS:
        continue

    checked_images += 1

    try:
        # 第一次確認檔案格式
        with Image.open(image_path) as image:
            image.verify()

        # 第二次確認圖片像素可以完整讀取
        with Image.open(image_path) as image:
            image.convert("RGB").load()

    except Exception as error:
        relative_path = image_path.relative_to(DATASET_ROOT)
        destination = QUARANTINE_ROOT / relative_path

        destination.parent.mkdir(
            parents=True,
            exist_ok=True
        )

        shutil.move(
            str(image_path),
            str(destination)
        )

        invalid_images.append(image_path)

        print(f"無效圖片：{image_path}")
        print(f"原因：{error}")
        print(f"已移動到：{destination}\n")


# 移除已經變成空的物種資料夾
folders = sorted(
    [
        folder
        for folder in DATASET_ROOT.rglob("*")
        if folder.is_dir()
    ],
    key=lambda folder: len(folder.parts),
    reverse=True
)

for folder in folders:
    try:
        folder.rmdir()
        print(f"移除空資料夾：{folder}")
    except OSError:
        pass


print("\n檢查完成")
print(f"檢查圖片數量：{checked_images}")
print(f"無效圖片數量：{len(invalid_images)}")
print(f"無效圖片保存在：{QUARANTINE_ROOT.resolve()}")