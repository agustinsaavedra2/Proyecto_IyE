"use client"

import React from 'react'
import { useRouter } from 'next/navigation'
import { Button } from '@/components/ui/button'

type BackButtonProps = React.ComponentProps<typeof Button> & {
  children?: React.ReactNode
}

export default function BackButton({ children = 'Volver', ...props }: BackButtonProps) {
  const router = useRouter()

  function handleClick() {
    if (typeof window !== 'undefined' && window.history.length > 1) {
      router.back()
    } else {
      router.push('/dashboard')
    }
  }

  return (
    <Button variant="ghost" onClick={handleClick} {...props}>
      {children}
    </Button>
  )
}
