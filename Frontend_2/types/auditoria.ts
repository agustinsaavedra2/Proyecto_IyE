export interface Auditoria {
  id?: string
  empresaId?: number
  tipo?: string
  objetivo?: string
  alcance?: string
  auditorLider?: number
  fecha?: string
  score?: number
  hallazgosCriticosMsj?: string[]
  hallazgosMayoresMsj?: string[]
  hallazgosMenoresMsj?: string[]
  recomendaciones?: string
}

// No default export for types
