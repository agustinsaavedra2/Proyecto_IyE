"use client"

import { Spinner } from './spinner'

type LoadingOverlayProps = {
  loading: boolean
  message?: string
  fullScreen?: boolean
  className?: string
}

export function LoadingOverlay({ loading, message, fullScreen = false, className = '' }: LoadingOverlayProps) {
  if (!loading) return null

  if (fullScreen) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
        <div className="bg-white/90 dark:bg-slate-900/90 rounded p-6 flex flex-col items-center space-y-3 shadow">
          <Spinner />
          {message && <div className="text-sm text-muted-foreground">{message}</div>}
        </div>
      </div>
    )
  }

  return (
    <div className={`inline-flex items-center space-x-3 ${className}`} role="status" aria-live="polite">
      <Spinner />
      {message && <div className="text-sm text-muted-foreground">{message}</div>}
    </div>
  )
}

export default LoadingOverlay
