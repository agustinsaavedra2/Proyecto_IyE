import apiFetcher from './apis'

import type { EmpresaDTO, RegistrarEmpresa } from '@/types/empresa'

export async function getEmpresasDTO(): Promise<EmpresaDTO[]> {
  return apiFetcher('/api/empresas/resumen')
}

export async function crearEmpresa(data: RegistrarEmpresa): Promise<RegistrarEmpresa> {
  return apiFetcher('/api/empresas/registrar', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export default {
  getEmpresasDTO,
  crearEmpresa,
}