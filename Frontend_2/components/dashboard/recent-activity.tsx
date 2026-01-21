"use client"

import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { FileCheck, AlertTriangle, Sparkles, Clock } from "lucide-react"
import { motion } from "framer-motion"

const activities = [
  {
    id: 1,
    type: "regulation",
    title: "New regulation added: GDPR Compliance",
    time: "2 hours ago",
    icon: FileCheck,
    color: "text-blue-500",
  },
  {
    id: 2,
    type: "risk",
    title: "Risk assessment completed for Data Security",
    time: "5 hours ago",
    icon: AlertTriangle,
    color: "text-orange-500",
  },
  {
    id: 3,
    type: "audit",
    title: "AI Audit generated for Q4 2024",
    time: "1 day ago",
    icon: Sparkles,
    color: "text-purple-500",
  },
  {
    id: 4,
    type: "regulation",
    title: "Updated protocol: Employee Safety Standards",
    time: "2 days ago",
    icon: FileCheck,
    color: "text-blue-500",
  },
]

export function RecentActivity() {
  return (
    <Card className="p-6">
      <h2 className="text-xl font-semibold mb-6">Recent Activity</h2>

      <div className="space-y-4">
        {activities.map((activity, index) => {
          const Icon = activity.icon

          return (
            <motion.div
              key={activity.id}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: index * 0.1 }}
              className="flex items-start gap-4 p-4 rounded-lg border border-border hover:border-primary/50 transition-colors group"
            >
              <div className={`p-2 rounded-lg border border-border bg-background ${activity.color}`}>
                <Icon className="w-4 h-4" />
              </div>

              <div className="flex-1 min-w-0">
                <p className="font-medium text-sm mb-1 group-hover:text-primary transition-colors">{activity.title}</p>
                <div className="flex items-center gap-2 text-xs text-muted-foreground">
                  <Clock className="w-3 h-3" />
                  {activity.time}
                </div>
              </div>

              <Badge variant="outline" className="capitalize">
                {activity.type}
              </Badge>
            </motion.div>
          )
        })}
      </div>
    </Card>
  )
}
