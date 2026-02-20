# skin-ai/app/main.py
from fastapi import FastAPI
from app.api.diagnose import router as diagnose_router
from app.core.config import settings

app = FastAPI(title=settings.app_name, version=settings.app_version)

@app.get("/health")
def health():
    return {"status": "ok"}

app.include_router(diagnose_router)