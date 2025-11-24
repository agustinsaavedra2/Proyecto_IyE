"use client"

import type React from "react"

import { useEffect, useState } from "react"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Loader2 } from "lucide-react"
import { requestRegister } from "@/lib/userService"
import { ApiError } from '@/lib/apis'
import { toast } from '@/hooks/use-toast'
import type { RegisterAdminDTO, RegisterUserDTO } from '@/types/auth'
import { EmpresaDTO } from "@/types/empresa"
import empresaService from "@/lib/empresaService"
import userService from '@/lib/userService'

export function RegisterForm() {
  const [loading, setLoading] = useState(false)
  const [formType, setFormType] = useState<'admin' | 'user'>('admin')
  const [companies, setCompanies] = useState<EmpresaDTO[]>([])
  const [admins, setAdmins] = useState<EmpresaDTO[]>([])

  useEffect(() => {
    (async () => {
      try {
        const [list, adminsList] = await Promise.all([empresaService.getEmpresasDTO(), userService.getAdminsDTO()])
        setCompanies(list)
        setAdmins(adminsList)
      } catch (_) {}
    })()
  }, [])

  const [formAdmin, setFormAdmin] = useState<RegisterAdminDTO>({
    nombre: '',
    email: '',
    password: '',
  })

  const [formUser, setFormUser] = useState<RegisterUserDTO>({
    empresaId: 0,
    nombre: '',
    email: '',
    password: '',
    rol: 'viewer',
    adminId: undefined,
  })

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setLoading(true)
    try {
      // Use the magic-link request endpoint so backend sends verification email
      const dto = formType === 'admin'
        ? {
            tipo: 'admin',
            nombre: formAdmin.nombre,
            email: formAdmin.email,
            password: formAdmin.password,
          }
        : {
            tipo: 'user',
            empresaId: Number(formUser.empresaId),
            nombre: formUser.nombre,
            email: formUser.email,
            password: formUser.password,
            rol: formUser.rol,
            adminId: formUser.adminId ? Number(formUser.adminId) : undefined,
          }

      // Send through the central request-register endpoint for both admin and user.
      // The backend will handle logic based on dto.tipo.
      await requestRegister(dto as any)
      // UX: do not reveal whether the email existed
      toast({
        title: 'Revisa tu correo',
        description: 'Si el correo existe, enviaremos un enlace de verificación. Revisa tu carpeta de spam.',
      })
    } catch (err: any) {
      // apiFetcher throws ApiError with status and body
      console.error('Registration failed:', err)
      if (err instanceof ApiError) {
        // show a friendly message; prefer server-provided message if available
        const body = err.body
  const message = typeof body === 'string' ? body : (body as any)?.message || `Error del servidor (${err.status})`
        toast({ title: 'Registro fallido', description: message })
      } else {
        toast({ title: 'Registro fallido', description: String(err?.message || err) })
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card className="p-8">
      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="flex gap-4 items-center">
          <Label htmlFor="typeAdmin">Registro como</Label>
          <div className="flex items-center gap-2">
            <label className="flex items-center gap-2">
              <input
                type="radio"
                name="registerType"
                checked={formType === 'admin'}
                onChange={() => setFormType('admin')}
              />
              Admin
            </label>
            <label className="flex items-center gap-2">
              <input
                type="radio"
                name="registerType"
                checked={formType === 'user'}
                onChange={() => setFormType('user')}
              />
              User
            </label>
          </div>
        </div>

        {formType === 'admin' ? (
          <>
            <div className="space-y-2">
              <Label htmlFor="nombre">Full Name</Label>
              <Input
                id="nombre"
                placeholder="John Doe"
                value={formAdmin.nombre}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFormAdmin({ ...formAdmin, nombre: e.target.value })}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                placeholder="john@example.com"
                value={formAdmin.email}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFormAdmin({ ...formAdmin, email: e.target.value })}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                value={formAdmin.password}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFormAdmin({ ...formAdmin, password: e.target.value })}
                required
              />
            </div>
          </>
        ) : (
          // User form
          <>
              <div className="space-y-2">
                <Label htmlFor="empresaId">Empresa</Label>
                <select
                  id="empresaId"
                  className="input"
                  value={formUser.empresaId ? String(formUser.empresaId) : ''}
                  onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setFormUser({ ...formUser, empresaId: Number(e.target.value) })}
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

            <div className="space-y-2">
              <Label htmlFor="nombre">Full Name</Label>
              <Input
                id="nombre"
                placeholder="Jane User"
                value={formUser.nombre}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFormUser({ ...formUser, nombre: e.target.value })}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                placeholder="jane@example.com"
                value={formUser.email}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFormUser({ ...formUser, email: e.target.value })}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                value={formUser.password}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFormUser({ ...formUser, password: e.target.value })}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="rol">Rol</Label>
              <select
                id="rol"
                className="input"
                value={formUser.rol}
                onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setFormUser({ ...formUser, rol: e.target.value as any })}
              >
                <option value="viewer">viewer</option>
                <option value="complianceofficer">complianceofficer</option>
                <option value="auditor">auditor</option>
              </select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="adminId">Admin (opcional)</Label>
              <select
                id="adminId"
                className="input"
                value={formUser.adminId ? String(formUser.adminId) : ''}
                onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setFormUser({ ...formUser, adminId: e.target.value ? Number(e.target.value) : undefined })}
              >
                <option value="">{admins.length === 0 ? 'No hay admins' : 'Seleccione un admin (opcional)'}</option>
                {admins.map((a) => (
                  <option key={a.id} value={String(a.id)}>
                    {a.nombre}
                  </option>
                ))}
              </select>
            </div>
          </>
        )}

        <Button type="submit" className="w-full glow-button" disabled={loading}>
          {loading && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
          Create Account
        </Button>
      </form>
    </Card>
  )
}
