"use client"

import React, { useState } from 'react'
import protocoloService from '@/lib/protocoloService'
import type { Protocolo } from '@/types/protocolo'

export default function ProtocoloDashboard() {
  const [empresaId, setEmpresaId] = useState('')
  const [usuarioId, setUsuarioId] = useState('')
  const [politicaId, setPoliticaId] = useState('')
  const [pregunta, setPregunta] = useState('')
  const [loading, setLoading] = useState(false)
  const [created, setCreated] = useState<Protocolo[]>([])
  const [error, setError] = useState<string | null>(null)

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const payload = {
        empresaId: Number(empresaId),
        usuarioId: Number(usuarioId),
        politicaId: politicaId || undefined,
        pregunta,
        protocoloId: undefined,
      }
      const res = await protocoloService.crearProtocolo(payload)
      setCreated(prev => [res, ...prev])
      setPregunta('')
    } catch (err: any) {
      setError(err?.message || 'Error al crear protocolo')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h2 className="text-xl font-semibold mb-4">Protocolo (IA) — Crear</h2>
      <form onSubmit={handleCreate} className="space-y-2">
        <div>
          <label>Empresa ID</label>
          <input value={empresaId} onChange={e => setEmpresaId(e.target.value)} className="block" />
        </div>
        <div>
          <label>Usuario ID</label>
          <input value={usuarioId} onChange={e => setUsuarioId(e.target.value)} className="block" />
        </div>
        <div>
          <label>Politica ID (opcional)</label>
          <input value={politicaId} onChange={e => setPoliticaId(e.target.value)} className="block" />
        </div>
        <div>
          <label>Pregunta / Tema</label>
          <textarea value={pregunta} onChange={e => setPregunta(e.target.value)} rows={4} className="block w-full" />
        </div>
        <div>
          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Creando...' : 'Crear Protocolo'}
          </button>
        </div>
        {error && <div className="text-red-600">{error}</div>}
      </form>

      <hr className="my-6" />

      <h3 className="text-lg font-medium mb-2">Protocolos creados en esta sesión</h3>
      {created.length === 0 ? (
        <p>No se han creado protocolos todavía.</p>
      ) : (
        <ul className="space-y-4">
          {created.map(p => (
            <li key={p.id} className="border p-3 rounded">
              <strong>{p.nombre}</strong>
              <p>{p.descripcion}</p>
              <p><em>Objetivo:</em> {p.objetivo}</p>
              {p.reglas && (
                <details>
                  <summary>Reglas ({p.reglas.length})</summary>
                  <ol>
                    {p.reglas.map((r, i) => <li key={i}>{r}</li>)}
                  </ol>
                </details>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
