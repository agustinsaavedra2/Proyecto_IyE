"use client"

import { StatsCards } from "@/components/dashboard/stats-cards"
import { RecentActivity } from "@/components/dashboard/recent-activity"
import { QuickActions } from "@/components/dashboard/quick-actions"
import { Button } from '@/components/ui/button'
import BackButton from '@/components/ui/back-button'
import { useRouter } from 'next/navigation'

export default function DashboardPage() {
  const router = useRouter()
  return (
    <div className="min-h-screen bg-background">

      <main className="container mx-auto px-4 py-8 space-y-8">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold mb-2">Dashboard</h1>
            <p className="text-muted-foreground">Welcome back! Here's what's happening with your compliance management.</p>
          </div>

          <div className="flex items-center gap-2">
            <BackButton />
          </div>
        </div>

        <StatsCards />

        <div className="grid lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2">
            <RecentActivity />
          </div>
          <div>
            <QuickActions />
          </div>
        </div>
      </main>
    </div>
  )
}
