"use client"

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { DashboardNav } from '@/components/dashboard/dashboard-nav'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import LoadingOverlay from '@/components/ui/loading-overlay'
import categoriaService from '@/lib/categoriaService'
import type { CategoriaRegulacionDTO } from '@/types/categoria'

export default function CategoriasPage() {
  const [loading, setLoading] = useState(false)
  const [categorias, setCategorias] = useState<CategoriaRegulacionDTO[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const list = await categoriaService.listarTodasCategorias()
        if (!mounted) return
        setCategorias(list || [])
      } catch (e: any) {
        console.error('Failed to load categorias', e)
        setError(e?.message || String(e))
      } finally {
        if (mounted) setLoading(false)
      }
    })()

    return () => {
      mounted = false
    }
  }, [])

  return (
    <div className="min-h-screen bg-background">
      <DashboardNav />

      <main className="container mx-auto px-4 py-8">
        <div className="mb-8 flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold mb-2">Categorías</h1>
            <p className="text-muted-foreground">Listado de categorías de industria y sus regulaciones asociadas</p>
          </div>

          <div className="flex items-center gap-2">
            <Button variant="ghost" onClick={() => history.back()}>
              Volver
            </Button>
            <Button asChild>
              <Link href="/categorias/crear">Crear categoría</Link>
            </Button>
          </div>
        </div>

        <LoadingOverlay loading={loading} fullScreen={false} message={loading ? 'Cargando categorías...' : undefined} />

        {error && <Card className="p-4 mb-4 text-red-600">Error: {error}</Card>}

        <div className="grid gap-4">
          {categorias.length === 0 ? (
            <Card className="p-8 text-center">
              <p className="text-muted-foreground">No hay categorías registradas</p>
              <div className="mt-4">
                <Button asChild>
                  <Link href="/categorias/crear">Crear la primera categoría</Link>
                </Button>
              </div>
            </Card>
          ) : (
            categorias.map((c) => (
              <Card key={c.id} className="p-6">
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="text-lg font-semibold">{c.nombre}</h3>
                    {c.descripcion && <p className="text-sm text-muted-foreground">{c.descripcion}</p>}

                    {c.regulaciones && c.regulaciones.length > 0 && (
                      <div className="mt-3 text-sm">
                        <strong>Regulaciones:</strong>
                        <ul className="list-disc pl-5 mt-1">
                          {c.regulaciones.map((r, i) => (
                            <li key={i} className="text-muted-foreground">{r}</li>
                          ))}
                        </ul>
                      </div>
                    )}
                  </div>

                    <div className="flex items-center gap-2">
                      {/* future: add edit/delete actions */}
                      <Button asChild>
                        <Link href={`/categorias/${c.id}`}>Ver regulaciones</Link>
                      </Button>
                    </div>
                </div>
              </Card>
            ))
          )}
        </div>
      </main>
    </div>
  )
}
