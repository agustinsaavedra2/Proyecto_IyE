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
import protocoloService from '@/lib/protocoloService'
import { toast } from '@/hooks/use-toast'
import empresaService from '@/lib/empresaService'
import type { EmpresaDTO } from '@/types/empresa'
import userService from '@/lib/userService'
import politicaService from '@/lib/politicaService'
import type { PoliticaDTO } from '@/types/politica'

export default function CrearProtocoloPage() {
  const router = useRouter()
  const [empresaId, setEmpresaId] = useState<number | ''>('')
  const [usuarioId, setUsuarioId] = useState<number | ''>('')
  const [politicaId, setPoliticaId] = useState('')
  const [pregunta, setPregunta] = useState('')
  const [protocoloId, setProtocoloId] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<any | null>(null)
  const [companies, setCompanies] = useState<EmpresaDTO[]>([])
  const [users, setUsers] = useState<EmpresaDTO[]>([])
  const [policies, setPolicies] = useState<PoliticaDTO[]>([])
  const pathname = usePathname()

  const isFilled = (v: any) => v !== '' && v !== null && v !== undefined && !(typeof v === 'string' && v.trim() === '')

  useEffect(() => {
    ;(async () => {
      try {
        const list = await empresaService.getEmpresasDTO()
        setCompanies(list)
      } catch (_) {
        // ignore
      }
    })()
  }, [])

  useEffect(() => {
    if (!empresaId) {
      setUsers([])
      setPolicies([])
      setUsuarioId('')
      setPoliticaId('')
      return
    }
    ;(async () => {
      try {
  const [u, p] = await Promise.all([userService.getUsuariosEmpresaDTO(Number(empresaId)), politicaService.getPoliticasPorEmpresa(Number(empresaId))])
        setUsers(u)
        setPolicies(p)
      } catch (_) {
        setUsers([])
        setPolicies([])
      }
    })()
  }, [empresaId])

  function handleRestoreDraft(data: any) {
    // populate fields from draft (defensive conversions)
    try {
      setEmpresaId(data.empresaId === undefined ? '' : data.empresaId)
      setUsuarioId(data.usuarioId === undefined ? '' : data.usuarioId)
      setPoliticaId(data.politicaId === undefined ? '' : data.politicaId)
      setPregunta(data.pregunta === undefined ? '' : data.pregunta)
      setProtocoloId(data.protocoloId === undefined ? '' : data.protocoloId)
    } catch (e) {
      // ignore malformed draft
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!empresaId || !usuarioId || !politicaId || !pregunta) {
      toast({ title: 'Completa los campos', description: 'empresaId, usuarioId, politicaId y pregunta son requeridos' })
      return
    }
    setLoading(true)
    try {
      const payload = {
        empresaId: Number(empresaId),
        usuarioId: Number(usuarioId),
        politicaId,
        pregunta,
        protocoloId: protocoloId || undefined,
      }
      const res = await protocoloService.crearProtocolo(payload)
      try { localStorage.removeItem(`draft:${pathname}`) } catch (e) {}
      setResult(res)
      toast({ title: 'Protocolo creado', description: `ID: ${res.id}` })
    } catch (err: any) {
      toast({ title: 'Error', description: err?.message || 'Error al crear protocolo' })
    } finally {
      setLoading(false)
    }
  }

    // autosave draft
  useEffect(() => {
    const key = `draft:${pathname}`
    const state = { empresaId, usuarioId, politicaId, pregunta, protocoloId }
    const serialized = JSON.stringify(state)
    const timer = window.setTimeout(() => {
      try {
        const any = [empresaId, usuarioId, politicaId, pregunta, protocoloId].some((v) => isFilled(v))
        if (any) localStorage.setItem(key, serialized)
        else localStorage.removeItem(key)
      } catch (e) {}
    }, 400)
    return () => window.clearTimeout(timer)
  }, [empresaId, usuarioId, politicaId, pregunta, protocoloId, pathname])

  return (
    <div className="max-w-2xl mx-auto py-12">
      <Card className="p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-medium">Crear Protocolo</h2>
          <DraftBackButton draftKey={`draft:${pathname}`} />
        </div>
  <RestoreDraftPrompt draftKey={`draft:${pathname}`} onRestore={handleRestoreDraft} />

  <FormDirtyAlert dirty={isFilled(empresaId) || isFilled(usuarioId) || isFilled(politicaId) || isFilled(pregunta) || isFilled(protocoloId)} />
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
            <Label htmlFor="politicaId">Política</Label>
            <select id="politicaId" className="input" value={politicaId} onChange={(e) => setPoliticaId(e.target.value)}>
              <option value="" disabled>{policies.length === 0 ? (empresaId ? 'No hay políticas para esta empresa' : 'Seleccione empresa primero') : 'Seleccione una política'}</option>
              {policies.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.titulo}
                </option>
              ))}
            </select>
          </div>

          <div>
            <Label htmlFor="pregunta">Pregunta</Label>
            <Input id="pregunta" value={pregunta} onChange={(e) => setPregunta(e.target.value)} />
          </div>

          <div>
            <Label htmlFor="protocoloId">Protocolo ID (opcional)</Label>
            <Input id="protocoloId" value={protocoloId} onChange={(e) => setProtocoloId(e.target.value)} />
          </div>

          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Creando...' : 'Crear Protocolo'}
          </Button>
        </form>

        {result && (
          <pre className="mt-4 bg-muted p-3 rounded">{JSON.stringify(result, null, 2)}</pre>
        )}
      </Card>
    </div>
  )
}