import os, json, time
from pathlib import Path

import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from torchvision import datasets, transforms
import timm

def main():
    data_dir = Path(os.getenv("DATA_DIR", "../data"))
    out_dir = Path(os.getenv("OUT_DIR", "../artifacts"))
    out_dir.mkdir(parents=True, exist_ok=True)

    model_name = os.getenv("MODEL_NAME", "tf_efficientnet_b0")
    img_size = int(os.getenv("IMG_SIZE", "224"))
    batch_size = int(os.getenv("BATCH_SIZE", "32"))
    epochs = int(os.getenv("EPOCHS", "10"))
    lr = float(os.getenv("LR", "3e-4"))
    num_workers = int(os.getenv("NUM_WORKERS", "2"))

    device = "cuda" if torch.cuda.is_available() else "cpu"
    print("device:", device)

    train_tf = transforms.Compose([
        transforms.Resize((img_size, img_size)),
        transforms.RandomHorizontalFlip(),
        transforms.RandomRotation(10),
        transforms.ToTensor(),
        transforms.Normalize((0.485,0.456,0.406), (0.229,0.224,0.225)),
    ])
    val_tf = transforms.Compose([
        transforms.Resize((img_size, img_size)),
        transforms.ToTensor(),
        transforms.Normalize((0.485,0.456,0.406), (0.229,0.224,0.225)),
    ])

    train_ds = datasets.ImageFolder(data_dir / "train", transform=train_tf)
    val_ds = datasets.ImageFolder(data_dir / "val", transform=val_tf)

    idx_to_class = {v: k for k, v in train_ds.class_to_idx.items()}
    (out_dir / "labels.json").write_text(
        json.dumps({str(k): v for k, v in idx_to_class.items()}, ensure_ascii=False, indent=2),
        encoding="utf-8"
    )
    num_classes = len(idx_to_class)

    train_loader = DataLoader(train_ds, batch_size=batch_size, shuffle=True, num_workers=num_workers)
    val_loader = DataLoader(val_ds, batch_size=batch_size, shuffle=False, num_workers=num_workers)

    model = timm.create_model(model_name, pretrained=True, num_classes=num_classes).to(device)
    crit = nn.CrossEntropyLoss()
    opt = torch.optim.AdamW(model.parameters(), lr=lr)

    best_acc = 0.0
    best_path = out_dir / "model_best.pth"

    for epoch in range(1, epochs + 1):
        model.train()
        t0 = time.time()
        tr_loss, tr_ok, tr_n = 0.0, 0, 0

        for x, y in train_loader:
            x, y = x.to(device), y.to(device)
            opt.zero_grad()
            logits = model(x)
            loss = crit(logits, y)
            loss.backward()
            opt.step()

            tr_loss += loss.item() * x.size(0)
            tr_ok += (logits.argmax(1) == y).sum().item()
            tr_n += x.size(0)

        tr_loss /= max(1, tr_n)
        tr_acc = tr_ok / max(1, tr_n)

        model.eval()
        va_ok, va_n = 0, 0
        with torch.no_grad():
            for x, y in val_loader:
                x, y = x.to(device), y.to(device)
                logits = model(x)
                va_ok += (logits.argmax(1) == y).sum().item()
                va_n += x.size(0)

        va_acc = va_ok / max(1, va_n)
        print(f"[{epoch}/{epochs}] train_loss={tr_loss:.4f} train_acc={tr_acc:.4f} val_acc={va_acc:.4f} time={time.time()-t0:.1f}s")

        if va_acc > best_acc:
            best_acc = va_acc
            torch.save(model.state_dict(), best_path)
            print("✅ saved:", best_path)

    print("best_acc:", best_acc)

if __name__ == "__main__":
    main()