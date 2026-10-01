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

## EMBEDDING STATUS.

PENDING  = ticket is waiting to be embedded (0)
EMBEDDED = embedding was successfully generated (1)
FAILED   = embedding failed twice (2)
SKIPPED  = ticket does not have messages/conversation (3)
           so embedding is not required


## Some Screenshots.



<img width="946" height="499" alt="Screenshot 2026-09-24 170817" src="https://github.com/user-attachments/assets/ef4c15a4-4c1a-46ed-ac65-fc403d6efd82" />


<img width="1863" height="1011" alt="AdobeExpressPhotos_95fb17b683674f39a30f7f33ff002a9b_CopyEdited" src="https://github.com/user-attachments/assets/0d61c29f-102b-47eb-9692-91162445f700" />


<img width="1858" height="1115" alt="AdobeExpressPhotos_056d8ddb96c04530bdf95e9044eefd94_CopyEdited" src="https://github.com/user-attachments/assets/0cbec6d6-77bb-422a-a929-9ee7a6bba368" />


<img width="1893" height="1095" alt="AdobeExpressPhotos_ed144a72f6ff473abe4650ad6a05173a_CopyEdited" src="https://github.com/user-attachments/assets/55e7428f-3f54-463b-b09c-51d79a7804f9" />

## Design Decision: Embedding Service Architecture

Initially, I started implementing the embedding pipeline in Kotlin using ONNX Runtime because our backend is built with Ktor. While working on the implementation, I realized that BGE-M3 requires several NLP-specific steps such as Hugging Face tokenization, ONNX inference, mean pooling, and vector normalization. The Hugging Face ecosystem and ONNX tooling are much more mature in Python than on the JVM. Instead of spending time maintaining complex AI inference code in Kotlin, I decided to keep the business backend in Kotlin and move only the embedding generation into a dedicated Python microservice. This keeps the main application in Kotlin while using Python only where it has a clear technical advantage.

## Changes to make it more optimal

### 1. Embedding Status Enum

Added `EmbeddingStatus` enum to make embedding states easier to understand.

- `PENDING = 0` → Embedding not generated yet
- `EMBEDDED = 1` → Embedding successfully generated and saved
- `FAILED = 2` → Maximum embedding attempts reached

The embedding process is:

New Ticket → PENDING → Generate Embedding → Save Vector → EMBEDDED

If embedding fails:

Attempt 1 → PENDING → Retry  
Attempt 2 → FAILED

The `embedding_attempts` column keeps track of failed attempts.

---

### 2. Kotlin Flow for Embedding Worker

Replaced the manual polling loop in `EmbeddingWorker` with Kotlin `Flow`.

#### Before

The worker repeatedly checked the database using a loop:


### 3. Chunking

Added **3000-token chunking** for large tickets.

- Large ticket text is split into chunks of up to 3000 tokens.
- Each chunk is embedded separately.
- Chunk embeddings are combined to create the final ticket embedding.
- This prevents very large tickets from causing model/ONNX input errors.

### 4. Vector Indexing

Added an **HNSW vector index** using `pgvector` for faster similarity search.

```sql
USING hnsw (embedding vector_cosine_ops)
```

### 5. Interval and Batch size

After every 24 hours (1440 min), we will re-scan the database for new tickets or pending tickets
The maximum batch size for processing those tickets will be 50.


## RAM Usage (1yr TICKETS IMPORT ALL AT ONCE).

   ID ProcessName RAM_MB
   -- ----------- ------
java(Ktor)        131.36 MB
python(Embeddings)      310.99 MB
Postgre     866.42 MB

