// Reuse the CrearPPP DTO declared in types/protocolo.ts for request bodies.
export interface PoliticaEmpresa {
  id: string
  empresaId: number
  usuarioId: number
  pregunta: string
  createdAt?: string
}

// DTO returned by /politica/politicaempresa
export interface PoliticaDTO {
  id: string
  titulo: string
}

// No default export for types
