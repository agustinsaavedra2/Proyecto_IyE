"use client"

import { useEffect, useState } from 'react'
import { Card } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import BackButton from '@/components/ui/back-button'
import DraftBackButton from '@/components/ui/draft-back-button'
import RestoreDraftPrompt from '@/components/ui/restore-draft-prompt'
import { usePathname } from 'next/navigation'
import FormDirtyAlert from '@/components/ui/form-dirty-alert'
import { useRouter } from 'next/navigation'
import { toast } from '@/hooks/use-toast'
import auditoriaService from '@/lib/auditoriaService'
import type { Auditoria } from '@/types/auditoria'
import empresaService from '@/lib/empresaService'
import userService from '@/lib/userService'
import type { EmpresaDTO } from '@/types/empresa'

export default function CrearAuditoriaSimplePage() {
  const router = useRouter()
  const pathname = usePathname()
  const [empresaId, setEmpresaId] = useState<number | ''>('')
  const [tipo, setTipo] = useState('')
  const [objetivo, setObjetivo] = useState('')
  const [alcance, setAlcance] = useState('')
  const [auditorLider, setAuditorLider] = useState<number | ''>('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<Auditoria | null>(null)
  const [companies, setCompanies] = useState<EmpresaDTO[]>([])
  const [admins, setAdmins] = useState<EmpresaDTO[]>([])

  useEffect(() => {
    ;(async () => {
      try {
        const list = await empresaService.getEmpresasDTO()
        setCompanies(list)
        // do not pre-load admins globally — load auditors for the selected company below
      } catch (_) {}
    })()
  }, [])

  // when empresaId changes, fetch users filtered by role 'auditor' using the new backend endpoint
  useEffect(() => {
    if (!empresaId) {
      setAdmins([])
      return
    }

    let mounted = true
    ;(async () => {
      try {
        const list = await userService.getUsersRolDTO(Number(empresaId), 'auditor')
        if (!mounted) return
        setAdmins(list)
      } catch (e) {
        // fallback: clear list on error
        if (mounted) setAdmins([])
      }
    })()

    return () => {
      mounted = false
    }
  }, [empresaId])

  const isFilled = (v: any) => v !== '' && v !== null && v !== undefined && !(typeof v === 'string' && v.trim() === '')

  // autosave draft
  useEffect(() => {
    const key = `draft:${pathname}`
    const state = { empresaId, tipo, objetivo, alcance, auditorLider }
    const serialized = JSON.stringify(state)
    const timer = window.setTimeout(() => {
      try {
        const any = [empresaId, tipo, objetivo, alcance, auditorLider].some((v) => isFilled(v))
        if (any) localStorage.setItem(key, serialized)
        else localStorage.removeItem(key)
      } catch (e) {}
    }, 400)
    return () => window.clearTimeout(timer)
  }, [empresaId, tipo, objetivo, alcance, auditorLider, pathname])

  function handleRestoreDraft(data: any) {
    try {
      setEmpresaId(data.empresaId === undefined ? '' : data.empresaId)
      setTipo(data.tipo ?? '')
      setObjetivo(data.objetivo ?? '')
      setAlcance(data.alcance ?? '')
      setAuditorLider(data.auditorLider === undefined ? '' : data.auditorLider)
    } catch (e) {}
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!empresaId || !tipo) {
      toast({ title: 'Campos faltantes', description: 'empresaId y tipo son requeridos' })
      return
    }
    setLoading(true)
    try {
      const payload: Auditoria = {
        empresaId: Number(empresaId),
        tipo,
        objetivo,
        alcance,
        auditorLider: auditorLider === '' ? undefined : Number(auditorLider),
      }
      const res = await auditoriaService.createAuditoria(payload)
      // clear draft on success
      try { localStorage.removeItem(`draft:${pathname}`) } catch (e) {}
      setResult(res)
      toast({ title: 'Auditoría creada', description: `ID: ${res.id}` })
    } catch (err: any) {
      toast({ title: 'Error', description: err?.message || 'Error al crear auditoría' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-2xl mx-auto py-12">
      <Card className="p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-medium">Crear Auditoría</h2>
          <DraftBackButton draftKey={`draft:${pathname}`} />
        </div>
        <FormDirtyAlert dirty={isFilled(empresaId) || isFilled(tipo) || isFilled(objetivo) || isFilled(alcance) || isFilled(auditorLider)} />
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
            <Label htmlFor="tipo">Tipo</Label>
            <select
              id="tipo"
              className="input"
              value={tipo}
              onChange={(e) => setTipo(e.target.value)}
              required
            >
              <option value="" disabled>
                Seleccione un tipo
              </option>
              <option value="interno">Interno</option>
              <option value="externo">Externo</option>
              <option value="regulatorio">Regulatorio</option>
            </select>
          </div>

          <div>
            <Label htmlFor="objetivo">Objetivo</Label>
            <Input id="objetivo" value={objetivo} onChange={(e) => setObjetivo(e.target.value)} />
          </div>

          <div>
            <Label htmlFor="alcance">Alcance</Label>
            <Input id="alcance" value={alcance} onChange={(e) => setAlcance(e.target.value)} />
          </div>

          <div>
            <Label htmlFor="auditorLider">Auditor líder</Label>
            <select id="auditorLider" className="input" value={auditorLider === '' ? '' : String(auditorLider)} onChange={(e) => setAuditorLider(e.target.value ? Number(e.target.value) : '')}>
              <option value="" disabled>
                {admins.length === 0 ? 'No hay admins disponibles' : 'Seleccione un auditor líder'}
              </option>
              {admins.map((a) => (
                <option key={a.id} value={String(a.id)}>
                  {a.nombre}
                </option>
              ))}
            </select>
          </div>

          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Creando...' : 'Crear Auditoría'}
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
