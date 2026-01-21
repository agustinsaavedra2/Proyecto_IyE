export interface SuscribirseDTO {
  empresaId: number
  plan: string
  adminId: number
}

// Respuesta mínima esperada
export interface Suscripcion {
  id: number
  empresaId: number
  plan: string
  adminId: number
  startedAt?: string
}
