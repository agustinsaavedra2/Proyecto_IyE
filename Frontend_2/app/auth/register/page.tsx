"use client"

import { RegisterForm } from "@/components/auth/register-form"
import Link from "next/link"
import { Button } from '@/components/ui/button'
import BackButton from '@/components/ui/back-button'

export default function RegisterPage() {
  

  return (
    <div className="min-h-screen bg-background flex items-center justify-center p-4">
      <div className="w-full max-w-md space-y-8">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold mb-2">Create your account</h1>
            <p className="text-muted-foreground">Get started with compliance management</p>
          </div>
          <BackButton />
        </div>

        <RegisterForm />

        <p className="text-center text-sm text-muted-foreground">
          Already have an account?{" "}
          <Link href="/auth/login" className="text-primary hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}
