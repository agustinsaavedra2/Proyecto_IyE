export interface Regulacion {
  id?: string
  nombre: string
  contenido?: string
  urlDocumento?: string
  entidadEmisora?: string
  anioEmision?: number
}

export type RegulacionDTO = Regulacion

export default Regulacion
