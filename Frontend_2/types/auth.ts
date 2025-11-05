export interface RegisterAdminDTO {
  nombre: string
  email: string
  password: string
}

export interface RegisterUserDTO {
  empresaId: number
  nombre: string
  email: string
  password: string
  rol: 'admin' | 'complianceofficer' | 'auditor' | 'viewer'
  adminId?: number
}

export interface LoginDTO {
    email: string
    password: string
}

export interface RequestRegisterDTO {
  tipo: 'admin' | 'user'
  empresaId?: number
  nombre?: string
  email: string
  password?: string
  rol?: 'admin' | 'complianceofficer' | 'auditor' | 'viewer'
  adminId?: number
}

// Usuario mínimo devuelto por el backend (ajusta según tu entidad real)
export interface UsuarioDTO {
  id: number
  empresaId?: number | null
  nombre: string
  email: string
  passwordHash: string
  rol: 'admin' | 'complianceofficer' | 'auditor' | 'viewer'
  activo: boolean
  // Las fechas que vienen de Java (LocalDateTime) se serializan como cadenas ISO en JSON.
  // En el frontend las representamos como strings (por ejemplo: "2025-10-30T12:34:56").
  ultimoAcceso?: string | null
  createDat?: string | null
  updateDat?: string | null
  deleteDat?: string | null
  // Agrega otros campos que tu backend devuelva si es necesario
}

// No default export for types
