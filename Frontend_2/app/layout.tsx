import type { Metadata } from 'next'
import { Geist, Geist_Mono } from 'next/font/google'
import { Analytics } from '@vercel/analytics/next'
import './globals.css'
import { SelectionProvider } from '@/components/dashboard/selection-context'
import { DashboardNav } from '@/components/dashboard/dashboard-nav'

const _geist = Geist({ subsets: ["latin"] });
const _geistMono = Geist_Mono({ subsets: ["latin"] });
export const metadata: Metadata = {
  title: 'PAAPSU',
  description: 'Platforma SAAS para MyPymes',
  generator: 'PAAPSU',
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  const enableVercel = process.env.NEXT_PUBLIC_VERCEL_ANALYTICS === '1'

  return (
    <html lang="en">
      <body className={`font-sans antialiased`}>
        <SelectionProvider>
          <DashboardNav />
          <main>{children}</main>
        </SelectionProvider>
        {enableVercel && <Analytics />}
      </body>
    </html>
  )
}
