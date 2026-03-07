# skin-ai/app/core/config.py
from pydantic import BaseModel
from dotenv import load_dotenv
import os

# skin-ai/.env 로드 (현재 작업 디렉터리 기준)
load_dotenv()

class Settings(BaseModel):
    app_name: str = os.getenv("APP_NAME", "skin-ai")
    app_version: str = os.getenv("APP_VERSION", "0.1.0")

    # 개발 편의: 기본값을 None으로 두면 토큰 검증이 꺼짐
    # 운영에서 보호하고 싶으면 .env에 INTERNAL_TOKEN을 설정하면 됨
    internal_token: str | None = os.getenv("INTERNAL_TOKEN", None)

    # 업로드 크기 제한(직접 검증용)
    max_image_mb: int = int(os.getenv("MAX_IMAGE_MB", "10"))

settings = Settings()