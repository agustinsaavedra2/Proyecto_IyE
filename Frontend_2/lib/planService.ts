import apiFetcher from './apis'
import type { Plan } from '../types/plan'

export async function crearPlan(data: Plan): Promise<Plan> {
  return apiFetcher('/api/planes/crear', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export default {
  crearPlan,
}
