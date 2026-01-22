import apiFetcher from './apis'
import type { Regulacion } from '@/types/regulacion'

/**
 * Frontend helper for Regulacion endpoints.
 * Backend controller base path: /api/regulaciones
 */
export async function getRegulations(): Promise<Regulacion[]> {
  // controller maps GET "/" so call the base path with trailing slash to match exactly
  return apiFetcher('/api/regulaciones/all')
}

export async function getRegulationById(id: string | number): Promise<Regulacion | null> {
  return apiFetcher(`/api/regulaciones/${id}`)
}

export async function createRegulation(payload: Partial<Regulacion>): Promise<Regulacion> {
  // POST /api/regulaciones
  return apiFetcher('/api/regulaciones', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function crearRegulacion(nombre: string, contenido?: string, urlDocumento?: string, entidadEmisora?: string, anioEmision?: number): Promise<Regulacion> {
  // The backend exposes a convenience POST /api/regulaciones/crear which accepts a Regulacion body
  const body = { nombre, contenido, urlDocumento, entidadEmisora, anioEmision }
  return apiFetcher('/api/regulaciones/crear', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function updateRegulation(id: string | number, payload: Partial<Regulacion>): Promise<Regulacion> {
  return apiFetcher(`/api/regulaciones/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export async function deleteRegulation(id: string | number): Promise<void> {
  return apiFetcher(`/api/regulaciones/${id}`, {
    method: 'DELETE',
  })
}

export async function getRegulationNames(): Promise<string[]> {
  const regs = await getRegulations()
  if (!Array.isArray(regs)) return []
  return regs.map((r: Regulacion) => r.nombre ?? String(r))
}

export default {
  getRegulations,
  getRegulationById,
  createRegulation,
  crearRegulacion,
  updateRegulation,
  deleteRegulation,
  getRegulationNames,
}
