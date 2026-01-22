"use client"

import { useState } from 'react'
import useSWR from 'swr'
import Link from 'next/link'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Search, Plus } from 'lucide-react'
import procedimientoService from '@/lib/procedimientoService'

export default function ProcedimientoList({ empresaId }: { empresaId?: number }) {
  const [q, setQ] = useState('')
  const { data, error, isLoading, mutate } = useSWR('/api/ollama/procedimiento/all', () => procedimientoService.getAllProcedimientos())

  if (isLoading) return <div>Cargando procedimientos...</div>
  if (error) return <div>Error al cargar procedimientos</div>

  let items = data || []
  if (typeof empresaId === 'number') {
    items = items.filter((it: any) => Number(it.empresaId) === empresaId)
  }
  if (q) {
    items = items.filter((it: any) => (it.nombre || it.descripcion || it.objetivo || '').toLowerCase().includes(q.toLowerCase()))
  }

  return (
    <div className="space-y-4">
      <div className="flex gap-2 items-center">
        <Search />
        <Input placeholder="Buscar procedimientos..." value={q} onChange={(e) => setQ(e.target.value)} />
        <Button asChild>
          <Link href="/ollama/procedimiento/crear" className="inline-flex items-center gap-2">
            <Plus /> Crear
          </Link>
        </Button>
      </div>

      <div className="grid gap-4">
        {items.length === 0 ? (
          <Card className="p-4">No hay procedimientos.</Card>
        ) : (
          items.map((p: any) => (
            <Card key={p.id ?? p._id} className="p-4">
              <h3 className="font-medium">{p.nombre}</h3>
              <p className="text-sm text-muted-foreground line-clamp-3">{p.descripcion}</p>
              <div className="text-xs text-muted-foreground mt-2">Objetivo: {p.objetivo}</div>
            </Card>
          ))
        )}
      </div>
    </div>
  )
}
