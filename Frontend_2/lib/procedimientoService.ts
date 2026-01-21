import apiFetcher from './apis'
import type { CrearPPP } from '../types/protocolo'
import type { Procedimiento } from '../types/procedimiento'

export async function crearProcedimiento(data: CrearPPP): Promise<Procedimiento> {
  return apiFetcher('/api/ollama/procedimiento/crear', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export default {
  crearProcedimiento,
}
