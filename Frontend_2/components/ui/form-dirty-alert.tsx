import React from 'react'
import { Alert, AlertTitle, AlertDescription } from './alert'

type Props = {
  dirty: boolean
  message?: string
}

export default function FormDirtyAlert({ dirty, message }: Props) {
  if (!dirty) return null

  return (
    <Alert className="mb-4" variant="default">
      <AlertTitle>Formulario incompleto</AlertTitle>
      <AlertDescription>
        {message ?? (
          <p>
            Has llenado al menos un campo. Recuerda guardar los cambios o cancelar si no quieres
            conservarlos.
          </p>
        )}
      </AlertDescription>
    </Alert>
  )
}
