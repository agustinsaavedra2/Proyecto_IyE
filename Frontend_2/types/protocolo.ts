export interface CrearPPP {
  empresaId: number
  usuarioId: number
  politicaId: string
  pregunta: string
  protocoloId?: string
}

export interface Protocolo {
  id: string
  empresaId: number
  usuarioId: number
  politicaId: string
  pregunta: string
  protocoloId?: string
  createdAt?: string
}
