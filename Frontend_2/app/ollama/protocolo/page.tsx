"use client"

import React from 'react'
import dynamic from 'next/dynamic'
import Link from 'next/link'
import { Button } from '@/components/ui/button'
import BackButton from '@/components/ui/back-button'

const ProtocoloDashboard = dynamic(() => import('@/components/protocolo/protocolo-dashboard'), { ssr: false })
const ProtocoloList = dynamic(() => import('@/components/protocolo/protocolo-list'), { ssr: false })

export default function ProtocoloPage() {
  return (
    <main className="container mx-auto px-4 py-8">
      <div className="mb-8 flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold mb-2">Protocolos</h1>
          <p className="text-muted-foreground">Listado de protocolos generados</p>
        </div>

        <div className="flex items-center gap-2">
          <BackButton />
          <Button asChild>
            <Link href="/ollama/protocolo/crear">Crear protocolo</Link>
          </Button>
        </div>
      </div>

      <ProtocoloDashboard />
      <div className="mt-6">
        <ProtocoloList />
      </div>
    </main>
  )
}
