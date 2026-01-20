"use client"

import React, { useState } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { Card } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { Loader2 } from 'lucide-react'
import { login } from '@/lib/userService'
import { toast } from '@/hooks/use-toast'

export default function LoginPage() {
  const router = useRouter()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const searchParams = useSearchParams()
  const verified = searchParams?.get('verified') === 'true'

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      const resp = await login({ email, password })
      // If backend returns the JwtResponse with token, store it for subsequent requests
      if (resp && resp.token) {
        try {
          localStorage.setItem('token', resp.token)
        } catch (e) {
          // ignore storage errors
        }
        toast({ title: 'Bienvenido', description: 'Has iniciado sesión correctamente.' })
        router.push('/dashboard')
      } else if (resp === true) {
        // backward compatibility: some backends returned boolean
        toast({ title: 'Bienvenido', description: 'Has iniciado sesión correctamente.' })
        router.push('/dashboard')
      } else {
        toast({ title: 'Error', description: 'Credenciales inválidas' })
      }
    } catch (err: any) {
      console.error('Login error', err)
      toast({ title: 'Error', description: err?.message || 'Error al iniciar sesión' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex items-center justify-center min-h-screen bg-background p-4">
      <div className="w-full max-w-md">
        <Card className="p-6">
          {verified && (
            <div className="mb-4 rounded-md border border-green-200 bg-green-50 px-3 py-2 text-green-800">
              Cuenta verificada con éxito. Ahora puedes iniciar sesión.
            </div>
          )}
          <h1 className="text-2xl font-bold mb-4">Sign in</h1>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </div>
            <div>
              <Label htmlFor="password">Password</Label>
              <Input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
            </div>
            <Button type="submit" className="w-full" disabled={loading}>
              {loading && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
              Sign in
            </Button>
          </form>
        </Card>
      </div>
    </div>
  )
}
