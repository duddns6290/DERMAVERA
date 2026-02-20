# 🐶 Skin AI Server (FastAPI)

반려동물 피부 질환 이미지 분류를 위한 AI 추론 서버입니다.
Spring Boot 기반 DERMAVERA 백엔드와 분리된 Python FastAPI 마이크로서비스입니다.

## ⚠️ Python Version Requirement

본 프로젝트는 딥러닝 모델(PyTorch) 사용을 위해
**Python 3.11.x 버전을 권장합니다.**

Python 3.13은 일부 딥러닝 라이브러리와 호환 문제가 발생할 수 있습니다.

여러 Python 버전이 설치되어 있어도 문제 없습니다.
skin-ai 폴더에서는 반드시 아래와 같이 3.11로 가상환경을 생성하세요:

py -3.11 -m venv venv

## 📌 Tech Stack

- Python 3.11
- FastAPI
- Uvicorn
- PyTorch
- timm
- torchvision
- python-dotenv
- python-multipart

## 📁 Project Structure

skin-ai/
│
├── app/
│   ├── main.py
│   ├── api/
│   │   ├── health.py
│   │   └── diagnose.py
│   ├── core/
│   │   └── config.py
│   ├── models/
│   │   └── inference.py
│   └── schemas/
│       └── diagnose_schema.py
│
├── .env
├── requirements.txt
└── README.md

## ⚙️ Installation & Run

1️⃣ 가상환경 생성

cd skin-ai
py -3.11 -m venv venv

2️⃣ 가상환경 활성화

Windows PowerShell:
.\venv\Scripts\Activate.ps1

Windows CMD:
venv\Scripts\activate.bat

3️⃣ 패키지 설치

pip install -r requirements.txt

4️⃣ 서버 실행

uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

## 🚀 API Endpoints

### 🔹 Health Check

GET /health

Response:

{
  "status": "ok",
  "service": "skin-ai",
  "version": "0.1.0"
}

### 🔹 Image Diagnosis

POST /v1/diagnose

Content-Type: multipart/form-data

Request Fields:
- image (file) [required]
- animal_type (text) [optional]
- body_part (text) [optional]
- top_k (int) [optional]

Optional Header:
X-Internal-Token: dev-internal-token

Response Example:

{
  "request_id": "uuid",
  "model": {
    "name": "skin-clf",
    "version": "0.1.0"
  },
  "input": {
    "filename": "dog.jpg",
    "animal_type": "dog",
    "body_part": "ear"
  },
  "top_label": "atopic_dermatitis",
  "predictions": [
    { "label": "atopic_dermatitis", "score": 0.82 },
    { "label": "fungal", "score": 0.11 }
  ],
  "processing_ms": 132
}

## 🧠 Model

현재는 더미 랜덤 예측 로직이 적용되어 있습니다.

실제 모델 로직은 아래 파일에서 구현합니다:

app/models/inference.py

추후 확장 가능:
- PyTorch
- TensorFlow
- ONNX Runtime

## 🔐 Environment Variables (.env)

APP_NAME=skin-ai
APP_VERSION=0.1.0
INTERNAL_TOKEN=dev-internal-token
MAX_IMAGE_MB=10

## 🔗 Integration

Spring Boot 서버는 WebClient를 통해
/v1/diagnose 엔드포인트를 호출하여 추론 결과를 수신합니다.

## 📖 Swagger Docs

서버 실행 후 접속:

http://localhost:8000/docs

## 📌 Future Improvements

- 실제 딥러닝 모델 연동
- Grad-CAM 시각화
- 비동기 Job Queue 처리
- Docker 배포
- GPU 지원

## 👨‍💻 Author

DERMAVERA AI Module

Tested Environment:
- Windows 11
- Python 3.11.9
- PyTorch 2.2.x
- CUDA 12.x (optional)