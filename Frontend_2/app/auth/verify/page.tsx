"use client"

import { useEffect, useState } from 'react'
import { useSearchParams, useRouter } from 'next/navigation'
import { verifyRegister } from '@/lib/userService'
import { Card } from '@/components/ui/card'

export default function VerifyPage() {
  const router = useRouter()
  const params = useSearchParams()
  const token = params?.get('token')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!token) {
      setError('Token no proporcionado')
      setLoading(false)
      return
    }
    ;(async () => {
      try {
  const res = await verifyRegister(token)
  // After verification, send user to login (we don't auto-complete password here)
  // If you want to surface a success flag you can add a query parameter, e.g. ?verified=true
  router.push('/auth/login')
      } catch (e: any) {
        setError(e?.message || 'Token inválido o caducado')
      } finally {
        setLoading(false)
      }
    })()
  }, [token, router])

  return (
    <div className="max-w-md mx-auto py-12">
      <Card className="p-6">
        {loading ? (
          <div>Verificando...</div>
        ) : error ? (
          <div className="text-red-600">Error: {error}</div>
        ) : (
          <div>Verificado. Redirigiendo...</div>
        )}
      </Card>
    </div>
  )
}
