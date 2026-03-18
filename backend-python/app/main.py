import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.routes import router
from app.core.config import PORT, get_origins

app = FastAPI(title="Energia Analytics Python API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=get_origins(),
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(router)


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=PORT)
