import apiFetcher from './apis'
import type { Auditoria } from '../types/auditoria'

export async function createAuditoria(data: Auditoria): Promise<Auditoria> {
  return apiFetcher('/api/ollama/crearAuditoria', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export async function getAllAuditorias(): Promise<Auditoria[]> {
  return apiFetcher('/api/auditorias')
}

export async function getAuditoriaById(id: string): Promise<Auditoria> {
  return apiFetcher(`/api/auditorias/${encodeURIComponent(id)}`)
}

export async function updateAuditoria(id: string, data: Auditoria): Promise<Auditoria> {
  return apiFetcher(`/api/auditorias/${encodeURIComponent(id)}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  })
}

export default {
  createAuditoria,
  getAllAuditorias,
  getAuditoriaById,
  updateAuditoria,
}
