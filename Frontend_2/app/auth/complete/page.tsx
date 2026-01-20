import { Suspense } from "react"
import CompleteClient from "./CompleteClient"

export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-12">Cargando...</div>}>
      <CompleteClient />
    </Suspense>
  )
}