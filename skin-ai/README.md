# 🐶 Skin AI Server (FastAPI)

반려동물 피부 질환 이미지 분류를 위한 AI 추론 서버입니다.
Spring Boot 기반 DERMAVERA 백엔드와 분리된 Python FastAPI 마이크로서비스입니다.

## 모델 파일 (Git LFS)

이 저장소는 `skin-ai/artifacts/model.pt`를 Git LFS로 관리합니다.

- **처음 clone 한 경우**: `git lfs pull` 실행 후 `skin-ai/artifacts/model.pt`가 내려옵니다.
- LFS 미설치 시: [Git LFS](https://git-lfs.github.com) 설치 후 `git lfs install` 한 번 실행.

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

## 🛠 트러블슈팅

### Spring Boot ↔ FastAPI 이미지 전송 시 Content-Type 누락 문제

반려동물 피부 진단 기능 구현 과정에서 Spring Boot 서버와 FastAPI AI 서버를 연동하였으나,
이미지 전송 후 AI 서버가 정상적으로 응답하지 않는 문제가 발생하였습니다.

**원인**

multipart 요청으로 전달되는 이미지의 Content-Type 정보가 올바르게 전달되지 않아
FastAPI 서버에서 요청을 처리하지 못하고 있었습니다.

구체적으로는 `MultipartFile.getContentType()`이 `null`이거나 비어 있을 경우,
PNG 파일임에도 `image/jpeg`로 잘못 지정되어 전송되었고,
FastAPI 서버에서 **422 Unprocessable Entity** 오류가 반환되었습니다.

**해결**

파일 확장자를 기반으로 Content-Type을 보완하는 로직을 추가하여 해결하였습니다.
`getContentType()`이 유효하지 않은 경우 확장자(`.png`, `.jpg` 등)를 확인하여
올바른 MIME 타입을 명시적으로 지정한 뒤 전송하도록 수정하였습니다.

**교훈**

서로 다른 서버 간 통신에서는 API 호출 자체보다
데이터 형식과 통신 규약을 정확하게 맞추는 것이 중요하다는 점을 배울 수 있었습니다.

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