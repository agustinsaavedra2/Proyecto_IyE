export interface RiesgoDTO {
  id?: number
  nombre: string
  descripcion?: string
  nivel?: 'low' | 'medium' | 'high'
  empresaId?: number
  // Otros campos según tu modelo
}

// No default export for types
