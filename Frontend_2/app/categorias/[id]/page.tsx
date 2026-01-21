"use client"

import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import BackButton from '@/components/ui/back-button'
import LoadingOverlay from '@/components/ui/loading-overlay'
import categoriaService from '@/lib/categoriaService'
import type { CategoriaRegulacionDTO } from '@/types/categoria'

export default function CategoriaDetailPage() {
  const params = useParams()
  const router = useRouter()
  const id = params?.id

  const [loading, setLoading] = useState(true)
  const [categoria, setCategoria] = useState<CategoriaRegulacionDTO | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      if (!id) {
        setError('ID de categoría no proporcionado')
        setLoading(false)
        return
      }

      setLoading(true)
      try {
        const list = await categoriaService.listarTodasCategorias()
        if (!mounted) return
        const found = (list || []).find((c: any) => String(c.id) === String(id))
        if (!found) {
          setError('Categoría no encontrada')
          setCategoria(null)
        } else {
          setCategoria(found)
        }
      } catch (e: any) {
        console.error('Failed to load categoria', e)
        setError(e?.message || 'Error al cargar categoría')
      } finally {
        if (mounted) setLoading(false)
      }
    })()

    return () => {
      mounted = false
    }
  }, [id])

  return (
    <div className="min-h-screen bg-background">
      <main className="container mx-auto px-4 py-8">
        <div className="mb-8 flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold mb-2">Detalle de Categoría</h1>
            <p className="text-muted-foreground">Muestra el string ordenado de regulaciones asociado a la categoría</p>
          </div>

          <div className="flex items-center gap-2">
            <BackButton />
            <Button asChild>
              <a href="/categorias/crear">Crear categoría</a>
            </Button>
          </div>
        </div>

        <LoadingOverlay loading={loading} fullScreen={false} message={loading ? 'Cargando categoría...' : undefined} />

        {error && <Card className="p-4 mb-4 text-red-600">Error: {error}</Card>}

        {categoria && (
          <Card className="p-6 max-w-3xl">
            <h2 className="text-xl font-semibold mb-2">{categoria.nombre}</h2>
            {categoria.descripcion && <p className="text-sm text-muted-foreground mb-4">{categoria.descripcion}</p>}

            <div className="mb-4">
              <h3 className="font-medium">String ordenado (CSV)</h3>
              <pre className="bg-muted p-3 rounded mt-2 text-sm overflow-x-auto">{(categoria.regulaciones || []).join(', ')}</pre>
            </div>

            <div>
              <h3 className="font-medium">Regulaciones (lista)</h3>
              <ol className="list-decimal pl-5 mt-2">
                {(categoria.regulaciones || []).map((r, i) => (
                  <li key={i} className="text-sm text-muted-foreground mb-1">{r}</li>
                ))}
              </ol>
            </div>
          </Card>
        )}
      </main>
    </div>
  )
}
