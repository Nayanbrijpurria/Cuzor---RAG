import numpy as np


def mean_pool(
    last_hidden_state: np.ndarray,
    attention_mask: np.ndarray
) -> np.ndarray:
    """
    Mean pooling over the sequence dimension while ignoring padding tokens.

    Parameters
    ----------
    last_hidden_state
        Shape:
        (batch_size, sequence_length, hidden_size)

    attention_mask
        Shape:
        (batch_size, sequence_length)

    Returns
    -------
    Shape:
    (batch_size, hidden_size)
    """

    mask = attention_mask.astype(np.float32)
    mask = np.expand_dims(mask, axis=-1)

    masked_embeddings = last_hidden_state * mask

    summed = np.sum(masked_embeddings, axis=1)

    token_count = np.clip(
        np.sum(mask, axis=1),
        a_min=1e-9,
        a_max=None
    )

    return summed / token_count


def l2_normalize(
    embeddings: np.ndarray
) -> np.ndarray:
    """
    L2 normalize embeddings.

    Input:
        (batch_size, hidden_size)

    Output:
        (batch_size, hidden_size)
    """

    norm = np.linalg.norm(
        embeddings,
        axis=1,
        keepdims=True
    )

    norm = np.clip(
        norm,
        a_min=1e-12,
        a_max=None
    )

    return embeddings / norm