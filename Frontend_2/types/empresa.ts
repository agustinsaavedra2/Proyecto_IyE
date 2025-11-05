/**
 * EmpresaDTO: resumen mínimo que devuelve el backend para listados.
 * El backend envía únicamente `id` y `nombre`. Usa este tipo para poblar selects.
 * Nota: al crear una empresa desde el frontend enviamos `RegistrarEmpresa` (sin id).
 */
export interface EmpresaDTO {
  id: number
  nombre: string
}

export interface RegistrarEmpresa {
    admin: number
    categoriaId: number
    nombre: string
    codigoEmpresa: string
    ubicacion: string
    descripcion: string
}



// No default export for types