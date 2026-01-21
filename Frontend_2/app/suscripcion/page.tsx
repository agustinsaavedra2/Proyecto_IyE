"use client"

import { useEffect, useState } from 'react'
import { Card } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import DraftBackButton from '@/components/ui/draft-back-button'
import RestoreDraftPrompt from '@/components/ui/restore-draft-prompt'
import { usePathname } from 'next/navigation'
import FormDirtyAlert from '@/components/ui/form-dirty-alert'
import { toast } from '@/hooks/use-toast'
import suscripcionService from '@/lib/suscripcionService'
import empresaService from '@/lib/empresaService'
import userService from '@/lib/userService'
import type { EmpresaDTO } from '@/types/empresa'

export default function SuscripcionPage() {
  const [empresaId, setEmpresaId] = useState<number | ''>('')
  const [plan, setPlan] = useState('basic')
  const [adminId, setAdminId] = useState<number | ''>('')
  const [loading, setLoading] = useState(false)
  const [companies, setCompanies] = useState<EmpresaDTO[]>([])
  const [admins, setAdmins] = useState<EmpresaDTO[]>([])
  const pathname = usePathname()

  const isFilled = (v: any) => v !== '' && v !== null && v !== undefined && !(typeof v === 'string' && v.trim() === '')

  useEffect(() => {
    (async () => {
      try {
        const [list, adminsList] = await Promise.all([empresaService.getEmpresasDTO(), userService.getAdminsDTO()])
        setCompanies(list)
        setAdmins(adminsList)
      } catch (e) {
        // ignore
      }
    })()
  }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!empresaId || !adminId) {
      toast({ title: 'Completa los campos', description: 'Empresa y admin son requeridos' })
      return
    }
    setLoading(true)
    try {
      const payload = { empresaId: Number(empresaId), plan, adminId: Number(adminId) }
      const res = await suscripcionService.suscribirse(payload)
      try { localStorage.removeItem(`draft:${pathname}`) } catch (e) {}
      toast({ title: 'Suscripción creada', description: `Plan: ${res.plan}` })
    } catch (err: any) {
      toast({ title: 'Error', description: err?.message || 'Error al suscribirse' })
    } finally {
      setLoading(false)
    }
  }

  // autosave draft
  useEffect(() => {
    const key = `draft:${pathname}`
    const state = { empresaId, plan, adminId }
    const serialized = JSON.stringify(state)
    const timer = window.setTimeout(() => {
      try {
        const any = [empresaId, plan, adminId].some((v) => isFilled(v))
        if (any) localStorage.setItem(key, serialized)
        else localStorage.removeItem(key)
      } catch (e) {}
    }, 400)
    return () => window.clearTimeout(timer)
  }, [empresaId, plan, adminId, pathname])

  function handleRestoreDraft(data: any) {
    try {
      setEmpresaId(data.empresaId === undefined ? '' : data.empresaId)
      setPlan(data.plan ?? 'basic')
      setAdminId(data.adminId === undefined ? '' : data.adminId)
    } catch (e) {}
  }

  return (
    <div className="max-w-2xl mx-auto py-12">
      <Card className="p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-medium">Suscribirse</h2>
          <DraftBackButton draftKey={`draft:${pathname}`} />
        </div>
        <FormDirtyAlert dirty={isFilled(empresaId) || isFilled(plan) || isFilled(adminId)} />
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="empresa">Empresa</Label>
            <select id="empresa" className="input w-full" value={empresaId === '' ? '' : String(empresaId)} onChange={(e) => setEmpresaId(e.target.value ? Number(e.target.value) : '')}>
              <option value="">Seleccione una empresa</option>
              {companies.map((c) => (
                <option key={c.id} value={String(c.id)}>
                  {c.nombre}
                </option>
              ))}
            </select>
          </div>

          <div>
            <Label htmlFor="plan">Plan</Label>
            <select id="plan" className="input w-full" value={plan} onChange={(e) => setPlan(e.target.value)}>
              <option value="basic">basic</option>
              <option value="pro">pro</option>
              <option value="enterprise">enterprise</option>
            </select>
          </div>

          <div>
            <Label htmlFor="adminId">Admin</Label>
            <select id="adminId" className="input w-full" value={adminId === '' ? '' : String(adminId)} onChange={(e) => setAdminId(e.target.value ? Number(e.target.value) : '')}>
              <option value="">Seleccione un admin</option>
              {admins.map((a) => (
                <option key={a.id} value={String(a.id)}>
                  {a.nombre}
                </option>
              ))}
            </select>
          </div>

          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Procesando...' : 'Suscribirse'}
          </Button>
        </form>
      </Card>
    </div>
  )
}
