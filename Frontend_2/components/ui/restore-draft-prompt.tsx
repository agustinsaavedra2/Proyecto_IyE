"use client"

import React, { useEffect, useState } from 'react'
import { usePathname } from 'next/navigation'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from './dialog'
import { Button } from './button'

type Props = {
  draftKey?: string
  onRestore: (data: any) => void
}

export default function RestoreDraftPrompt({ draftKey, onRestore }: Props) {
  const pathname = usePathname()
  const key = draftKey ?? `draft:${pathname}`
  const [open, setOpen] = useState(false)
  const [draft, setDraft] = useState<any | null>(null)

  useEffect(() => {
    try {
      const raw = localStorage.getItem(key)
      if (!raw) return
      const parsed = JSON.parse(raw)
      setDraft(parsed)
      setOpen(true)
    } catch (e) {
      // ignore parsing/localStorage errors
    }
  }, [key])

  function handleRestore() {
    if (!draft) return
    onRestore(draft)
    setOpen(false)
  }

  function handleKeepEditing() {
    setOpen(false)
  }

  function handleDeleteDraft() {
    try {
      localStorage.removeItem(key)
    } catch (e) {
      // ignore
    }
    setDraft(null)
    setOpen(false)
  }

  if (!draft) return null

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Borrador encontrado</DialogTitle>
          <DialogDescription>Se ha encontrado un borrador guardado localmente. ¿Deseas restaurarlo?</DialogDescription>
        </DialogHeader>

        <div className="mt-2 max-h-40 overflow-auto rounded border bg-muted p-3 text-sm">
          <pre className="whitespace-pre-wrap">{JSON.stringify(draft, null, 2)}</pre>
        </div>

        <DialogFooter>
          <Button variant="ghost" onClick={handleKeepEditing}>Seguir editando</Button>
          <Button variant="outline" onClick={handleDeleteDraft}>Borrar borrador</Button>
          <Button onClick={handleRestore}>Restaurar borrador</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
