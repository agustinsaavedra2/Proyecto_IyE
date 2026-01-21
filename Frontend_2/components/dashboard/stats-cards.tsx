"use client"

import { Card } from "@/components/ui/card"
import { FileCheck, AlertTriangle, Sparkles, TrendingUp } from "lucide-react"
import { motion } from "framer-motion"

const stats = [
  { label: "Total Regulations", value: "24", icon: FileCheck, trend: "+12%", color: "text-blue-500" },
  { label: "Active Risks", value: "8", icon: AlertTriangle, trend: "-5%", color: "text-orange-500" },
  { label: "AI Audits", value: "15", icon: Sparkles, trend: "+23%", color: "text-purple-500" },
  { label: "Compliance Score", value: "94%", icon: TrendingUp, trend: "+8%", color: "text-green-500" },
]

export function StatsCards() {
  return (
    <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {stats.map((stat, index) => {
        const Icon = stat.icon

        return (
          <motion.div
            key={stat.label}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.1 }}
          >
            <Card className="p-6 hover:border-primary/50 transition-all duration-300 hover:shadow-glow group">
              <div className="flex items-start justify-between mb-4">
                <div className={cn("p-2 rounded-lg border border-border bg-background", stat.color)}>
                  <Icon className="w-5 h-5" />
                </div>
                <span className="text-sm font-medium text-green-500">{stat.trend}</span>
              </div>

              <div>
                <p className="text-3xl font-bold mb-1">{stat.value}</p>
                <p className="text-sm text-muted-foreground">{stat.label}</p>
              </div>
            </Card>
          </motion.div>
        )
      })}
    </div>
  )
}

function cn(...classes: string[]) {
  return classes.filter(Boolean).join(" ")
}
