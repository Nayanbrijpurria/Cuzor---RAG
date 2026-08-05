from fastapi import FastAPI

from app.model import embedding_model
from app.schemas import EmbeddingRequest, EmbeddingResponse


app = FastAPI(
    title="Cuzor Embedding Service",
    version="1.0.0"
)


@app.get("/health")
async def health() -> dict[str, str]:
    return {
        "status": "UP"
    }


@app.post(
    "/embed",
    response_model=EmbeddingResponse
)
async def embed(
    request: EmbeddingRequest
) -> EmbeddingResponse:

    embedding = embedding_model.generate_embedding(
        request.text
    )

    return EmbeddingResponse(
        embedding=embedding
    )