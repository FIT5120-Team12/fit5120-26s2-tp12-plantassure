import torch
import open_clip

device = "mps" if torch.backends.mps.is_available() else "cpu"

model_name = "hf-hub:imageomics/bioclip-2"

print("使用裝置:", device)
print("開始載入 BioCLIP 2...")

model, _, preprocess = open_clip.create_model_and_transforms(
    model_name,
    device=device
)

tokenizer = open_clip.get_tokenizer(model_name)

print("BioCLIP 2 載入成功")