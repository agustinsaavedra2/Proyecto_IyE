"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Shield, FileCheck, FileText, List, BookOpen, AlertTriangle, Sparkles, Users, Menu, Briefcase } from "lucide-react"
import { useState } from "react"

const navItems = [
  { href: "/dashboard", label: "Dashboard", icon: Shield },
  { href: "/regulations", label: "Regulations", icon: FileCheck },
  { href: "/ollama/protocolo", label: "Protocolos", icon: FileText },
  { href: "/ollama/procedimiento", label: "Procedimientos", icon: List },
  { href: "/ollama/politica", label: "Políticas", icon: BookOpen },
  { href: "/risks", label: "Risks", icon: AlertTriangle },
  { href: "/ollama/crearAuditoria", label: "Audits", icon: Sparkles },
  { href: "/users", label: "Users", icon: Users },
  { href: "/empresas", label: "Empresas", icon: Briefcase },
]

export function DashboardNav() {
  const pathname = usePathname()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  return (
    <nav className="border-b border-border bg-card/50 backdrop-blur-sm sticky top-0 z-50">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          <Link href="/" className="text-xl font-bold flex items-center gap-2">
            <Shield className="w-6 h-6 text-primary" />
            <span>ComplianceAI</span>
          </Link>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center gap-1">
            {navItems.map((item) => {
              const Icon = item.icon
              const isActive = pathname === item.href

              return (
                <Link key={item.href} href={item.href}>
                  <Button
                    variant="ghost"
                    className={cn(
                      "gap-2",
                      isActive && "bg-primary/10 text-primary border-b-2 border-primary rounded-b-none",
                    )}
                  >
                    <Icon className="w-4 h-4" />
                    {item.label}
                  </Button>
                </Link>
              )
            })}
          </div>

          {/* Mobile Menu Button */}
          <Button variant="ghost" size="icon" className="md:hidden" onClick={() => setMobileMenuOpen(!mobileMenuOpen)}>
            <Menu className="w-5 h-5" />
          </Button>
        </div>

        {/* Mobile Navigation */}
        {mobileMenuOpen && (
          <div className="md:hidden py-4 space-y-2">
            {navItems.map((item) => {
              const Icon = item.icon
              const isActive = pathname === item.href

              return (
                <Link key={item.href} href={item.href} onClick={() => setMobileMenuOpen(false)}>
                  <Button
                    variant="ghost"
                    className={cn("w-full justify-start gap-2", isActive && "bg-primary/10 text-primary")}
                  >
                    <Icon className="w-4 h-4" />
                    {item.label}
                  </Button>
                </Link>
              )
            })}
          </div>
        )}
      </div>
    </nav>
  )
}
