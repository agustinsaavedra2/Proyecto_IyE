import apiFetcher from './apis'
import type { SuscribirseDTO, Suscripcion } from '@/types/suscripcion'

export async function suscribirse(data: SuscribirseDTO): Promise<Suscripcion> {
  return apiFetcher('/api/suscripcion/suscribirse', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export default {
  suscribirse,
}
