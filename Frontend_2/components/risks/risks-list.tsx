"use client"

import { useState } from "react"
import useSWR from "swr"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Search, Plus, Sparkles } from "lucide-react"
import { motion } from "framer-motion"
import { LoadingSkeleton } from "@/components/ui/loading-skeleton"
import apiFetcher from '@/lib/apis'
import riesgoService from "@/lib/riesgoService"

export function RisksList() {
  const [searchQuery, setSearchQuery] = useState("")
  const { data: risks, error, isLoading } = useSWR('/api/riesgos', () => riesgoService.getRiesgos())
  if (isLoading) {
    return <LoadingSkeleton count={5} />
  }

  if (error) {
    return (
      <Card className="p-8 text-center">
        <p className="text-muted-foreground">Failed to load risks. Please try again.</p>
      </Card>
    )
  }

  const filteredRisks = risks?.filter((risk: any) => risk.nombre?.toLowerCase().includes(searchQuery.toLowerCase()))

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <Input
            placeholder="Search risks..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10"
          />
        </div>
        <div className="flex gap-2">
          <Button variant="outline" className="gap-2 bg-transparent">
            <Sparkles className="w-4 h-4" />
            Generate with AI
          </Button>
          <Button className="gap-2 glow-button">
            <Plus className="w-4 h-4" />
            Add Risk
          </Button>
        </div>
      </div>

      <div className="grid gap-4">
        {filteredRisks?.map((risk: any, index: number) => (
          <motion.div
            key={risk.id}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.05 }}
          >
            <Card className="p-6 hover:border-primary/50 transition-all duration-300 hover:shadow-glow">
              <div className="space-y-4">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold mb-2">{risk.nombre || "Unnamed Risk"}</h3>
                    <p className="text-sm text-muted-foreground">{risk.descripcion}</p>
                  </div>
                  <Badge variant={risk.nivel === "high" ? "destructive" : "outline"}>{risk.nivel || "Medium"}</Badge>
                </div>
              </div>
            </Card>
          </motion.div>
        ))}

        {(!filteredRisks || filteredRisks.length === 0) && (
          <Card className="p-12 text-center">
            <p className="text-muted-foreground mb-4">No risks found</p>
            <Button className="gap-2">
              <Plus className="w-4 h-4" />
              Create your first risk
            </Button>
          </Card>
        )}
      </div>
    </div>
  )
}
