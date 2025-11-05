import type React from "react"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Sparkles, Shield, FileCheck, AlertTriangle, Users, Zap } from "lucide-react"

export default function HomePage() {
  return (
    <div className="min-h-screen bg-background">
      {/* Hero Section */}
      <section className="relative overflow-hidden border-b border-border">
        <div className="absolute inset-0 bg-gradient-to-br from-primary/20 via-background to-accent/20 blur-3xl" />

        <div className="relative container mx-auto px-4 py-24 md:py-32">
          <div className="max-w-4xl mx-auto text-center space-y-8">
            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full border border-primary/50 bg-primary/10 backdrop-blur-sm">
              <Sparkles className="w-4 h-4 text-primary" />
              <span className="text-sm font-medium text-foreground">Powered by AI</span>
            </div>

            <h1 className="text-5xl md:text-7xl font-bold text-balance">
              Ship <span className="text-primary glow-text">Compliance</span> products that work.
            </h1>

            <p className="text-xl text-muted-foreground max-w-2xl mx-auto text-balance">
              The end-to-end platform for building world-class compliance management systems with AI-powered automation.
            </p>

            <div className="flex flex-wrap gap-4 justify-center">
              <Link href="/dashboard">
                <Button size="lg" className="glow-button">
                  Get started
                </Button>
              </Link>
              <Link href="/auth/register">
                <Button size="lg" variant="outline">
                  Sign up for free
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Features Grid */}
      <section className="container mx-auto px-4 py-24">
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          <FeatureCard
            icon={<Shield className="w-6 h-6" />}
            title="Regulations"
            description="Store, organize, and manage regulatory compliance documentation with AI-powered insights."
          />
          <FeatureCard
            icon={<FileCheck className="w-6 h-6" />}
            title="Protocols"
            description="Build comprehensive protocols with real-time AI generation and validation."
          />
          <FeatureCard
            icon={<AlertTriangle className="w-6 h-6" />}
            title="Risk Management"
            description="Integrate AI models to identify, assess, and mitigate compliance risks automatically."
          />
          <FeatureCard
            icon={<Sparkles className="w-6 h-6" />}
            title="AI Audits"
            description="Generate instant compliance audits with detailed findings and recommendations."
          />
          <FeatureCard
            icon={<Users className="w-6 h-6" />}
            title="Team Management"
            description="Add user sign ups and logins, securing your data with enterprise-level security."
          />
          <FeatureCard
            icon={<Zap className="w-6 h-6" />}
            title="Real-time Sync"
            description="Instant ready-to-use APIs for seamless integration with your existing systems."
          />
        </div>
      </section>
    </div>
  )
}

function FeatureCard({ icon, title, description }: { icon: React.ReactNode; title: string; description: string }) {
  return (
    <div className="group relative p-6 rounded-lg border border-border bg-card hover:border-primary/50 transition-all duration-300 hover:shadow-glow">
      <div className="absolute inset-0 bg-gradient-to-br from-primary/5 to-accent/5 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity" />

      <div className="relative space-y-4">
        <div className="w-12 h-12 rounded-lg border border-border bg-background flex items-center justify-center text-primary group-hover:border-primary/50 transition-colors">
          {icon}
        </div>

        <div>
          <h3 className="text-lg font-semibold mb-2">{title}</h3>
          <p className="text-sm text-muted-foreground leading-relaxed">{description}</p>
        </div>
      </div>
    </div>
  )
}
