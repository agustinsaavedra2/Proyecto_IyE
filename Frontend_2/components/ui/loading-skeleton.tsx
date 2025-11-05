"use client"

import { Card } from "@/components/ui/card"
import { motion } from "framer-motion"

export function LoadingSkeleton({ count = 3 }: { count?: number }) {
  return (
    <div className="space-y-4">
      {Array.from({ length: count }).map((_, i) => (
        <motion.div key={i} initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: i * 0.1 }}>
          <Card className="p-6">
            <div className="space-y-3">
              <div className="h-6 bg-muted rounded animate-pulse w-3/4" />
              <div className="h-4 bg-muted rounded animate-pulse w-full" />
              <div className="h-4 bg-muted rounded animate-pulse w-5/6" />
              <div className="flex gap-2 mt-4">
                <div className="h-6 bg-muted rounded animate-pulse w-20" />
                <div className="h-6 bg-muted rounded animate-pulse w-16" />
              </div>
            </div>
          </Card>
        </motion.div>
      ))}
    </div>
  )
}
