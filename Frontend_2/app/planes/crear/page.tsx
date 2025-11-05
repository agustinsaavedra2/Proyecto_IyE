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
import planService from '@/lib/planService'
import type { Plan } from '@/types/plan'

export default function CrearPlanPage() {
  const router = useRouter()
  const pathname = usePathname()
  const [nombre, setNombre] = useState('')
  const [precio, setPrecio] = useState<number | ''>('')
  const [maxUsuarios, setMaxUsuarios] = useState<number | ''>('')
  const [duracionMeses, setDuracionMeses] = useState<number | ''>('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<Plan | null>(null)


  const isFilled = (v: any) => v !== '' && v !== null && v !== undefined && !(typeof v === 'string' && v.trim() === '')

  // autosave draft to localStorage (per-path)
  useEffect(() => {
    const key = `draft:${pathname}`
    const state = { nombre, precio, maxUsuarios, duracionMeses }
    const serialized = JSON.stringify(state)
    const timer = window.setTimeout(() => {
      try {
        const any = [nombre, precio, maxUsuarios, duracionMeses].some((v) => isFilled(v))
        if (any) localStorage.setItem(key, serialized)
        else localStorage.removeItem(key)
      } catch (e) {}
    }, 400)
    return () => window.clearTimeout(timer)
  }, [nombre, precio, maxUsuarios, duracionMeses, pathname])

  function handleRestoreDraft(data: any) {
    try {
      setNombre(data.nombre ?? '')
      setPrecio(data.precio === undefined ? '' : data.precio)
      setMaxUsuarios(data.maxUsuarios === undefined ? '' : data.maxUsuarios)
      setDuracionMeses(data.duracionMeses === undefined ? '' : data.duracionMeses)
    } catch (e) {}
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!nombre || precio === '' || maxUsuarios === '' || duracionMeses === '') {
      toast({ title: 'Completa los campos', description: 'Todos los campos son requeridos' })
      return
    }
    setLoading(true)
    try {
      const payload: Plan = {
        nombre,
        precio: Number(precio),
        maxUsuarios: Number(maxUsuarios),
        duracionMeses: Number(duracionMeses),
      }
      const res = await planService.crearPlan(payload)
  try { localStorage.removeItem(`draft:${pathname}`) } catch (e) {}
  setResult(res)
      toast({ title: 'Plan creado', description: `ID: ${res.id}` })
    } catch (err: any) {
      toast({ title: 'Error', description: err?.message || 'Error al crear plan' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-2xl mx-auto py-12">
      <Card className="p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-medium">Crear Plan</h2>
          <DraftBackButton draftKey={`draft:${pathname}`} />
        </div>
        <FormDirtyAlert dirty={isFilled(nombre) || isFilled(precio) || isFilled(maxUsuarios) || isFilled(duracionMeses)} />
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="nombre">Nombre</Label>
            <Input id="nombre" value={nombre} onChange={(e) => setNombre(e.target.value)} />
          </div>

          <div>
            <Label htmlFor="precio">Precio</Label>
            <Input id="precio" type="number" value={precio === '' ? '' : String(precio)} onChange={(e) => setPrecio(e.target.value ? Number(e.target.value) : '')} />
          </div>

          <div>
            <Label htmlFor="maxUsuarios">Max Usuarios</Label>
            <Input id="maxUsuarios" type="number" value={maxUsuarios === '' ? '' : String(maxUsuarios)} onChange={(e) => setMaxUsuarios(e.target.value ? Number(e.target.value) : '')} />
          </div>

          <div>
            <Label htmlFor="duracionMeses">Duración (meses)</Label>
            <Input id="duracionMeses" type="number" value={duracionMeses === '' ? '' : String(duracionMeses)} onChange={(e) => setDuracionMeses(e.target.value ? Number(e.target.value) : '')} />
          </div>

          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Creando...' : 'Crear Plan'}
          </Button>
        </form>

        {result && (
          <div className="mt-4">
            <h3 className="font-medium">Resultado</h3>
            <pre className="text-sm mt-2">{JSON.stringify(result, null, 2)}</pre>
          </div>
        )}
      </Card>
    </div>
  )
}
