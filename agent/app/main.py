from fastapi import FastAPI

app = FastAPI(title="Web Hotel MIS Agent", version="0.1.0")


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok", "service": "hotel-mis-agent"}
