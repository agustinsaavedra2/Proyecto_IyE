"use client"

import React, { useEffect, useState } from 'react'
import BackButton from '@/components/ui/back-button'
import userService from '@/lib/userService'
import empresaService from '@/lib/empresaService'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import Link from 'next/link'
import LoadingOverlay from '@/components/ui/loading-overlay'
import { DashboardNav } from '@/components/dashboard/dashboard-nav'
import type { UsuarioDTO } from '@/types/auth'
import type { EmpresaDTO } from '@/types/empresa'

export default function UsersPage() {
  const [loading, setLoading] = useState(false)
  const [empresas, setEmpresas] = useState<EmpresaDTO[]>([])
  const [selectedEmpresa, setSelectedEmpresa] = useState<number | ''>('')
  const [users, setUsers] = useState<UsuarioDTO[]>([])
  const [allUsers, setAllUsers] = useState<UsuarioDTO[]>([])
  const [error, setError] = useState<string | null>(null)
  

  useEffect(() => {
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const eps = await empresaService.getEmpresasDTO()
        if (!mounted) return
        setEmpresas(eps || [])
      } catch (e: any) {
        console.error('Failed to load empresas', e)
        setError(e?.message || String(e))
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => {
      mounted = false
    }
  }, [])

  // Fetch full list once and filter client-side when empresa is selected.
  useEffect(() => {
    let mounted = true
    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const all = await userService.getAllUsuariosDTO()
        if (!mounted) return
        setAllUsers(all || [])
        // apply current filter
        if (selectedEmpresa === '') {
          setUsers(all || [])
        } else {
          setUsers((all || []).filter((u) => u.empresaId === Number(selectedEmpresa)))
        }
      } catch (e: any) {
        console.error('Failed to load users', e)
        setError(e?.message || String(e))
      } finally {
        if (mounted) setLoading(false)
      }
    })()
    return () => {
      mounted = false
    }
  }, [selectedEmpresa])

  return (
    <div className="min-h-screen bg-background">
      <DashboardNav />

      <main className="container mx-auto px-4 py-8">
          <div className="mb-8 flex items-start justify-between">
            <div>
              <h1 className="text-3xl font-bold mb-2">Usuarios</h1>
              <p className="text-muted-foreground">Gestión de usuarios: revisa, filtra y administra cuentas.</p>
            </div>

            <div className="flex items-center gap-2">
              <BackButton />
              <Button asChild>
                <Link href="/auth/register">Registrar usuario</Link>
              </Button>
            </div>
          </div>

          <div className="mb-4 flex items-center gap-3">
            <label className="text-sm">Filtrar por empresa:</label>
            <select
              className="rounded border px-3 py-2"
              value={selectedEmpresa === '' ? '' : String(selectedEmpresa)}
              onChange={(e) => setSelectedEmpresa(e.target.value === '' ? '' : Number(e.target.value))}
            >
              <option value="">Todas</option>
              {empresas.map((ep) => (
                <option key={ep.id} value={ep.id}>
                  {ep.nombre}
                </option>
              ))}
            </select>
          </div>

          <LoadingOverlay loading={loading} fullScreen={false} message={loading ? 'Cargando usuarios...' : undefined} />

          {error && <Card className="p-4 mb-4 text-red-600">Error: {error}</Card>}

          <Card className="p-0 overflow-auto">
            <table className="w-full table-fixed text-sm">
              <thead>
                <tr className="text-left border-b">
                  <th className="p-3">ID</th>
                  <th className="p-3">Nombre</th>
                  <th className="p-3">Email</th>
                  <th className="p-3">Rol</th>
                  <th className="p-3">Activo</th>
                  <th className="p-3">EmpresaId</th>
                </tr>
              </thead>
              <tbody>
                {users.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="p-4 text-center text-muted-foreground">
                      No hay usuarios encontrados
                    </td>
                  </tr>
                ) : (
                  users.map((u) => (
                    <tr key={u.id} className="border-b last:border-b-0">
                      <td className="p-3">{u.id}</td>
                      <td className="p-3 font-medium">{u.nombre}</td>
                      <td className="p-3">{u.email}</td>
                      <td className="p-3">{u.rol}</td>
                      <td className="p-3">{u.activo ? 'Sí' : 'No'}</td>
                      <td className="p-3">{u.empresaId ?? '-'}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </Card>
      </main>
    </div>
  )
}
 
