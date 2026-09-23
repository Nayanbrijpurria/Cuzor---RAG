# cuzor-rag

This project was created using the [Ktor Project Generator](https://start.ktor.io).

## RAG pipeline is now doing the following successfully:

✅ User sends a query 

⬇️

✅ Ktor receives the request

⬇️

✅ FastAPI generates an embedding for the query

⬇️

✅ PostgreSQL returns all stored embeddings

⬇️

✅ Cosine similarity compares the query with every ticket

⬇️

✅ Tickets with low confidence and high confidence will be returned.


## Some Screenshots.

<img width="1905" height="1085" alt="AdobeExpressPhotos_1842941cdf56417f83c908ff56a9527b_CopyEdited" src="https://github.com/user-attachments/assets/3060e896-df2e-47c5-b7d5-c40e34db7030" />


<img width="1877" height="1100" alt="AdobeExpressPhotos_3433665b976e42c5a58d186a822dc745_CopyEdited" src="https://github.com/user-attachments/assets/37115390-f45c-4b70-8d20-82041df996c9" />


<img width="1858" height="1115" alt="AdobeExpressPhotos_056d8ddb96c04530bdf95e9044eefd94_CopyEdited" src="https://github.com/user-attachments/assets/0cbec6d6-77bb-422a-a929-9ee7a6bba368" />

<img width="1893" height="1095" alt="AdobeExpressPhotos_ed144a72f6ff473abe4650ad6a05173a_CopyEdited" src="https://github.com/user-attachments/assets/55e7428f-3f54-463b-b09c-51d79a7804f9" />

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
