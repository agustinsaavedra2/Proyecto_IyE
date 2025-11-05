"use client"

import { useEffect, useState } from 'react'
import { Card } from '@/components/ui/card'
import { DashboardNav } from '@/components/dashboard/dashboard-nav'
import empresaService from '@/lib/empresaService'
import LoadingOverlay from '@/components/ui/loading-overlay'
import { Button } from '@/components/ui/button'
import BackButton from '@/components/ui/back-button'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import type { EmpresaDTO } from '@/types/empresa'

export default function EmpresasPage() {
  const [loading, setLoading] = useState(false)
  const [empresas, setEmpresas] = useState<EmpresaDTO[]>([])
  const [error, setError] = useState<string | null>(null)
  const router = useRouter()

  useEffect(() => {
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const list = await empresaService.getEmpresasDTO()
        if (!mounted) return
        setEmpresas(list || [])
      } catch (e: any) {
        console.error('Failed to load empresas', e)
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
            <h1 className="text-3xl font-bold mb-2">Empresas</h1>
            <p className="text-muted-foreground">Listado de empresas registradas</p>
          </div>

            <div className="flex items-center gap-2">
            <BackButton />
            <Button asChild>
              <Link href="/empresas/registrar">Registrar Empresa</Link>
            </Button>
          </div>
        </div>

        <LoadingOverlay loading={loading} fullScreen={false} message={loading ? 'Cargando empresas...' : undefined} />

        {error && <Card className="p-4 mb-4 text-red-600">Error: {error}</Card>}

        <Card className="p-0 overflow-auto">
          <table className="w-full table-fixed text-sm">
            <thead>
              <tr className="text-left border-b">
                <th className="p-3">ID</th>
                <th className="p-3">Nombre</th>
              </tr>
            </thead>
            <tbody>
              {empresas.length === 0 ? (
                <tr>
                  <td colSpan={2} className="p-4 text-center text-muted-foreground">
                    No hay empresas encontradas
                  </td>
                </tr>
              ) : (
                empresas.map((e) => (
                  <tr key={e.id} className="border-b last:border-b-0">
                    <td className="p-3">{e.id}</td>
                    <td className="p-3 font-medium">{e.nombre}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </Card>
      </main>
    </div>
  )
}
