import apiFetcher from './apis'
import type { OllamaResponse, CrearAuditoria } from '../types/ollama'

export async function getAllResponses(): Promise<OllamaResponse[]> {
  return apiFetcher('/api/ollama')
}

export async function getResponseById(id: string): Promise<OllamaResponse> {
  return apiFetcher(`/api/ollama/${encodeURIComponent(id)}`)
}

export async function createResponse(data: OllamaResponse): Promise<OllamaResponse> {
  return apiFetcher('/api/ollama', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export async function crearAuditoria(data: CrearAuditoria): Promise<OllamaResponse> {
  return apiFetcher('/api/ollama/crearAuditoria', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export async function updateResponse(id: string, data: OllamaResponse): Promise<OllamaResponse> {
  return apiFetcher(`/api/ollama/${encodeURIComponent(id)}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  })
}

export async function deleteResponse(id: string): Promise<void> {
  return apiFetcher(`/api/ollama/${encodeURIComponent(id)}`, {
    method: 'DELETE',
  })
}

export default {
  getAllResponses,
  getResponseById,
  createResponse,
  updateResponse,
  deleteResponse,
  crearAuditoria,
}
