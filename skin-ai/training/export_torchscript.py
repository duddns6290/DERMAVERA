import os, json
from pathlib import Path
import torch
import timm

def main():
    out_dir = Path(os.getenv("OUT_DIR", "../artifacts"))
    weights = out_dir / "model_best.pth"
    labels = out_dir / "labels.json"

    idx_to_class = json.loads(labels.read_text(encoding="utf-8"))
    num_classes = len(idx_to_class)

    model_name = os.getenv("MODEL_NAME", "tf_efficientnet_b0")
    img_size = int(os.getenv("IMG_SIZE", "224"))
    device = "cuda" if torch.cuda.is_available() else "cpu"

    model = timm.create_model(model_name, pretrained=False, num_classes=num_classes)
    model.load_state_dict(torch.load(weights, map_location="cpu"))
    model.eval().to(device)

    example = torch.randn(1, 3, img_size, img_size).to(device)
    traced = torch.jit.trace(model, example)
    out_path = out_dir / "model.pt"
    traced.save(str(out_path))
    print("✅ saved:", out_path)

if __name__ == "__main__":
    main()