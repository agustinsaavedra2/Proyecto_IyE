"use client"

import { useState } from 'react'
import { useSearchParams, useRouter } from 'next/navigation'
import { completeRegister } from '@/lib/userService'
import { Card } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import BackButton from '@/components/ui/back-button'

export default function CompletePage() {
  const router = useRouter()
  const params = useSearchParams()
  const token = params?.get('token')
  const [password, setPassword] = useState('')
  const [password2, setPassword2] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!token) return setError('Token no encontrado')
    if (password.length < 6) return setError('La contraseña debe tener al menos 6 caracteres')
    if (password !== password2) return setError('Las contraseñas no coinciden')
    setLoading(true)
    try {
      await completeRegister(token, password)
      router.push('/auth/login')
    } catch (e: any) {
      setError(e?.message || 'Error al completar registro')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-md mx-auto py-12">
      <Card className="p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-medium">Completar registro</h2>
          <BackButton />
        </div>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="password">Nueva contraseña</Label>
            <Input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <div>
            <Label htmlFor="password2">Repetir contraseña</Label>
            <Input id="password2" type="password" value={password2} onChange={(e) => setPassword2(e.target.value)} required />
          </div>

          {error && <div className="text-sm text-red-600">{error}</div>}

          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Guardando...' : 'Establecer contraseña'}
          </Button>
        </form>
      </Card>
    </div>
  )
}
