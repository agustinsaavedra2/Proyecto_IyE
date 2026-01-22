"use client"

import React from 'react'
import useSWR from 'swr'
import politicaService from '../../lib/politicaService'
import empresaService from '../../lib/empresaService'
import type { PoliticaDTO } from '../../types/politica'

export default function PoliticaList({ empresaId }: { empresaId?: number }) {
  const key = typeof empresaId === 'number' && !Number.isNaN(empresaId) ? ['politicas', empresaId] : 'politicas-all'

  const { data: politicas, error, isLoading } = useSWR(key, async () => {
    if (typeof empresaId === 'number' && !Number.isNaN(empresaId)) {
      return politicaService.getPoliticasPorEmpresa(empresaId)
    }

    // No empresaId: fetch all empresas then aggregate politicas per empresa
    const empresas = await empresaService.getEmpresasDTO()
    if (!empresas || empresas.length === 0) return []

    const lists = await Promise.all(empresas.map((e) => politicaService.getPoliticasPorEmpresa(Number(e.id))))
    // flatten and deduplicate by id
    const merged: PoliticaDTO[] = ([] as PoliticaDTO[]).concat(...lists)
    const seen = new Set<string | number>()
    const unique = merged.filter((p) => {
      const id = p.id ?? JSON.stringify(p)
      if (seen.has(id)) return false
      seen.add(id)
      return true
    })
    return unique
  })

  if (error) return <div>Fallo al cargar políticas. Intenta de nuevo.</div>
  if (isLoading || !politicas) return <div>Cargando políticas...</div>

  return (
    <div>
      {politicas.length === 0 ? (
        <div className="text-sm text-muted-foreground">No hay políticas registradas.</div>
      ) : (
        <ul className="space-y-2 text-sm">
          {politicas.map((p: PoliticaDTO) => (
            <li key={p.id} className="border-b py-2">
              <div className="font-medium line-clamp-1">{p.titulo}</div>
              <div className="text-xs text-muted-foreground line-clamp-2">{p.resumen ?? p.contenido ?? ''}</div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
