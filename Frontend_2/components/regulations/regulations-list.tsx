"use client"

import { useState, useEffect } from "react"
import useSWR from "swr"
import Link from 'next/link'
import { useSearchParams, useRouter } from 'next/navigation'
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Search, Plus, Edit, Trash2, ExternalLink } from "lucide-react"
import { motion } from "framer-motion"
import { LoadingSkeleton } from "@/components/ui/loading-skeleton"
import apiFetcher from '@/lib/apis'
import categoriaService from '@/lib/categoriaService'

export function RegulationsList() {
  const [searchQuery, setSearchQuery] = useState("")
  const { data: regulations, error, isLoading, mutate } = useSWR('/api/regulaciones', apiFetcher)
  const searchParams = useSearchParams()
  const router = useRouter()

  // If a categoryId is provided via query param, load its allowed regulaciones
  const categoriaId = searchParams?.get('categoriaId')
  const [allowedRegulaciones, setAllowedRegulaciones] = useState<string[] | null>(null)
  const [selectedCategoriaName, setSelectedCategoriaName] = useState<string | null>(null)

  useEffect(() => {
    let mounted = true
    ;(async () => {
      if (!categoriaId) {
        if (mounted) {
          setAllowedRegulaciones(null)
          setSelectedCategoriaName(null)
        }
        return
      }

      try {
        const cats = await categoriaService.listarTodasCategorias()
        if (!mounted) return
        const found = (cats || []).find((c: any) => String(c.id) === String(categoriaId))
        if (found) {
          setAllowedRegulaciones(found.regulaciones || [])
          setSelectedCategoriaName(found.nombre || null)
        } else {
          setAllowedRegulaciones([])
          setSelectedCategoriaName(null)
        }
      } catch (e) {
        console.error('Failed to load categorias for filter', e)
        if (mounted) {
          setAllowedRegulaciones([])
          setSelectedCategoriaName(null)
        }
      }
    })()

    return () => {
      mounted = false
    }
  }, [categoriaId])

  if (isLoading) {
    return <LoadingSkeleton count={5} />
  }

  if (error) {
    return (
      <Card className="p-8 text-center">
        <p className="text-muted-foreground">Failed to load regulations. Please try again.</p>
      </Card>
    )
  }

  let filteredRegulations = regulations || []

  // apply search query
  if (searchQuery) {
    filteredRegulations = filteredRegulations.filter(
      (reg: any) =>
        reg.nombre?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        reg.entidadEmisora?.toLowerCase().includes(searchQuery.toLowerCase()),
    )
  }

  // apply categoria filter if present
  if (allowedRegulaciones && Array.isArray(allowedRegulaciones)) {
    // match by regulation nombre or if allowedRegulaciones contains a substring
    filteredRegulations = filteredRegulations.filter((reg: any) => {
      if (!reg || !reg.nombre) return false
      return allowedRegulaciones.includes(reg.nombre) || allowedRegulaciones.some((ar) => reg.nombre?.toLowerCase().includes(String(ar).toLowerCase()))
    })
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <Input
            placeholder="Search regulations..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10"
          />
        </div>
        <div className="flex items-center gap-2">
          <Button asChild className="gap-2 glow-button">
            <Link href="/regulations/new" className="inline-flex items-center gap-2">
              <Plus className="w-4 h-4" />
              Add Regulation
            </Link>
          </Button>

          {selectedCategoriaName && (
            <div className="flex items-center gap-2">
              <span className="text-sm text-muted-foreground">Filtrado por categoría:</span>
              <Badge>{selectedCategoriaName}</Badge>
              <Button variant="ghost" size="sm" onClick={() => router.push('/regulations')}>
                Limpiar
              </Button>
            </div>
          )}
        </div>
      </div>

      <div className="grid gap-4">
        {filteredRegulations?.map((regulation: any, index: number) => (
          <motion.div
            key={regulation.id}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.05 }}
          >
            <Card className="p-6 hover:border-primary/50 transition-all duration-300 hover:shadow-glow group">
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1 space-y-3">
                  <div className="flex items-start gap-3">
                    <div className="flex-1">
                      <h3 className="text-lg font-semibold mb-1 group-hover:text-primary transition-colors">
                        {regulation.nombre}
                      </h3>
                      <p className="text-sm text-muted-foreground line-clamp-2">{regulation.contenido}</p>
                    </div>
                  </div>

                  <div className="flex flex-wrap items-center gap-3 text-sm">
                    <Badge variant="outline">{regulation.entidadEmisora}</Badge>
                    <span className="text-muted-foreground">Year: {regulation.anioEmision}</span>
                    {regulation.urlDocumento && (
                      <a
                        href={regulation.urlDocumento}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex items-center gap-1 text-primary hover:underline"
                      >
                        <ExternalLink className="w-3 h-3" />
                        View Document
                      </a>
                    )}
                  </div>
                </div>

                <div className="flex gap-2">
                  <Button asChild size="icon" variant="ghost" className="hover:text-primary">
                    <Link href={`/regulations/${regulation.id}/edit`}>
                      <Edit className="w-4 h-4" />
                    </Link>
                  </Button>
                  <Button
                    size="icon"
                    variant="ghost"
                    className="hover:text-destructive"
                    onClick={async () => {
                      if (!confirm('Delete this regulation?')) return
                      try {
                        await apiFetcher(`/api/regulaciones/${regulation.id}`, { method: 'DELETE' })
                        // revalidate list
                        mutate()
                      } catch (err) {
                        console.error('Failed to delete regulation', err)
                        alert('Failed to delete regulation')
                      }
                    }}
                  >
                    <Trash2 className="w-4 h-4" />
                  </Button>
                </div>
              </div>
            </Card>
          </motion.div>
        ))}
      </div>
    </div>
  )
}
