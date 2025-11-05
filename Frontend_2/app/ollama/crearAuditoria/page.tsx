"use client"

import { useState, useEffect } from 'react'
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
import ollamaService from '@/lib/ollamaResponseService'
import type { CrearAuditoria } from '@/types/ollama'
import empresaService from '@/lib/empresaService'
import userService from '@/lib/userService'
import type { EmpresaDTO } from '@/types/empresa'
import politicaService from '@/lib/politicaService'
import type { PoliticaDTO } from '@/types/politica'

export default function CrearAuditoriaPage() {
  const router = useRouter()
  const pathname = usePathname()
  const [empresaId, setEmpresaId] = useState<number | ''>('')
  const [tipo, setTipo] = useState('')
  const [objetivo, setObjetivo] = useState('')
  const [auditorLiderId, setAuditorLiderId] = useState<number | ''>('')
  const [politicaId, setPoliticaId] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<any | null>(null)
  const [companies, setCompanies] = useState<EmpresaDTO[]>([])
  const [admins, setAdmins] = useState<EmpresaDTO[]>([])
  const [policies, setPolicies] = useState<PoliticaDTO[]>([])

  const isFilled = (v: any) => v !== '' && v !== null && v !== undefined && !(typeof v === 'string' && v.trim() === '')

  useEffect(() => {
    ;(async () => {
      try {
        const [list, adminsList] = await Promise.all([empresaService.getEmpresasDTO(), userService.getAdminsDTO()])
        setCompanies(list)
        setAdmins(adminsList)
      } catch (_) {}
    })()
  }, [])

  useEffect(() => {
    if (!empresaId) {
      setPolicies([])
      setPoliticaId('')
      return
    }
    ;(async () => {
      try {
        const p = await politicaService.getPoliticasPorEmpresa(Number(empresaId))
        setPolicies(p)
      } catch (_) {
        setPolicies([])
      }
    })()
  }, [empresaId])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!empresaId || !tipo || !objetivo || auditorLiderId === '' || !politicaId) {
      toast({ title: 'Completa los campos', description: 'empresaId, tipo, objetivo, auditorLiderId y politicaId son requeridos' })
      return
    }
    setLoading(true)
    try {
      const payload: CrearAuditoria = {
        empresaId: Number(empresaId),
        tipo,
        objetivo,
        auditorLiderId: Number(auditorLiderId),
        politicaId,
      }
      const res = await ollamaService.crearAuditoria(payload)
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

  // autosave draft to localStorage when fields change (debounced)
  useEffect(() => {
    const key = `draft:${pathname}`
    // compute current state
    const state = {
      empresaId,
      tipo,
      objetivo,
      auditorLiderId,
      politicaId,
    }

    const serialized = JSON.stringify(state)
    let timer = window.setTimeout(() => {
      try {
        // if any field filled, save; otherwise remove
        const any = [empresaId, tipo, objetivo, auditorLiderId, politicaId].some((v) => v !== '' && v !== null && v !== undefined && !(typeof v === 'string' && v.trim() === ''))
        if (any) localStorage.setItem(key, serialized)
        else localStorage.removeItem(key)
      } catch (e) {}
    }, 400)

    return () => window.clearTimeout(timer)
  }, [empresaId, tipo, objetivo, auditorLiderId, politicaId, pathname])

  function handleRestoreDraft(data: any) {
    try {
      setEmpresaId(data.empresaId === undefined ? '' : data.empresaId)
      setTipo(data.tipo ?? '')
      setObjetivo(data.objetivo ?? '')
      setAuditorLiderId(data.auditorLiderId === undefined ? '' : data.auditorLiderId)
      setPoliticaId(data.politicaId ?? '')
    } catch (e) {}
  }

  return (
    <div className="max-w-3xl mx-auto py-12">
      <Card className="p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-medium">Crear Auditoría (IA)</h2>
          <DraftBackButton draftKey={`draft:${pathname}`} />
        </div>
        {/* show alert when at least one field contains a value */}
        <FormDirtyAlert dirty={isFilled(empresaId) || isFilled(tipo) || isFilled(objetivo) || isFilled(auditorLiderId) || isFilled(politicaId)} />
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="empresa">Empresa</Label>
            <select id="empresa" value={empresaId === '' ? '' : String(empresaId)} onChange={(e) => setEmpresaId(e.target.value ? Number(e.target.value) : '')} className="w-full border rounded px-2 py-1">
              <option value="">-- Selecciona empresa --</option>
              {companies.map((c) => (
                <option key={c.id} value={c.id}>{c.nombre} ({c.id})</option>
              ))}
            </select>
          </div>

          <div>
            <Label htmlFor="tipo">Tipo</Label>
            <Input id="tipo" value={tipo} onChange={(e) => setTipo(e.target.value)} />
          </div>

          <div>
            <Label htmlFor="objetivo">Objetivo</Label>
            <Input id="objetivo" value={objetivo} onChange={(e) => setObjetivo(e.target.value)} />
          </div>

          <div>
            <Label htmlFor="auditorLiderId">Auditor líder</Label>
            <select id="auditorLiderId" className="w-full border rounded px-2 py-1" value={auditorLiderId === '' ? '' : String(auditorLiderId)} onChange={(e) => setAuditorLiderId(e.target.value ? Number(e.target.value) : '')}>
              <option value="">-- Selecciona auditor líder --</option>
              {admins.map((a) => (
                <option key={a.id} value={String(a.id)}>
                  {a.nombre}
                </option>
              ))}
            </select>
          </div>

          <div>
            <Label htmlFor="politicaId">Política a evaluar</Label>
            <select id="politicaId" className="input" value={politicaId} onChange={(e) => setPoliticaId(e.target.value)}>
              <option value="" disabled>{policies.length === 0 ? 'Seleccione empresa primero' : 'Seleccione una política'}</option>
              {policies.map((p) => (
                <option key={p.id} value={p.id}>{p.titulo}</option>
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
