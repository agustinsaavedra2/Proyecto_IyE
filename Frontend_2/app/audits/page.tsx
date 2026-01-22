"use client"

import { AuditsList } from "@/components/audits/audits-list"
import { Button } from '@/components/ui/button'
import BackButton from '@/components/ui/back-button'
import Link from 'next/link'
import { useRouter } from 'next/navigation'

export default function AuditsPage() {
  const router = useRouter()
  return (
    <div className="min-h-screen bg-background">

      <main className="container mx-auto px-4 py-8">
        <div className="mb-8 flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold mb-2">audirtoria AI</h1>
            <p className="text-muted-foreground">Generar y gestionar auditorías de cumplimiento con IA</p>
          </div>

            <div className="flex items-center gap-2">
            <BackButton />
            <Button asChild>
              <Link href="/auditorias/crear">Crear auditoría</Link>
            </Button>
          </div>
        </div>

        <AuditsList />
      </main>
    </div>
  )
}
