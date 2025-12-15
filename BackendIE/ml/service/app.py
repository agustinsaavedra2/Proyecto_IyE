from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from ml import predict as predict_module

app = FastAPI(title='BackendIE ML Service')

class PredictRequest(BaseModel):
    text: str


@app.post('/predict')
def predict(req: PredictRequest):
    try:
        res = predict_module.predict_text(req.text)
        return res
    except FileNotFoundError as e:
        raise HTTPException(status_code=500, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get('/health')
def health():
    return {'status':'ok'}

