import apiFetcher from './apis'
import type { CategoriaIndustria } from '../types/categoria'

export async function crearCategoria(data: CategoriaIndustria): Promise<CategoriaIndustria> {
  return apiFetcher('/api/categorias/crear', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export async function listarCategoriasDTO(): Promise<Array<{ id: number; nombre: string; descripcion?: string }>> {
  return apiFetcher('/api/categorias/dto')
}

export async function listarTodasCategorias(): Promise<Array<{ id: number; nombre: string; descripcion?: string; regulaciones: string[] }>> {
  return apiFetcher('/api/categorias/listar')
}

export default {
  crearCategoria,
  listarCategoriasDTO,
  listarTodasCategorias,
}
