import { Suspense } from "react"
import VerifyClient from "./VerifyClient"

export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-12">Verificando...</div>}>
      <VerifyClient />
    </Suspense>
  )
}