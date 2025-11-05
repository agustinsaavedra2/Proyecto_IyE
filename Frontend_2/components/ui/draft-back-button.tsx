"use client"

import React, { useEffect, useRef, useState } from 'react'
import { usePathname, useRouter } from 'next/navigation'
import { Button } from './button'

type Props = {
  draftKey?: string
  children?: React.ReactNode
  className?: string
}

export default function DraftBackButton({ draftKey, children = 'Volver', className }: Props) {
  const router = useRouter()
  const pathname = usePathname()
  const key = draftKey ?? `draft:${pathname}`
  const [hasDraft, setHasDraft] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [count, setCount] = useState(5)
  const timerRef = useRef<number | null>(null)

  useEffect(() => {
    try {
      const raw = localStorage.getItem(key)
      setHasDraft(!!raw)
    } catch (e) {
      setHasDraft(false)
    }
  }, [key])

  useEffect(() => {
    if (!showConfirm) {
      if (timerRef.current) {
        window.clearInterval(timerRef.current)
        timerRef.current = null
      }
      setCount(5)
      return
    }

    timerRef.current = window.setInterval(() => {
      setCount((c) => c - 1)
    }, 1000)

    return () => {
      if (timerRef.current) {
        window.clearInterval(timerRef.current)
        timerRef.current = null
      }
    }
  }, [showConfirm])

  useEffect(() => {
    if (count <= 0 && showConfirm) {
      // proceed
      doNavigate()
    }
  }, [count, showConfirm])

  function doNavigate() {
    // behave like BackButton: try back, otherwise push to /dashboard
    try {
      if (typeof window !== 'undefined' && window.history.length > 1) {
        router.back()
      } else {
        router.push('/dashboard')
      }
    } catch (e) {
      router.push('/dashboard')
    }
  }

  function onClick() {
    if (!hasDraft) return doNavigate()
    setShowConfirm(true)
  }

  function cancel() {
    setShowConfirm(false)
  }

  function goNow() {
    setShowConfirm(false)
    doNavigate()
  }

  return (
    <div className={className}>
      <Button variant="ghost" onClick={onClick}>
        {children}
      </Button>

      {showConfirm && (
        <div className="mt-2 rounded-md border bg-card p-3 text-sm">
          <div className="flex items-center justify-between">
            <div>
              <strong>Datos detectados</strong>
              <div className="text-muted-foreground">Hay información en el formulario.</div>
            </div>
            <div className="text-sm">Volviendo en {count}s</div>
          </div>

          <div className="mt-3 flex gap-2 justify-end">
            <Button variant="ghost" onClick={cancel}>Cancelar</Button>
            <Button onClick={goNow}>Volver ahora</Button>
          </div>
        </div>
      )}
    </div>
  )
}
