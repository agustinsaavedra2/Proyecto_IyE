"use client"

import { useState, useEffect } from 'react'
import { Card } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import DraftBackButton from '@/components/ui/draft-back-button'
import RestoreDraftPrompt from '@/components/ui/restore-draft-prompt'
import { usePathname } from 'next/navigation'
import FormDirtyAlert from '@/components/ui/form-dirty-alert'
import { useRouter } from 'next/navigation'
import { toast } from '@/hooks/use-toast'
import categoriaService from '@/lib/categoriaService'
import regulationService from '@/lib/regulationService'
import type { Regulacion } from '@/types/regulacion'
import type { CategoriaIndustria } from '@/types/categoria'

export default function CrearCategoriaPage() {
  const router = useRouter()
  const pathname = usePathname()
  const [nombre, setNombre] = useState('')
  const [descripcion, setDescripcion] = useState('')
  const [selectedRegIds, setSelectedRegIds] = useState<string[]>([])
  const [availableRegs, setAvailableRegs] = useState<Regulacion[]>([])
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!nombre) {
      toast({ title: 'Completa el nombre', description: 'El nombre es requerido' })
      return
    }
    setLoading(true)
    try {
      const regsToSend = await Promise.all(
        selectedRegIds.map(async (id) => {
          const found = availableRegs.find((r) => String(r.id) === String(id))
          if (found) return JSON.stringify(found)
          try {
            const fetched = await regulationService.getRegulationById(id)
            return fetched ? JSON.stringify(fetched) : ''
          } catch (e) {
            return ''
          }
        }),
      )

      const payload: CategoriaIndustria = {
        nombre,
        descripcion,
        regulaciones: regsToSend.filter(Boolean) as string[],
      }
    const res = await categoriaService.crearCategoria(payload)
    try { localStorage.removeItem(`draft:${pathname}`) } catch (e) {}
    toast({ title: 'Categoría creada', description: `ID: ${res?.id ?? 'n/a'}` })
    } catch (err: any) {
      toast({ title: 'Error', description: err?.message || 'Error al crear categoría' })
    } finally {
      setLoading(false)
    }
  }

  const isFilled = (v: any) => v !== '' && v !== null && v !== undefined && !(typeof v === 'string' && v.trim() === '')

  // autosave draft to localStorage (per-path)
  useEffect(() => {
    const key = `draft:${pathname}`
    const timer = window.setTimeout(() => {
      try {
        if (isFilled(nombre) || isFilled(descripcion) || (selectedRegIds && selectedRegIds.length > 0)) {
          localStorage.setItem(key, JSON.stringify({ nombre, descripcion, regulaciones: selectedRegIds }))
        } else {
          localStorage.removeItem(key)
        }
      } catch (e) {}
    }, 400)
    return () => window.clearTimeout(timer)
  }, [nombre, descripcion, selectedRegIds, pathname])

  useEffect(() => {
    // load available regulations to show as checkboxes
    let mounted = true
    ;(async () => {
      try {
        const list = await regulationService.getRegulations()
        if (!mounted) return
        setAvailableRegs(list)
      } catch (e) {
        if (!mounted) return
        setAvailableRegs([])
      }
    })()
    return () => {
      mounted = false
    }
  }, [])

  function handleRestoreDraft(data: any) {
    try {
      setNombre(data.nombre ?? '')
      setDescripcion(data.descripcion ?? '')
      // ensure we set an array
  setSelectedRegIds(Array.isArray(data.regulaciones) ? data.regulaciones : [])
    } catch (e) {}
  }

  return (
    <div className="max-w-2xl mx-auto py-12">
      <Card className="p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-medium">Crear Categoría de Industria</h2>
          <DraftBackButton draftKey={`draft:${pathname}`} />
        </div>
  <FormDirtyAlert dirty={isFilled(nombre) || isFilled(descripcion) || (selectedRegIds && selectedRegIds.length > 0)} />
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="nombre">Nombre</Label>
            <Input id="nombre" value={nombre} onChange={(e) => setNombre(e.target.value)} />
          </div>

          <div>
            <Label htmlFor="descripcion">Descripción</Label>
            <Input id="descripcion" value={descripcion} onChange={(e) => setDescripcion(e.target.value)} />
          </div>

          <div>
            <Label>Regulaciones</Label>
            <div className="grid grid-cols-1 gap-2 mt-2">
              {availableRegs.length === 0 && <p className="text-sm text-muted-foreground">No hay regulaciones disponibles</p>}
              {availableRegs.map((r) => {
                const label = `${r.nombre}${r.anioEmision ? ` (${r.anioEmision})` : ''}`
                const checked = selectedRegIds.includes(String(r.id ?? ''))
                return (
                  <label key={r.id ?? label} className="inline-flex items-center space-x-2">
                    <input
                      type="checkbox"
                      checked={checked}
                      onChange={(e) => {
                        const id = String(r.id ?? '')
                        if (e.target.checked) setSelectedRegIds((s) => Array.from(new Set([...s, id])))
                        else setSelectedRegIds((s) => s.filter((x) => x !== id))
                      }}
                    />
                    <span className="text-sm">{label}</span>
                  </label>
                )
              })}
            </div>
          </div>

          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Creando...' : 'Crear Categoría'}
          </Button>
        </form>
      </Card>
    </div>
  )
}
