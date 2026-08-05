# cuzor-rag

This project was created using the [Ktor Project Generator](https://start.ktor.io).

## RAG pipeline is now doing the following successfully:

✅ User sends a query in Postman

⬇️

✅ Ktor receives the request

⬇️

✅ FastAPI generates an embedding for the query

⬇️

✅ PostgreSQL returns all stored embeddings

⬇️

✅ Cosine similarity compares the query with every ticket

⬇️

✅ Top 5 most similar tickets are returned

## Design Decision: Embedding Service Architecture

Initially, I started implementing the embedding pipeline in Kotlin using ONNX Runtime because our backend is built with Ktor. While working on the implementation, I realized that BGE-M3 requires several NLP-specific steps such as Hugging Face tokenization, ONNX inference, mean pooling, and vector normalization. The Hugging Face ecosystem and ONNX tooling are much more mature in Python than on the JVM. Instead of spending time maintaining complex AI inference code in Kotlin, I decided to keep the business backend in Kotlin and move only the embedding generation into a dedicated Python microservice. This keeps the main application in Kotlin while using Python only where it has a clear technical advantage.

## Building & Running

To build or run the project, use one of the following tasks:

| Task                      | Description  |
|---------------------------|--------------|
| `./gradlew :server:build` | Build the project |
| `./gradlew :server:run`   | Run the server |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```
