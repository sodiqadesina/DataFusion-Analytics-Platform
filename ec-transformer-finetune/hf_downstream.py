from transformers import AutoModel, AutoTokenizer, AutoModelForSequenceClassification
import torch

model_ckpt = "distilbert-base-uncased"
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model = AutoModel.from_pretrained(model_ckpt).to(device)
tokenizer = AutoTokenizer.from_pretrained(model_ckpt)

def tokenize(batch):
    return tokenizer(batch["text"], padding=True, truncation=True)

def extract_hidden_states(batch):
    # Place model inputs on the GPU
    inputs = {k:v.to(device) for k,v in batch.items() 
              if k in tokenizer.model_input_names}
    # Extract last hidden states
    with torch.no_grad():
        last_hidden_state = model(**inputs).last_hidden_state
    # Return vector for [CLS] token
    return {"hidden_state": last_hidden_state[:,0].cpu().numpy()}



# Computing hidden state for downstream training
from datasets import load_dataset
emotions = load_dataset("dair-ai/emotion")
labels = emotions['train'].features['label'].names
emotions_encoded = emotions.map(tokenize, batched=True, batch_size=None)
emotions_encoded.set_format("torch", columns=["input_ids", "attention_mask", "label"])

# Computing hidden states for the datadata, this mayt take a few mintues 
emotions_hidden = emotions_encoded.map(extract_hidden_states, batched=True)
print(emotions_encoded["train"].column_names)

# Creating feature matrix
import numpy as np
X_train = np.array(emotions_hidden["train"]["hidden_state"])
X_valid = np.array(emotions_hidden["validation"]["hidden_state"])
y_train = np.array(emotions_hidden["train"]["label"])
y_valid = np.array(emotions_hidden["validation"]["label"])
X_train.shape, X_valid.shape

# Train a simple dummy classifer
from sklearn.dummy import DummyClassifier
dummy_clf = DummyClassifier(strategy="most_frequent")
# Model training
dummy_clf.fit(X_train, y_train)
# Compute the model accuracy by the validation dataset
dummy_clf.score(X_valid, y_valid)


# Training a better classifier by LogisticRegression
from sklearn.linear_model import LogisticRegression
lr_clf = LogisticRegression(max_iter=3000)
# Model training
lr_clf.fit(X_train, y_train)
# Compute the model accuracy by the validation dataset
lr_clf.score(X_valid, y_valid)


# Finetuning the base nodel for the best classifier
from sklearn.metrics import accuracy_score, f1_score

def compute_metrics(pred):
    labels = pred.label_ids
    preds = pred.predictions.argmax(-1)
    f1 = f1_score(labels, preds, average="weighted")
    acc = accuracy_score(labels, preds)
    return {"accuracy": acc, "f1": f1}


from transformers import Trainer, TrainingArguments
num_labels = 6
model = (AutoModelForSequenceClassification.from_pretrained(model_ckpt, num_labels=num_labels).to(device))

batch_size = 64
logging_steps = len(emotions_encoded["train"]) 
model_name = "ec-finetuned-emotion"
training_args = TrainingArguments(output_dir=model_name, num_train_epochs=2, learning_rate=2e-5, per_device_train_batch_size=batch_size, per_device_eval_batch_size=batch_size, weight_decay=0.01, evaluation_strategy="epoch", disable_tqdm=False, logging_steps=logging_steps, push_to_hub=False, log_level="error")   

trainer = Trainer(model=model, args=training_args, 
                  compute_metrics=compute_metrics,
                  train_dataset=emotions_encoded["train"],
                  eval_dataset=emotions_encoded["validation"],
                  tokenizer=tokenizer)

# Finetune trainig, may take a while
trainer.train()

# Compute model accuracy score by the validation dataset
preds_output = trainer.predict(emotions_encoded["validation"])
print(preds_output.metrics)
print(preds_output.metrics['test_accuracy'])


# Save the finetuned model
model_path = "c:/enterprise/tmp/model/ec-finetuned-emotion"
trainer.save_model(model_path)


# Using the finetuned model for emotion prediction
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch

model_path = "c:/enterprise/tmp/model/ec-finetuned-emotion"
ec_tokenizer = AutoTokenizer.from_pretrained(model_path)
ec_model = AutoModelForSequenceClassification.from_pretrained(model_path)

input_text = "i didnt feel humiliated"
inputs = ec_tokenizer(input_text, return_tensors="pt")
outputs = ec_model(**inputs)
probabilities = torch.nn.functional.softmax(outputs.logits, dim=-1)
predicted_class = torch.argmax(probabilities).item()
confidence = probabilities[0][predicted_class].item()
labels = ['sadness', 'joy', 'love', 'anger', 'fear', 'surprise']
predict = {'class': labels[predicted_class], 'confidence': confidence}
print(predict)







