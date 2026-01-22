import apiFetcher from './apis'
import type { CrearPPP, Protocolo } from '@/types/protocolo'

export async function crearProtocolo(data: CrearPPP): Promise<Protocolo> {
  return apiFetcher('/api/ollama/protocolo/crear', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export async function getAllProtocolos(): Promise<Protocolo[]> {
  return apiFetcher('/api/ollama/protocolo/all', {
    method: 'GET',
  })
}

export default {
  crearProtocolo,
  getAllProtocolos,
}
