"use client"

import { useState, useEffect } from 'react'
import { Card } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Button } from '@/components/ui/button'
import DraftBackButton from '@/components/ui/draft-back-button'
import RestoreDraftPrompt from '@/components/ui/restore-draft-prompt'
import { usePathname } from 'next/navigation'
import FormDirtyAlert from '@/components/ui/form-dirty-alert'
import { DashboardNav } from '@/components/dashboard/dashboard-nav'
import { useRouter } from 'next/navigation'
import apiFetcher from '@/lib/apis'

export default function CreateRegulationPage() {
  const router = useRouter()
  const pathname = usePathname()
  const [nombre, setNombre] = useState('')
  const [contenido, setContenido] = useState('')
  const [urlDocumento, setUrlDocumento] = useState('')
  const [entidadEmisora, setEntidadEmisora] = useState('')
  const [anioEmision, setAnioEmision] = useState<number | ''>('')
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)

    try {
      const payload = {
        nombre,
        contenido,
        urlDocumento: urlDocumento || null,
        entidadEmisora,
        anioEmision: anioEmision ? Number(anioEmision) : null,
      }

      await apiFetcher('/api/regulaciones', {
        method: 'POST',
        body: JSON.stringify(payload),
      })

      try { localStorage.removeItem(`draft:${pathname}`) } catch (e) {}
      // navigate back to list
      router.push('/regulations')
    } catch (err: any) {
      // lightweight error handling — improve with toasts if desired
      console.error('Failed to create regulation', err)
      alert(err?.message || 'Failed to create regulation')
    } finally {
      setLoading(false)
    }
  }

  // autosave draft
  useEffect(() => {
    const key = `draft:${pathname}`
    const timer = window.setTimeout(() => {
      try {
        const any = Boolean(nombre || contenido || urlDocumento || entidadEmisora || anioEmision)
        if (any) localStorage.setItem(key, JSON.stringify({ nombre, contenido, urlDocumento, entidadEmisora, anioEmision }))
        else localStorage.removeItem(key)
      } catch (e) {}
    }, 400)
    return () => window.clearTimeout(timer)
  }, [nombre, contenido, urlDocumento, entidadEmisora, anioEmision, pathname])

  function handleRestoreDraft(data: any) {
    try {
      setNombre(data.nombre ?? '')
      setContenido(data.contenido ?? '')
      setUrlDocumento(data.urlDocumento ?? '')
      setEntidadEmisora(data.entidadEmisora ?? '')
      setAnioEmision(data.anioEmision === undefined ? '' : data.anioEmision)
    } catch (e) {}
  }

  return (
    <div className="min-h-screen bg-background">
      <DashboardNav />

      <main className="container mx-auto px-4 py-8">
        <Card className="p-6 max-w-2xl mx-auto">
          <div className="flex justify-between items-center mb-4">
            <h1 className="text-2xl font-bold">Create Regulation</h1>
            <DraftBackButton draftKey={`draft:${pathname}`} />
          </div>

          <FormDirtyAlert dirty={Boolean(nombre || contenido || urlDocumento || entidadEmisora || anioEmision)} />

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
              <Button type="submit" disabled={loading}>
                {loading ? 'Creating...' : 'Create Regulation'}
              </Button>
            </div>
          </form>
        </Card>
      </main>
    </div>
  )
}
