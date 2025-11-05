import apiFetcher from './apis'
import type { EmpresaDTO } from '@/types/empresa'

export async function getRegulations(): Promise<any[]> {
  return apiFetcher('/api/regulaciones')
}

export async function getRegulationById(id: string | number): Promise<any> {
  return apiFetcher(`/api/regulaciones/${id}`)
}

export default {
  getRegulations,
  getRegulationById,
}
