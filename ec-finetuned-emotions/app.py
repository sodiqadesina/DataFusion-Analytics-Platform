from flask import Flask, request, jsonify
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch

model_path = r"c:\enterprise\tmp\model\ec-finetuned-emotion"
tokenizer = AutoTokenizer.from_pretrained(model_path)
model = AutoModelForSequenceClassification.from_pretrained(model_path)


# Emotion labels
labels = ['anger', 'fear', 'joy', 'love', 'sadness', 'surprise']

# Create Flask app
app = Flask(__name__)

@app.route('/')
def info():
    return jsonify({'message': 'This is an emotion analysis microservice.'})

@app.route('/predict', methods=['POST'])
def get_emotion():
    # Get the input text from request
    data = request.json
    if data['type'] == "emotion":
        input_text = data['text']

        # Tokenize input text
        inputs = tokenizer(input_text, return_tensors="pt")

        # Get model output
        outputs = model(**inputs)
        probabilities = torch.nn.functional.softmax(outputs.logits, dim=-1)

        # Get predicted class and confidence
        predicted_class = torch.argmax(probabilities).item()
        confidence = probabilities[0][predicted_class].item()

        # Create response
        response = {
            "class": labels[predicted_class],
            "confidence": confidence
        }
        return jsonify(response)

    return jsonify({"error": "Invalid request"}), 400

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8000)
