import torch
import open_clip
from PIL import Image

device = "mps" if torch.backends.mps.is_available() else "cpu"
model_name = "hf-hub:imageomics/bioclip-2"

model, _, preprocess = open_clip.create_model_and_transforms(
    model_name,
    device=device
)

tokenizer = open_clip.get_tokenizer(model_name)

image = preprocess(
    Image.open("test_images/plant.jpg").convert("RGB")
).unsqueeze(0).to(device)

labels = [
    "a photo of a eucalyptus",
    "a photo of a fern",
    "a photo of a grass",
    "a photo of a flower",
    "a photo of a non-plant"
]

text = tokenizer(labels).to(device)

with torch.no_grad():
    image_features = model.encode_image(image)
    text_features = model.encode_text(text)

    if isinstance(image_features, tuple):
        image_features = image_features[0]

    if isinstance(text_features, tuple):
        text_features = text_features[0]

    image_features /= image_features.norm(dim=-1, keepdim=True)
    text_features /= text_features.norm(dim=-1, keepdim=True)

    probabilities = (
        100 * image_features @ text_features.T
    ).softmax(dim=-1)[0]

for label, probability in zip(labels, probabilities):
    print(f"{label}: {probability.item() * 100:.2f}%")

best_index = probabilities.argmax().item()
print("\n預測結果:", labels[best_index])