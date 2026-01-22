"use client"

import React from 'react'
import dynamic from 'next/dynamic'
import Link from 'next/link'
import { Button } from '@/components/ui/button'
import BackButton from '@/components/ui/back-button'

const PoliticaList = dynamic(() => import('@/components/politica/politica-list'), { ssr: false })

export default function PoliticaPage() {
  return (
    <main className="container mx-auto px-4 py-8">
      <div className="mb-8 flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold mb-2">Políticas</h1>
          <p className="text-muted-foreground">Listado de políticas</p>
        </div>

        <div className="flex items-center gap-2">
          <BackButton />
          <Button asChild>
            <Link href="/ollama/politica/crear">Crear política</Link>
          </Button>
        </div>
      </div>

      <PoliticaList />
    </main>
  )
}
