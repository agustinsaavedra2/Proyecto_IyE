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
  // En la nueva versión el backend acepta una lista de ids de políticas a evaluar
  idsDePoliticasAEvaluar: string[]
}

// No default export for types
