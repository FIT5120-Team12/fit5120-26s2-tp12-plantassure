from pathlib import Path
from collections import Counter
from PIL import Image

SOURCE = Path("raw_dataset")
EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp"}

valid_counts = Counter()
invalid_files = []

for folder in sorted(SOURCE.iterdir()):
    if not folder.is_dir():
        continue

    for path in folder.rglob("*"):
        if not path.is_file() or path.suffix.lower() not in EXTENSIONS:
            continue

        try:
            with Image.open(path) as image:
                image.verify()
            valid_counts[folder.name] += 1
        except Exception:
            invalid_files.append(path)

print("有效圖片總數：", sum(valid_counts.values()))
print("無效圖片總數：", len(invalid_files))
print("有有效圖片的物種：", len(valid_counts))
print("至少10張有效圖片：", sum(v >= 10 for v in valid_counts.values()))
print("至少20張有效圖片：", sum(v >= 20 for v in valid_counts.values()))
print("有效圖片最少：", min(valid_counts.values(), default=0))
print("有效圖片最多：", max(valid_counts.values(), default=0))

print("\n前20個無效檔案：")
for path in invalid_files[:20]:
    print(path)