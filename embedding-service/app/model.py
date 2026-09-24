from pathlib import Path
import time

import numpy as np
import onnxruntime as ort

from app.pooling import l2_normalize, mean_pool
from app.tokenizer import tokenizer_service


MODEL_DIRECTORY = Path(__file__).resolve().parent.parent / "model"
MODEL_PATH = MODEL_DIRECTORY / "model_int8.onnx"

# ============================================================
# CHUNK CONFIGURATION
# ============================================================

# We tested 3075 tokens successfully.
# Using 3000 gives us a small safety margin.
MAX_CHUNK_TOKENS = 3000


class EmbeddingModel:

    def __init__(self) -> None:

        self.session = ort.InferenceSession(
            str(MODEL_PATH),
            providers=["CPUExecutionProvider"]
        )

        # ====================================================
        # ONNX MODEL INFORMATION
        # ====================================================

        print()
        print("=" * 60)
        print("              ONNX MODEL INFORMATION")
        print("=" * 60)

        for tensor in self.session.get_inputs():

            print(
                f"Input name : {tensor.name}"
            )

            print(
                f"Input shape: {tensor.shape}"
            )

            print(
                f"Input type : {tensor.type}"
            )

            print("-" * 60)

        print("=" * 60)
        print()


    @property
    def input_names(self) -> list[str]:

        return [
            tensor.name
            for tensor in self.session.get_inputs()
        ]


    @property
    def output_names(self) -> list[str]:

        return [
            tensor.name
            for tensor in self.session.get_outputs()
        ]


    # ========================================================
    # EMBEDDING ONE TOKEN CHUNK
    # ========================================================

    def _embed_token_chunk(
        self,
        input_ids: list[int],
        attention_mask: list[int]
    ) -> np.ndarray:

        input_ids_array = np.asarray(
            [input_ids],
            dtype=np.int64

        )

        attention_mask_array = np.asarray(
            [attention_mask],
            dtype=np.int64
        )

        # ----------------------------------------------------
        # ONNX INFERENCE
        # ----------------------------------------------------

        outputs = self.session.run(
            None,
            {
                "input_ids": input_ids_array,
                "attention_mask": attention_mask_array
            }
        )

        # ----------------------------------------------------
        # LAST HIDDEN STATE
        # ----------------------------------------------------

        last_hidden_state = outputs[0]

        # ----------------------------------------------------
        # MEAN POOLING
        # ----------------------------------------------------

        embedding = mean_pool(
            last_hidden_state,
            attention_mask_array
        )

        # ----------------------------------------------------
        # L2 NORMALIZATION
        # ----------------------------------------------------

        embedding = l2_normalize(
            embedding
        )

        return embedding[0].astype(
            np.float32
        )


    # ========================================================
    # SPLIT TOKENS INTO CHUNKS
    # ========================================================

    def _split_into_chunks(
        self,
        input_ids: list[int],
        attention_mask: list[int]
    ) -> list[tuple[list[int], list[int]]]:

        chunks = []

        total_tokens = len(input_ids)

        start = 0

        while start < total_tokens:

            end = min(
                start + MAX_CHUNK_TOKENS,
                total_tokens
            )

            chunk_ids = input_ids[start:end]

            chunk_mask = attention_mask[start:end]

            chunks.append(
                (
                    chunk_ids,
                    chunk_mask
                )
            )

            start = end

        return chunks


    # ========================================================
    # GENERATE EMBEDDING
    # ========================================================

    def generate_embedding(
        self,
        text: str
    ) -> list[float]:

        total_start = time.perf_counter()

        # ====================================================
        # 1. TOKENIZATION
        # ====================================================

        tokenization_start = time.perf_counter()

        input_ids, attention_mask = (
            tokenizer_service.tokenize(text)
        )

        tokenization_time = (
            time.perf_counter()
            - tokenization_start
        ) * 1000

        token_count = len(input_ids)

        print()
        print("=" * 60)
        print("              EMBEDDING REQUEST")
        print("=" * 60)

        print(
            f"Text length : {len(text)} characters"
        )

        print(
            f"Token count : {token_count}"
        )

        print(
            f"Chunk size  : {MAX_CHUNK_TOKENS} tokens"
        )

        print("=" * 60)


        # ====================================================
        # 2. NORMAL TICKET
        # ====================================================

        if token_count <= MAX_CHUNK_TOKENS:

            print(
                "Embedding mode : SINGLE CHUNK"
            )

            onnx_start = time.perf_counter()

            embedding = self._embed_token_chunk(
                input_ids,
                attention_mask
            )

            onnx_time = (
                time.perf_counter()
                - onnx_start
            ) * 1000

            chunk_count = 1


        # ====================================================
        # 3. LARGE TICKET
        # ====================================================

        else:

            print(
                "Embedding mode : MULTIPLE CHUNKS"
            )

            chunks = self._split_into_chunks(
                input_ids,
                attention_mask
            )

            chunk_count = len(chunks)

            print(
                f"Chunks created : {chunk_count}"
            )

            chunk_embeddings = []

            onnx_start = time.perf_counter()

            for index, (
                chunk_ids,
                chunk_mask
            ) in enumerate(chunks, start=1):

                print(
                    f"Processing chunk "
                    f"{index}/{chunk_count} "
                    f"({len(chunk_ids)} tokens)"
                )

                chunk_embedding = (
                    self._embed_token_chunk(
                        chunk_ids,
                        chunk_mask
                    )
                )

                chunk_embeddings.append(
                    chunk_embedding
                )

            onnx_time = (
                time.perf_counter()
                - onnx_start
            ) * 1000

            # ------------------------------------------------
            # COMBINE CHUNK EMBEDDINGS
            # ------------------------------------------------

            embedding_matrix = np.vstack(
                chunk_embeddings
            )

            embedding = np.mean(
                embedding_matrix,
                axis=0
            )

            # ------------------------------------------------
            # NORMALIZE FINAL EMBEDDING
            # ------------------------------------------------

            embedding = l2_normalize(
                embedding.reshape(1, -1)
            )[0].astype(
                np.float32
            )


        # ====================================================
        # PERFORMANCE
        # ====================================================

        total_time = (
            time.perf_counter()
            - total_start
        ) * 1000


        # ====================================================
        # PERFORMANCE LOG
        # ====================================================

        print()
        print("=" * 60)
        print("              EMBEDDING PERFORMANCE")
        print("=" * 60)

        print(
            f"Text length             : "
            f"{len(text)} characters"
        )

        print(
            f"Token count             : "
            f"{token_count}"
        )

        print(
            f"Chunks                  : "
            f"{chunk_count}"
        )

        print()

        print(
            f"Tokenization time       : "
            f"{tokenization_time:.2f} ms"
        )

        print(
            f"ONNX inference time     : "
            f"{onnx_time:.2f} ms"
        )

        print(
            f"Total embedding time    : "
            f"{total_time:.2f} ms"
        )

        print()

        print(
            f"Final embedding size    : "
            f"{len(embedding)}"
        )

        print("=" * 60)
        print()


        # ====================================================
        # RETURN FINAL 1024-D VECTOR
        # ====================================================

        return embedding.tolist()


# ============================================================
# GLOBAL EMBEDDING MODEL
# ============================================================

embedding_model = EmbeddingModel()