export interface CategoriaIndustria {
  id?: number
  nombre: string
  descripcion?: string
  regulaciones: string[]
  createdAt?: string
  updatedAt?: string
  deletedAt?: string | null
}

// No default export for types

export interface CategoriaDTO {
  id: number
  nombre: string
  descripcion?: string
}

export interface CategoriaRegulacionDTO {
  id: number
  nombre: string
  descripcion?: string
  regulaciones: string[]
}
