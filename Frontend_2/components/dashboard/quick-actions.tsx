"use client"

import Link from 'next/link'
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Plus, Sparkles, FileCheck, AlertTriangle } from "lucide-react"
import { motion } from "framer-motion"

const actions = [
  { label: "Create Regulation", icon: FileCheck, href: "/regulations/new" },
  { label: "Generate AI Audit", icon: Sparkles, href: "/auditorias/crear" },
  { label: "Register User", icon: Plus, href: "/auth/register" },
  { label: "Add Risk", icon: AlertTriangle, href: "/risks/new" },
  { label: "Protocolos", icon: FileCheck, href: "/ollama/protocolo" },
  { label: "Procedimientos", icon: FileCheck, href: "/ollama/procedimiento" },
  { label: "New Protocol", icon: Plus, href: "/ollama/protocolo/crear" },
]

export function QuickActions() {
  return (
    <Card className="p-6">
      <h2 className="text-xl font-semibold mb-6">Quick Actions</h2>

      <div className="space-y-3">
        {actions.map((action, index) => {
          const Icon = action.icon

          return (
            <motion.div
              key={action.label}
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: index * 0.1 }}
            >
              <Button
                asChild
                variant="outline"
                className="w-full justify-start gap-3 h-auto py-4 hover:border-primary/50 hover:bg-primary/5 transition-all group bg-transparent"
              >
                <Link href={action.href} className="flex items-center gap-3 w-full">
                  <div className="p-2 rounded-lg border border-border bg-background group-hover:border-primary/50 transition-colors">
                    <Icon className="w-4 h-4 text-primary" />
                  </div>
                  <span className="font-medium">{action.label}</span>
                </Link>
              </Button>
            </motion.div>
          )
        })}
      </div>
    </Card>
  )
}
