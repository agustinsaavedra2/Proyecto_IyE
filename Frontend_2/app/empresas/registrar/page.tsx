"use client"

import { useEffect, useState } from 'react'
import { Card } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import DraftBackButton from '@/components/ui/draft-back-button'
import RestoreDraftPrompt from '@/components/ui/restore-draft-prompt'
import { usePathname } from 'next/navigation'
import FormDirtyAlert from '@/components/ui/form-dirty-alert'
import { useRouter } from 'next/navigation'
import empresaService from '@/lib/empresaService'
import userService from '@/lib/userService'
import categoriaService from '@/lib/categoriaService'
import type { CategoriaDTO } from '@/types/categoria'
import type { EmpresaDTO } from '@/types/empresa'

export default function RegistrarEmpresaPage() {
  const router = useRouter()
  const pathname = usePathname()
  const [loading, setLoading] = useState(false)
  const [admins, setAdmins] = useState<EmpresaDTO[]>([])
  const [categorias, setCategorias] = useState<CategoriaDTO[]>([])

  const [admin, setAdmin] = useState<number | ''>('')
  const [categoriaId, setCategoriaId] = useState<number | ''>('')
  const [nombre, setNombre] = useState('')
  const [codigoEmpresa, setCodigoEmpresa] = useState('')
  const [ubicacion, setUbicacion] = useState('')
  const [descripcion, setDescripcion] = useState('')

  useEffect(() => {
    ;(async () => {
      try {
        const adminsList = await userService.getAdminsDTO()
        setAdmins(adminsList || [])
        const cats = await categoriaService.listarCategoriasDTO()
        setCategorias(cats || [])
      } catch (e) {
        console.error('Failed to load admins', e)
      }
    })()
  }, [])

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    try {
      const payload = {
        admin: admin === '' ? undefined : Number(admin),
        categoriaId: categoriaId === '' ? undefined : Number(categoriaId),
        nombre,
        codigoEmpresa,
        ubicacion,
        descripcion,
      }

      await empresaService.crearEmpresa(payload as any)
      try { localStorage.removeItem(`draft:${pathname}`) } catch (e) {}
      router.push('/empresas')
    } catch (err) {
      console.error('Failed to create empresa', err)
      alert('Error al crear empresa')
    } finally {
      setLoading(false)
    }
  }

  // autosave
  useEffect(() => {
    const key = `draft:${pathname}`
    const timer = window.setTimeout(() => {
      try {
        const any = Boolean(admin || categoriaId || nombre || codigoEmpresa || ubicacion || descripcion)
        if (any) localStorage.setItem(key, JSON.stringify({ admin, categoriaId, nombre, codigoEmpresa, ubicacion, descripcion }))
        else localStorage.removeItem(key)
      } catch (e) {}
    }, 400)
    return () => window.clearTimeout(timer)
  }, [admin, categoriaId, nombre, codigoEmpresa, ubicacion, descripcion, pathname])

  function handleRestoreDraft(data: any) {
    try {
      setAdmin(data.admin === undefined ? '' : data.admin)
      setCategoriaId(data.categoriaId === undefined ? '' : data.categoriaId)
      setNombre(data.nombre ?? '')
      setCodigoEmpresa(data.codigoEmpresa ?? '')
      setUbicacion(data.ubicacion ?? '')
      setDescripcion(data.descripcion ?? '')
    } catch (e) {}
  }

  return (
    <div className="min-h-screen bg-background">
      <main className="container mx-auto px-4 py-8">
        <Card className="p-6 max-w-2xl mx-auto">
          <div className="flex justify-between items-center mb-4">
            <h1 className="text-2xl font-bold">Registrar Empresa</h1>
            <DraftBackButton draftKey={`draft:${pathname}`} />
          </div>

          <FormDirtyAlert dirty={Boolean(admin || categoriaId || nombre || codigoEmpresa || ubicacion || descripcion)} />

          <form onSubmit={onSubmit} className="space-y-4">
            <div>
              <Label>Admin (usuario)</Label>
              <select className="input w-full" value={admin === '' ? '' : String(admin)} onChange={(e) => setAdmin(e.target.value ? Number(e.target.value) : '')}>
                <option value="">Seleccione un admin</option>
                {admins.map((a) => (
                  <option key={a.id} value={String(a.id)}>
                    {a.nombre}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <Label>Categoria</Label>
              <select className="input w-full" value={categoriaId === '' ? '' : String(categoriaId)} onChange={(e) => setCategoriaId(e.target.value ? Number(e.target.value) : '')}>
                <option value="">Seleccione una categoría</option>
                {categorias.map((c) => (
                  <option key={c.id} value={String(c.id)}>
                    {c.nombre}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <Label>Nombre</Label>
              <Input value={nombre} onChange={(e) => setNombre(e.target.value)} required />
            </div>

            <div>
              <Label>Codigo Empresa</Label>
              <Input value={codigoEmpresa} onChange={(e) => setCodigoEmpresa(e.target.value)} />
            </div>

            <div>
              <Label>Ubicacion</Label>
              <Input value={ubicacion} onChange={(e) => setUbicacion(e.target.value)} />
            </div>

            <div>
              <Label>Descripcion</Label>
              <Textarea value={descripcion} onChange={(e) => setDescripcion(e.target.value)} />
            </div>

            <div className="flex justify-end">
              <Button type="submit" disabled={loading}>{loading ? 'Registrando...' : 'Registrar Empresa'}</Button>
            </div>
          </form>
        </Card>
      </main>
    </div>
  )
}