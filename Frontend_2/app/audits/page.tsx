"use client"

import { DashboardNav } from "@/components/dashboard/dashboard-nav"
import { AuditsList } from "@/components/audits/audits-list"
import { Button } from '@/components/ui/button'
import BackButton from '@/components/ui/back-button'
import Link from 'next/link'
import { useRouter } from 'next/navigation'

export default function AuditsPage() {
  const router = useRouter()
  return (
    <div className="min-h-screen bg-background">
      <DashboardNav />

      <main className="container mx-auto px-4 py-8">
        <div className="mb-8 flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold mb-2">AI Audits</h1>
            <p className="text-muted-foreground">Generate and manage compliance audits with AI</p>
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
