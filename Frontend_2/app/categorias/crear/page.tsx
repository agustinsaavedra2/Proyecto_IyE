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
import type { CategoriaIndustria } from '@/types/categoria'

export default function CrearCategoriaPage() {
  const router = useRouter()
  const pathname = usePathname()
  const [nombre, setNombre] = useState('')
  const [descripcion, setDescripcion] = useState('')
  const [regulaciones, setRegulaciones] = useState('') // comma separated
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!nombre) {
      toast({ title: 'Completa el nombre', description: 'El nombre es requerido' })
      return
    }
    setLoading(true)
    try {
      const payload: CategoriaIndustria = {
        nombre,
        descripcion,
        regulaciones: regulaciones.split(',').map((s) => s.trim()).filter(Boolean),
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
        if (isFilled(nombre) || isFilled(descripcion) || isFilled(regulaciones)) {
          localStorage.setItem(key, JSON.stringify({ nombre, descripcion, regulaciones }))
        } else {
          localStorage.removeItem(key)
        }
      } catch (e) {}
    }, 400)
    return () => window.clearTimeout(timer)
  }, [nombre, descripcion, regulaciones, pathname])

  function handleRestoreDraft(data: any) {
    try {
      setNombre(data.nombre ?? '')
      setDescripcion(data.descripcion ?? '')
      setRegulaciones(data.regulaciones ?? '')
    } catch (e) {}
  }

  return (
    <div className="max-w-2xl mx-auto py-12">
      <Card className="p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-medium">Crear Categoría de Industria</h2>
          <DraftBackButton draftKey={`draft:${pathname}`} />
        </div>
        <FormDirtyAlert dirty={isFilled(nombre) || isFilled(descripcion) || isFilled(regulaciones)} />
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
            <Label htmlFor="regulaciones">Regulaciones (coma separadas)</Label>
            <Input id="regulaciones" value={regulaciones} onChange={(e) => setRegulaciones(e.target.value)} placeholder="reg1,reg2" />
          </div>

          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Creando...' : 'Crear Categoría'}
          </Button>
        </form>

        <div className="mt-6 p-4 bg-muted/50 rounded">
          <h3 className="font-medium mb-2">Nota técnica breve</h3>
          <p className="text-sm">
            El draft key por defecto es <code>{'draft:${pathname}'}</code>. Si quieres compartir drafts entre rutas, pásale explícitamente la misma
            <code className="mx-1">draftKey</code> al <code>&lt;DraftBackButton draftKey=&quot;...&quot; /&gt;</code> y usa la misma clave en el
            effect que guarda el draft.
          </p>
          <p className="text-sm mt-2">
            El guardado usa <code>localStorage</code> — funciona por navegador/usuario. Si prefieres sincronización en servidor o en
            <code className="mx-1">sessionStorage</code> lo adaptamos.
          </p>
        </div>
      </Card>
    </div>
  )
}
