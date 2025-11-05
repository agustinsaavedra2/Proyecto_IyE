"use client"

import { Card } from '@/components/ui/card'
import { useEffect, useState } from 'react'
import ollamaService from '@/lib/ollamaResponseService'
import type { OllamaResponse } from '@/types/ollama'

export default function OllamaIndexPage() {
  const [items, setItems] = useState<OllamaResponse[]>([])

  useEffect(() => {
    ;(async () => {
      try {
        const res = await ollamaService.getAllResponses()
        setItems(res)
      } catch (_) {}
    })()
  }, [])

  return (
    <div className="max-w-4xl mx-auto py-12">
      <Card className="p-6">
        <h2 className="text-lg font-medium mb-4">Ollama Responses</h2>
        {items.length === 0 ? (
          <div>No hay respuestas aún</div>
        ) : (
          <ul className="space-y-3">
            {items.map((it) => (
              <li key={it.id} className="border p-3 rounded">
                <div className="text-sm text-gray-600">ID: {it.id}</div>
                <div className="font-medium">Pregunta: {it.pregunta}</div>
                <div className="text-sm text-gray-700">Respuesta: {it.respuesta}</div>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  )
}
