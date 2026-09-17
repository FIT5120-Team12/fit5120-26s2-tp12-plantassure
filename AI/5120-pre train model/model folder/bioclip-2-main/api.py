import io
from pathlib import Path

import open_clip
import torch
import torch.nn as nn
from fastapi import FastAPI, File, HTTPException, UploadFile
from PIL import Image, UnidentifiedImageError


# =========================================================
# Settings
# =========================================================

CHECKPOINT_PATH = Path("models/bioclip_classifier.pth")
DEFAULT_MODEL_NAME = "hf-hub:imageomics/bioclip-2"
MAX_IMAGE_SIZE = 10 * 1024 * 1024


# =========================================================
# FastAPI
# =========================================================

app = FastAPI(
    title="Plant BioCLIP API",
    description="BioCLIP 2 plant classification API",
    version="1.0.0"
)


# =========================================================
# Device
# =========================================================

if torch.cuda.is_available():
    device = torch.device("cuda")
elif torch.backends.mps.is_available():
    device = torch.device("mps")
else:
    device = torch.device("cpu")

print("Device:", device)


# =========================================================
# Load checkpoint
# =========================================================

if not CHECKPOINT_PATH.exists():
    raise RuntimeError(
        f"找不到模型：{CHECKPOINT_PATH.resolve()}"
    )

print("Loading classifier checkpoint...")

checkpoint = torch.load(
    CHECKPOINT_PATH,
    map_location="cpu",
    weights_only=False
)

classes = checkpoint["classes"]
model_name = checkpoint.get(
    "model_name",
    DEFAULT_MODEL_NAME
)

print("Number of classes:", len(classes))


# =========================================================
# Load BioCLIP 2
# =========================================================

print("Loading BioCLIP 2...")

model, _, preprocess = open_clip.create_model_and_transforms(
    model_name,
    device=device
)

for parameter in model.parameters():
    parameter.requires_grad = False

model.eval()


# =========================================================
# Load classification head
# =========================================================

classifier = nn.Linear(
    model.visual.output_dim,
    len(classes)
).to(device)

classifier.load_state_dict(
    checkpoint["classifier"]
)

classifier.eval()

print("BioCLIP classifier loaded successfully.")
print("API ready.")


# =========================================================
# Prediction function
# =========================================================

def predict_image(image: Image.Image):
    image = image.convert("RGB")

    image_tensor = (
        preprocess(image)
        .unsqueeze(0)
        .to(device)
    )

    with torch.inference_mode():
        # 必須和訓練、evaluate 時保持相同
        features = model.encode_image(image_tensor)

        if isinstance(features, tuple):
            features = features[0]

        logits = classifier(features)
        probabilities = torch.softmax(logits, dim=1)[0]

        top_count = min(3, len(classes))

        scores, indices = torch.topk(
            probabilities,
            k=top_count
        )

    predictions = []

    for score, index in zip(scores, indices):
        probability = score.item()
        class_name = classes[index.item()]

        predictions.append({
            "class": class_name,
            "score": round(probability, 6),
            "percentage": round(probability * 100, 2)
        })

    best = predictions[0]

    return {
        "prediction": best["class"],
        "confidence": best["score"],
        "confidence_percentage": best["percentage"],
        "top_3": predictions
    }


# =========================================================
# GET /health
# =========================================================

@app.get("/health")
def health():
    return {
        "status": "ok",
        "model": "BioCLIP 2 with trained classifier",
        "device": str(device),
        "number_of_classes": len(classes)
    }


# =========================================================
# GET /classes
# =========================================================

@app.get("/classes")
def get_classes():
    return {
        "count": len(classes),
        "classes": classes
    }


# =========================================================
# POST /predict
# =========================================================

@app.post("/predict")
async def predict(
    file: UploadFile = File(...)
):
    allowed_types = {
        "image/jpeg",
        "image/png",
        "image/webp"
    }

    if (
        file.content_type
        and file.content_type not in allowed_types
    ):
        raise HTTPException(
            status_code=400,
            detail="Only JPG, PNG and WEBP images are supported."
        )

    try:
        contents = await file.read()

        if not contents:
            raise HTTPException(
                status_code=400,
                detail="No image was uploaded."
            )

        if len(contents) > MAX_IMAGE_SIZE:
            raise HTTPException(
                status_code=413,
                detail="Image cannot exceed 10MB."
            )

        image = Image.open(
            io.BytesIO(contents)
        )

        result = predict_image(image)

        return {
            "success": True,
            "filename": file.filename,
            **result
        }

    except HTTPException:
        raise

    except UnidentifiedImageError:
        raise HTTPException(
            status_code=400,
            detail="The uploaded file is not a valid image."
        )

    except Exception as error:
        print("Prediction error:", error)

        raise HTTPException(
            status_code=500,
            detail=str(error)
        )