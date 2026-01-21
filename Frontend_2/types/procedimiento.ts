// Reuse the CrearPPP DTO declared in types/protocolo.ts for request bodies.
export interface Procedimiento {
  id: string
  empresaId: number
  usuarioId: number
  protocoloId?: string
  pregunta: string
  createdAt?: string
}

// No default export for types
