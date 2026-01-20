import { Suspense } from "react"
import RegulationsClient from "./RegulationsClient"

export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-12">Cargando regulaciones...</div>}>
      <RegulationsClient />
    </Suspense>
  )
}