"use client"

import { useRouter } from 'next/navigation'
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Sparkles } from "lucide-react"

export function AuditsList() {
  const router = useRouter()

  return (
    <div className="space-y-6">
      <div className="flex justify-end">
        <Button className="gap-2 glow-button" onClick={() => router.push('/auditorias/crear')}>
          <Sparkles className="w-4 h-4" />
          Generate AI Audit
        </Button>
      </div>

      <Card className="p-12 text-center">
        <div className="max-w-md mx-auto space-y-4">
          <div className="w-16 h-16 rounded-full border-2 border-dashed border-primary/50 flex items-center justify-center mx-auto">
            <Sparkles className="w-8 h-8 text-primary" />
          </div>
          <h3 className="text-xl font-semibold">No audits yet</h3>
          <p className="text-muted-foreground">
            Generate your first AI-powered compliance audit to get detailed insights and recommendations.
          </p>
          <Button className="gap-2 glow-button" onClick={() => router.push('/auditorias/crear')}>
            <Sparkles className="w-4 h-4" />
            Create First Audit
          </Button>
        </div>
      </Card>
    </div>
  )
}
