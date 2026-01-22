"use client"

import { RegulationsList } from "@/components/regulations/regulations-list"
import { Button } from '@/components/ui/button'
import BackButton from '@/components/ui/back-button'
import Link from 'next/link'
import { useRouter } from 'next/navigation'

export default function RegulationsPage() {
  const router = useRouter()
  return (
    <div className="min-h-screen bg-background">

      <main className="container mx-auto px-4 py-8">
        <div className="mb-8 flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold mb-2">Regulations</h1>
            <p className="text-muted-foreground">Manage your regulatory compliance documentation</p>
          </div>

            <div className="flex items-center gap-2">
            <BackButton />
            <Button asChild>
              <Link href="/regulations/new">Crear regulation</Link>
            </Button>
          </div>
        </div>

        <RegulationsList />
      </main>
    </div>
  )
}
