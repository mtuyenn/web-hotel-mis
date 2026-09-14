from fastapi import FastAPI

# Ứng dụng ASGI nhận các request của agent; metadata này xuất hiện trong tài liệu OpenAPI.
app = FastAPI(title="Web Hotel MIS Agent", version="0.1.0")


@app.get("/health")
# Endpoint kiểm tra liveness, không truy cập dữ liệu nghiệp vụ nên trả lời nhanh và ổn định.
def health() -> dict[str, str]:
    # Payload nhỏ giúp hệ thống giám sát xác nhận đúng service đang phục vụ.
    return {"status": "ok", "service": "hotel-mis-agent"}
