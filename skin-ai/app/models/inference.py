# skin-ai/app/models/inference.py
import os, json
from pathlib import Path
from typing import Optional, Dict, List
from io import BytesIO

import numpy as np
from PIL import Image

import torch
import torch.nn.functional as F
from torchvision import transforms

MODEL_NAME = "skin-clf"
MODEL_VERSION = os.getenv("MODEL_VERSION", "0.1.0")
IMG_SIZE = int(os.getenv("IMG_SIZE", "224"))

_device = "cuda" if torch.cuda.is_available() else "cpu"
_model = None
_labels: Dict[str, str] = {}
_preprocess = None

def _lazy_load():
    global _model, _labels, _preprocess
    if _model is not None and _preprocess is not None and _labels:
        return

    # 실행 위치가 달라도 항상 skin-ai 기준으로 artifacts를 찾도록 고정
    BASE_DIR = Path(__file__).resolve().parents[2]  # app/models/inference.py -> skin-ai/
    default_model_path = BASE_DIR / "artifacts" / "model.pt"
    default_labels_path = BASE_DIR / "artifacts" / "labels.json"

    model_path = Path(os.getenv("MODEL_PATH", str(default_model_path)))
    labels_path = Path(os.getenv("LABELS_PATH", str(default_labels_path)))

    if not model_path.exists():
        raise RuntimeError(f"MODEL_PATH not found: {model_path}")
    if not labels_path.exists():
        raise RuntimeError(f"LABELS_PATH not found: {labels_path}")

    _labels = json.loads(labels_path.read_text(encoding="utf-8"))  # {"0":"akiec", ...}

    _model = torch.jit.load(str(model_path), map_location=_device)
    _model.eval()

    # 학습 때와 동일한 전처리(중요!)
    _preprocess = transforms.Compose([
        transforms.Resize((IMG_SIZE, IMG_SIZE)),
        transforms.ToTensor(),
        transforms.Normalize((0.485,0.456,0.406), (0.229,0.224,0.225)),
    ])

def predict(
    image_bytes: bytes,
    top_k: int = 3,
    animal_type: Optional[str] = None,
    body_part: Optional[str] = None,
) -> Dict:
    _lazy_load()

    img = Image.open(BytesIO(image_bytes)).convert("RGB")
    x = _preprocess(img).unsqueeze(0).to(_device)

    with torch.no_grad():
        logits = _model(x)
        probs = F.softmax(logits, dim=1).squeeze(0).cpu().numpy()

    top_k = max(1, min(int(top_k), probs.shape[0]))
    idxs = np.argsort(-probs)[:top_k]

    preds: List[Dict] = []
    for i in idxs:
        label = _labels.get(str(int(i)), str(int(i)))
        preds.append({"label": label, "score": float(probs[i])})

    return {
        "model": {"name": MODEL_NAME, "version": MODEL_VERSION},
        "top_label": preds[0]["label"],
        "predictions": preds,
    }