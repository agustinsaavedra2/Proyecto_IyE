import apiFetcher from './apis'
import type { LoginDTO, RegisterAdminDTO, RegisterUserDTO, UsuarioDTO, RequestRegisterDTO } from '@/types/auth'
import type { EmpresaDTO } from '@/types/empresa'

export async function registerAdmin(data: RegisterAdminDTO): Promise<UsuarioDTO> {
  return apiFetcher('/api/usuarios/registerAdmin', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export async function registerUser(data: RegisterUserDTO): Promise<UsuarioDTO> {
  return apiFetcher('/api/usuarios/registerUser', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export async function login(data: LoginDTO): Promise<any> {
  // returns the backend JwtResponse: { token, refreshToken, expiresIn }
  return apiFetcher('/api/usuarios/login', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export async function requestRegister(data: RequestRegisterDTO): Promise<void> {
  return apiFetcher('/api/usuarios/request-register', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export async function verifyRegister(token: string): Promise<{ next?: string }> {
  return apiFetcher(`/api/usuarios/verify-register?token=${encodeURIComponent(token)}`, {
    method: 'POST',
  })
}

export async function completeRegister(token: string, password: string): Promise<void> {
  // using query params as you described; adjust if backend prefers JSON body
  const url = `/api/usuarios/complete-register?token=${encodeURIComponent(token)}&password=${encodeURIComponent(
    password,
  )}`
  return apiFetcher(url, { method: 'POST' })
}

export async function getAllUsuariosDTO(): Promise<UsuarioDTO[]> {
  // Calls the controller that returns all users: GET /api/usuarios/users
  return apiFetcher('/api/usuarios/users')
}

export async function getUsuariosEmpresaDTO(empresaId: number): Promise<EmpresaDTO[]> {
  // The backend endpoint `/api/usuarios/usersdto?empresaId=...` returns a minimal list
  // (id + nombre) used for selects. That shape is represented by `EmpresaDTO`.
  const url = `/api/usuarios/usersdto?empresaId=${encodeURIComponent(String(empresaId))}`
  return apiFetcher(url)
}

// Compatibility helper: original code used `getUsuariosDTO(empresaId?)` — keep an overload
export function getUsuariosDTO(): Promise<UsuarioDTO[]>
export function getUsuariosDTO(empresaId: number): Promise<EmpresaDTO[]>
export function getUsuariosDTO(empresaId?: number) {
  if (typeof empresaId === 'number') {
    return getUsuariosEmpresaDTO(empresaId)
  }
  return getAllUsuariosDTO()
}

export async function getAdminsDTO(): Promise<EmpresaDTO[]> {
  return apiFetcher('/api/usuarios/adminsdto')
}

export async function getUsersRolDTO(empresaId: number, rol: string): Promise<EmpresaDTO[]> {
  const url = `/api/usuarios/usersRol?empresaId=${encodeURIComponent(String(empresaId))}&rol=${encodeURIComponent(
    rol,
  )}`
  return apiFetcher(url)
}

export default {
  registerAdmin,
  registerUser,
  requestRegister,
  verifyRegister,
  completeRegister,
  getAllUsuariosDTO,
  getUsuariosEmpresaDTO,
  getUsuariosDTO,
  getAdminsDTO,
  getUsersRolDTO,
}
