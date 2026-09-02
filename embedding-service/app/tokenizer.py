from pathlib import Path

from tokenizers import Tokenizer

MODEL_DIRECTORY = Path(__file__).resolve().parent.parent / "model"
TOKENIZER_PATH = MODEL_DIRECTORY / "tokenizer.json"


class TokenizerService:

    def __init__(self) -> None:
        self._tokenizer = Tokenizer.from_file(str(TOKENIZER_PATH))

    def tokenize(self, text: str) -> tuple[list[int], list[int]]:
        encoding = self._tokenizer.encode(text)

        return (
            encoding.ids,
            encoding.attention_mask
        )


tokenizer_service = TokenizerService()