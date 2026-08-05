from pathlib import Path

import numpy as np
import onnxruntime as ort

from app.pooling import l2_normalize, mean_pool
from app.tokenizer import tokenizer_service


MODEL_DIRECTORY = Path(__file__).resolve().parent.parent / "model"
MODEL_PATH = MODEL_DIRECTORY / "model_int8.onnx"


class EmbeddingModel:

    def __init__(self) -> None:
        self.session = ort.InferenceSession(
            str(MODEL_PATH),
            providers=["CPUExecutionProvider"]
        )

    @property
    def input_names(self) -> list[str]:
        return [tensor.name for tensor in self.session.get_inputs()]

    @property
    def output_names(self) -> list[str]:
        return [tensor.name for tensor in self.session.get_outputs()]

    def generate_embedding(
        self,
        text: str
    ) -> list[float]:

        input_ids, attention_mask = tokenizer_service.tokenize(text)

        input_ids = np.asarray(
            [input_ids],
            dtype=np.int64
        )

        attention_mask = np.asarray(
            [attention_mask],
            dtype=np.int64
        )

        outputs = self.session.run(
            None,
            {
                "input_ids": input_ids,
                "attention_mask": attention_mask
            }
        )

        last_hidden_state = outputs[0]

        embedding = mean_pool(
            last_hidden_state,
            attention_mask
        )

        embedding = l2_normalize(
            embedding
        )

        return embedding[0].astype(np.float32).tolist()


embedding_model = EmbeddingModel()