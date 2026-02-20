# skin-ai/app/schemas/diagnose_schema.py
from pydantic import BaseModel
from typing import Optional, List

class ModelInfo(BaseModel):
    name: str
    version: str

class DiagnoseInput(BaseModel):
    filename: str
    animal_type: Optional[str] = None
    body_part: Optional[str] = None

class Prediction(BaseModel):
    label: str
    score: float

class DiagnoseResponse(BaseModel):
    request_id: str
    model: ModelInfo
    input: DiagnoseInput
    top_label: str
    predictions: List[Prediction]
    processing_ms: int