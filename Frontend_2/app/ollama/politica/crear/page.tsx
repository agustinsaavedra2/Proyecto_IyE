"use client"

import { useEffect, useState } from 'react'
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
import politicaService from '@/lib/politicaService'
import type { CrearPPP } from '@/types/protocolo'
import empresaService from '@/lib/empresaService'
import type { EmpresaDTO } from '@/types/empresa'
import userService from '@/lib/userService'
import type { UsuarioDTO } from '@/types/auth'

export default function CrearPoliticaPage() {
  const router = useRouter()
  const pathname = usePathname()
  const [empresaId, setEmpresaId] = useState<number | ''>('')
  const [usuarioId, setUsuarioId] = useState<number | ''>('')
  const [pregunta, setPregunta] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<any | null>(null)
  const [companies, setCompanies] = useState<EmpresaDTO[]>([])
  const [users, setUsers] = useState<EmpresaDTO[]>([])

  const isFilled = (v: any) => v !== '' && v !== null && v !== undefined && !(typeof v === 'string' && v.trim() === '')

  useEffect(() => {
    ;(async () => {
      try {
        const list = await empresaService.getEmpresasDTO()
        setCompanies(list)
      } catch (_) {}
    })()
  }, [])

  useEffect(() => {
    if (!empresaId) {
      setUsers([])
      setUsuarioId('')
      return
    }
    ;(async () => {
      try {
        const u = await userService.getUsuariosEmpresaDTO(Number(empresaId))
        setUsers(u)
      } catch (_) {
        setUsers([])
      }
    })()
  }, [empresaId])

  function handleRestoreDraft(data: any) {
    try {
      setEmpresaId(data.empresaId === undefined ? '' : data.empresaId)
      setUsuarioId(data.usuarioId === undefined ? '' : data.usuarioId)
      setPregunta(data.pregunta === undefined ? '' : data.pregunta)
    } catch (e) {}
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!empresaId || !usuarioId || !pregunta) {
      toast({ title: 'Completa los campos', description: 'empresaId, usuarioId y pregunta son requeridos' })
      return
    }
    setLoading(true)
    try {
      const payload: CrearPPP = {
        empresaId: Number(empresaId),
        usuarioId: Number(usuarioId),
        politicaId: '',
        pregunta,
      }
      const res = await politicaService.crearPolitica(payload)
  try { localStorage.removeItem(`draft:${pathname}`) } catch (e) {}
  setResult(res)
      toast({ title: 'Política creada', description: `ID: ${res.id}` })
    } catch (err: any) {
      toast({ title: 'Error', description: err?.message || 'Error al crear política' })
    } finally {
      setLoading(false)
    }
  }

  // autosave draft
  useEffect(() => {
    const key = `draft:${pathname}`
    const state = { empresaId, usuarioId, pregunta }
    const serialized = JSON.stringify(state)
    const timer = window.setTimeout(() => {
      try {
        const any = [empresaId, usuarioId, pregunta].some((v) => isFilled(v))
        if (any) localStorage.setItem(key, serialized)
        else localStorage.removeItem(key)
      } catch (e) {}
    }, 400)
    return () => window.clearTimeout(timer)
  }, [empresaId, usuarioId, pregunta, pathname])

  return (
    <div className="max-w-2xl mx-auto py-12">
      <Card className="p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-medium">Crear Política</h2>
          <DraftBackButton draftKey={`draft:${pathname}`} />
        </div>
        <FormDirtyAlert dirty={isFilled(empresaId) || isFilled(usuarioId) || isFilled(pregunta)} />
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="empresaId">Empresa</Label>
            <select
              id="empresaId"
              className="input"
              value={empresaId === '' ? '' : String(empresaId)}
              onChange={(e) => setEmpresaId(e.target.value ? Number(e.target.value) : '')}
              required
            >
              <option value="" disabled>
                {companies.length === 0 ? 'No hay empresas disponibles' : 'Seleccione una empresa'}
              </option>
              {companies.map((c) => (
                <option key={c.id} value={String(c.id)}>
                  {c.nombre}
                </option>
              ))}
            </select>
          </div>

          <div>
            <Label htmlFor="usuarioId">Usuario</Label>
            <select id="usuarioId" className="input" value={usuarioId === '' ? '' : String(usuarioId)} onChange={(e) => setUsuarioId(e.target.value ? Number(e.target.value) : '')}>
              <option value="" disabled>{users.length === 0 ? 'Seleccione empresa primero' : 'Seleccione un usuario'}</option>
              {users.map((u) => (
                <option key={u.id} value={String(u.id)}>
                  {u.nombre}
                </option>
              ))}
            </select>
          </div>

          <div>
            <Label htmlFor="pregunta">Pregunta</Label>
            <Input id="pregunta" value={pregunta} onChange={(e) => setPregunta(e.target.value)} />
          </div>

          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Creando...' : 'Crear Política'}
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
