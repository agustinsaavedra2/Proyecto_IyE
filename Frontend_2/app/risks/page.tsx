"use client"

import { RisksList } from "@/components/risks/risks-list"
import { Button } from '@/components/ui/button'
import BackButton from '@/components/ui/back-button'
import Link from 'next/link'
import { useRouter } from 'next/navigation'

export default function RisksPage() {
  const router = useRouter()
  return (
    <div className="min-h-screen bg-background">

      <main className="container mx-auto px-4 py-8">
        <div className="mb-8 flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold mb-2">Risk Management</h1>
            <p className="text-muted-foreground">Identify, assess, and mitigate compliance risks</p>
          </div>

            <div className="flex items-center gap-2">
            <BackButton />
            <Button asChild>
              <Link href="/risks/new">Crear riesgo</Link>
            </Button>
          </div>
        </div>

        <RisksList />
      </main>
    </div>
  )
}
