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

Here are some useful links to get you started:

* [Ktor Documentation](https://ktor.io/docs/home.html)
* [Ktor GitHub page](https://github.com/ktorio/ktor)
* [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). [Request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up).

## Features

Here's a list of features included in this project:

| Name                                                                                  | Description                                                                        |
|---------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| [Content Negotiation](https://start.ktor.io/p/io.ktor/server-content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [Call Logging](https://start.ktor.io/p/io.ktor/server-call-logging)                   | Logs client requests                                                               |
| [Status Pages](https://start.ktor.io/p/io.ktor/server-status-pages)                   | Provides exception handling for routes                                             |
| [kotlinx.serialization](https://start.ktor.io/p/io.ktor/server-kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |
| [kotlinx.rpc](https://start.ktor.io/p/org.jetbrains/kotlinx-rpc)                      | Adds remote procedure call (RPC) routing                                           |
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
