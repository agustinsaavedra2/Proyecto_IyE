import apiFetcher from './apis'
import type { CrearPPP } from '../types/protocolo'
import type { PoliticaDTO, PoliticaEmpresa } from '../types/politica'

export async function crearPolitica(data: CrearPPP): Promise<PoliticaEmpresa> {
  return apiFetcher('/api/ollama/politica/crear', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export async function getPoliticasPorEmpresa(empresaId: number): Promise<PoliticaDTO[]> {
  return apiFetcher(`/api/ollama/politica/politicaempresa?empresaId=${encodeURIComponent(String(empresaId))}`)
}

export default {
  crearPolitica,
  getPoliticasPorEmpresa,
}
