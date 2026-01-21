from fastapi import FastAPI, HTTPException, Header
from pydantic import BaseModel
from ml import predict as predict_module
from ml import explain as explain_module
from pathlib import Path
import json
from typing import Optional

app = FastAPI(title='BackendIE ML Service')

class PredictRequest(BaseModel):
    text: str
    tenant_id: Optional[str] = None

class ExplainRequest(BaseModel):
    text: str
    top_k: int = 10
    tenant_id: Optional[str] = None


@app.post('/predict')
def predict(req: PredictRequest, x_categoria_id: Optional[str] = Header(None, alias='X-Categoria-id'), x_tenant_id: Optional[str] = Header(None, alias='X-Tenant-Id')):
    try:
        # prefer tenant_id in payload, then header X-Categoria-id, then X-Tenant-Id
        tenant = req.tenant_id or x_categoria_id or x_tenant_id
        res = predict_module.predict_text(req.text, tenant_id=tenant)
        # include which tenant/model path used for traceability if available
        if isinstance(res, dict):
            res['_tenant_used'] = tenant or 'global'
        return res
    except FileNotFoundError as e:
        raise HTTPException(status_code=500, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get('/health')
def health():
    return {'status':'ok'}


@app.get('/model_info')
def model_info():
    """Devuelve contenido de manifest.json asociado al modelo en uso (models_augmented o models)."""
    base = Path(__file__).parent.parent / 'ml'
    # predict_module uses same lookup; find directory and manifest
    try:
        # inspect possible dirs
        aug = base / 'models_augmented' / 'manifest.json'
        m = base / 'models' / 'manifest.json'
        if aug.exists():
            data = json.loads(aug.read_text(encoding='utf-8'))
            data['path'] = str((base / 'models_augmented').absolute())
            data['used'] = 'models_augmented'
            return data
        if m.exists():
            data = json.loads(m.read_text(encoding='utf-8'))
            data['path'] = str((base / 'models').absolute())
            data['used'] = 'models'
            return data
        raise FileNotFoundError('No manifest.json found in models_augmented or models')
    except FileNotFoundError as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post('/explain')
def explain(req: ExplainRequest, x_categoria_id: Optional[str] = Header(None, alias='X-Categoria-id'), x_tenant_id: Optional[str] = Header(None, alias='X-Tenant-Id')):
    try:
        tenant = req.tenant_id or x_categoria_id or x_tenant_id
        res = explain_module.explain_text(req.text, top_k=req.top_k, tenant_id=tenant)
        # include trace info
        if isinstance(res, dict):
            res['_tenant_used'] = tenant or 'global'
        return res
    except FileNotFoundError as e:
        raise HTTPException(status_code=500, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
