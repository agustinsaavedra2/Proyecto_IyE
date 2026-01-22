"use client"

import React from 'react'
import Link from 'next/link'
import { Button } from '@/components/ui/button'
import useSWR from 'swr'
import protocoloService from '@/lib/protocoloService'
import type { Protocolo } from '@/types/protocolo'

export default function ProtocoloList({ empresaId }: { empresaId?: number }) {
  const { data, error, isLoading } = useSWR('/api/ollama/protocolo/all', () => protocoloService.getAllProtocolos())

  if (isLoading) return <div>Cargando protocolos...</div>
  if (error) return <div>Error al cargar protocolos</div>

  let items: Protocolo[] = data || []
  if (typeof empresaId === 'number') {
    items = items.filter((i) => Number(i.empresaId) === empresaId)
  }

  return (
    <section>
      <div className="flex items-center justify-between mb-2">
        <div />
        <div className="flex gap-2">
          <Button asChild size="sm" variant="ghost">
            <Link href="/ollama/protocolo">Ver todo</Link>
          </Button>
          <Button asChild size="sm">
            <Link href="/ollama/protocolo/crear">Crear</Link>
          </Button>
        </div>
      </div>

      {items.length === 0 ? (
        <div className="text-sm text-muted-foreground">No hay protocolos.</div>
      ) : (
        <ul className="space-y-2 text-sm">
          {items.map((p: Protocolo, idx: number) => (
            <li key={p.id ?? (p._id ?? idx)} className="border-b py-2">
              <div className="font-medium line-clamp-1">{p.pregunta ?? 'Sin pregunta'}</div>
              <div className="text-xs text-muted-foreground line-clamp-2">{p.protocoloId ?? ''}</div>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
