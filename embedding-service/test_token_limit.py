from app.tokenizer import tokenizer_service
from app.model import embedding_model


def test_tokens(token_count: int):

    text = "customer cannot connect to the UPS " * token_count

    ids, mask = tokenizer_service.tokenize(text)

    print()
    print("=" * 60)
    print(f"REQUESTED SIZE : {token_count}")
    print(f"ACTUAL TOKENS  : {len(ids)}")
    print("=" * 60)

    try:
        embedding_model.generate_embedding(text)

        print("RESULT         : SUCCESS")

    except Exception as e:

        print("RESULT         : FAILED")
        print("ERROR          :", type(e).__name__)
        print("MESSAGE        :", str(e))


for size in [
    128,
    256,
    512,
    768,
    1024,
    1536,
    2048,
    4096
]:
    test_tokens(size)