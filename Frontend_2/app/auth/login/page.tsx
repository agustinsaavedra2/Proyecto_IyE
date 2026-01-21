import { Suspense } from "react"
import LoginClient from "./LoginClient"

export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-12">Cargando...</div>}>
      <LoginClient />
    </Suspense>
  )
}