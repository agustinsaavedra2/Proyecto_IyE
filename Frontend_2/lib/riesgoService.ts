import apiFetcher from './apis'
import type { RiesgoDTO } from '@/types/riesgo'

export async function getRiesgos(): Promise<RiesgoDTO[]> {
  return apiFetcher('/api/riesgos')
}

export async function crearRiesgo(data: RiesgoDTO): Promise<RiesgoDTO> {
  return apiFetcher('/api/riesgos', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export async function generarRiesgosConIA(payload: any): Promise<any> {
  return apiFetcher('/api/riesgos/generar', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export default {
  getRiesgos,
  crearRiesgo,
  generarRiesgosConIA,
}
