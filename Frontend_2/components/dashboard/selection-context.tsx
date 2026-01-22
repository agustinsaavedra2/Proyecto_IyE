"use client"

import React, { createContext, useContext, useState } from 'react'

type SelectionState = {
  empresaId?: number | ''
  usuarioId?: number | ''
  setEmpresaId: (v: number | '') => void
  setUsuarioId: (v: number | '') => void
}

const SelectionContext = createContext<SelectionState | null>(null)

export function SelectionProvider({ children }: { children: React.ReactNode }) {
  const [empresaId, setEmpresaId] = useState<number | ''>('')
  const [usuarioId, setUsuarioId] = useState<number | ''>('')

  return (
    <SelectionContext.Provider value={{ empresaId, usuarioId, setEmpresaId, setUsuarioId }}>
      {children}
    </SelectionContext.Provider>
  )
}

export function useSelection() {
  const ctx = useContext(SelectionContext)
  if (!ctx) throw new Error('useSelection must be used inside SelectionProvider')
  return ctx
}
