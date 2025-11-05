"use client"

import { useEffect, useState } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { Card } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Button } from '@/components/ui/button'
import apiFetcher from '@/lib/apis'

export default function EditRegulationPage() {
  const router = useRouter()
  const pathname = usePathname()
  const id = pathname?.split('/')?.[2] || ''

  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [nombre, setNombre] = useState('')
  const [contenido, setContenido] = useState('')
  const [urlDocumento, setUrlDocumento] = useState('')
  const [entidadEmisora, setEntidadEmisora] = useState('')
  const [anioEmision, setAnioEmision] = useState<number | ''>('')

  useEffect(() => {
    if (!id) return
    let mounted = true

    ;(async () => {
      try {
        const data = await apiFetcher(`/api/regulaciones/${id}`)
        if (!mounted) return
        setNombre(data.nombre || '')
        setContenido(data.contenido || '')
        setUrlDocumento(data.urlDocumento || '')
        setEntidadEmisora(data.entidadEmisora || '')
        setAnioEmision(data.anioEmision ?? '')
      } catch (err: any) {
        console.error(err)
        setError('Failed to load regulation')
      } finally {
        if (mounted) setLoading(false)
      }
    })()

    return () => {
      mounted = false
    }
  }, [id])

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    try {
      const payload = {
        nombre,
        contenido,
        urlDocumento: urlDocumento || null,
        entidadEmisora,
        anioEmision: anioEmision ? Number(anioEmision) : null,
      }

      await apiFetcher(`/api/regulaciones/${id}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      })

      router.push('/regulations')
    } catch (err) {
      console.error(err)
      alert('Failed to update regulation')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="p-8">Loading...</div>
  if (error) return <div className="p-8 text-destructive">{error}</div>

  return (
    <div className="min-h-screen bg-background">
      <main className="container mx-auto px-4 py-8">
        <Card className="p-6 max-w-2xl mx-auto">
          <h1 className="text-2xl font-bold mb-4">Edit Regulation</h1>

          <form onSubmit={onSubmit} className="space-y-4">
            <div>
              <label className="block mb-1 text-sm font-medium">Name</label>
              <Input value={nombre} onChange={(e) => setNombre(e.target.value)} required />
            </div>

            <div>
              <label className="block mb-1 text-sm font-medium">Content</label>
              <Textarea value={contenido} onChange={(e) => setContenido(e.target.value)} required />
            </div>

            <div>
              <label className="block mb-1 text-sm font-medium">Document URL</label>
              <Input value={urlDocumento} onChange={(e) => setUrlDocumento(e.target.value)} />
            </div>

            <div>
              <label className="block mb-1 text-sm font-medium">Issuing Entity</label>
              <Input value={entidadEmisora} onChange={(e) => setEntidadEmisora(e.target.value)} required />
            </div>

            <div>
              <label className="block mb-1 text-sm font-medium">Year of Issue</label>
              <Input
                type="number"
                value={anioEmision}
                onChange={(e) => setAnioEmision(e.target.value ? Number(e.target.value) : '')}
                min={1900}
                max={2100}
              />
            </div>

            <div className="flex justify-end">
              <Button type="submit" disabled={saving}>
                {saving ? 'Saving...' : 'Save Changes'}
              </Button>
            </div>
          </form>
        </Card>
      </main>
    </div>
  )
}
