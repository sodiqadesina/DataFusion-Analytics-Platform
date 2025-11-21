from transformers import AutoTokenizer

# Get tokenzier of pretrained transformer model
model_ckpt = "distilbert-base-uncased"
tokenizer = AutoTokenizer.from_pretrained(model_ckpt)
tokenizer.vocab_size
tokenizer.model_input_names

# Encode tokens of a given string
text = "Tokenizing text is a core task of NLP."
encoded_text = tokenizer(text)
print(encoded_text)

# Decode tokens
tokens = tokenizer.convert_ids_to_tokens(encoded_text.input_ids)
print(tokens)
print(tokenizer.convert_tokens_to_string(tokens))


# Let's define a function for batch encoding
def tokenize(batch):
    return tokenizer(batch["text"], padding=True, truncation=True)

from datasets import load_dataset
emotions = load_dataset("dair-ai/emotion")
print(tokenize(emotions["train"][:2]))

# Let's encode the emotion dataset
emotions_encoded = emotions.map(tokenize, batched=True, batch_size=None)
emotions_encoded 
print(emotions_encoded["train"].column_names)
