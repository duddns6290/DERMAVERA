# skin-ai/app/api/diagnose.py
from fastapi import APIRouter, File, UploadFile, Form, Header, HTTPException
from typing import Optional
import time
import uuid

from app.core.config import settings
from app.models.inference import predict
from app.schemas.diagnose_schema import DiagnoseResponse, ModelInfo, DiagnoseInput, Prediction

router = APIRouter(prefix="/v1", tags=["diagnosis"])

def _validate_token(x_internal_token: Optional[str]):
    # INTERNAL_TOKEN이 설정된 경우에만 검증
    if settings.internal_token and x_internal_token != settings.internal_token:
        raise HTTPException(status_code=401, detail="Invalid internal token")

def _validate_image_size(file_bytes: bytes):
    max_bytes = settings.max_image_mb * 1024 * 1024
    if len(file_bytes) > max_bytes:
        raise HTTPException(
            status_code=413,
            detail=f"Image too large. Max {settings.max_image_mb}MB allowed.",
        )

@router.post("/diagnose", response_model=DiagnoseResponse)
async def diagnose(
    image: UploadFile = File(...),
    animal_type: Optional[str] = Form(default=None),
    body_part: Optional[str] = Form(default=None),
    top_k: int = Form(default=3),
    x_internal_token: Optional[str] = Header(default=None),
):
    _validate_token(x_internal_token)

    start = time.time()
    image_bytes = await image.read()
    _validate_image_size(image_bytes)

    result = predict(
        image_bytes=image_bytes,
        top_k=top_k,
        animal_type=animal_type,
        body_part=body_part,
    )

    request_id = str(uuid.uuid4())
    processing_ms = int((time.time() - start) * 1000)

    return DiagnoseResponse(
        request_id=request_id,
        model=ModelInfo(**result["model"]),
        input=DiagnoseInput(
            filename=image.filename or "unknown",
            animal_type=animal_type,
            body_part=body_part,
        ),
        top_label=result["top_label"],
        predictions=[Prediction(**p) for p in result["predictions"]],
        processing_ms=processing_ms,
    )