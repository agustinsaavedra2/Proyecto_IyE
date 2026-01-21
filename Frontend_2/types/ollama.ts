export interface OllamaResponse {
  id?: string
  empresaId?: number
  usuarioId?: number
  pregunta?: string
  respuesta?: string
  // timestamps opcionales
  createdAt?: string
}

export interface CrearAuditoria {
  empresaId: number
  tipo: string
  objetivo: string
  auditorLiderId: number
  // El backend espera UNA sola política como id (string, guardada en Mongo).
  politicaId: string
}

// No default export for types
