"use client"

import { useEffect, useState } from 'react'
import { Card } from '@/components/ui/card'
import auditoriaService from '@/lib/auditoriaService'
import type { Auditoria } from '@/types/auditoria'

export default function AuditoriasIndexPage() {
  const [items, setItems] = useState<Auditoria[]>([])

  useEffect(() => {
    ;(async () => {
      try {
        const res = await auditoriaService.getAllAuditorias()
        setItems(res)
      } catch (_) {}
    })()
  }, [])

  return (
    <div className="max-w-4xl mx-auto py-12">
      <Card className="p-6">
        <h2 className="text-lg font-medium mb-4">Auditorías</h2>
        {items.length === 0 ? (
          <div>No hay auditorías</div>
        ) : (
          <ul className="space-y-3">
            {items.map((a) => (
              <li key={a.id} className="border p-3 rounded">
                <div className="text-sm text-gray-600">ID: {a.id}</div>
                <div className="font-medium">Tipo: {a.tipo}</div>
                <div className="text-sm">Objetivo: {a.objetivo}</div>
                <div className="text-sm text-gray-700">Score: {a.score ?? 'N/A'}</div>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  )
}
