// Helper centralizado para llamadas al API
// @ts-ignore - allow using process.env in this runtime-aware helper
export const API_BASE = process.env.NEXT_PUBLIC_API_URL

export class ApiError extends Error {
  status: number
  body: string | object | null

  constructor(status: number, body: string | object | null, message?: string) {
    super(message || `API Error ${status}`)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

export async function apiFetcher(path: string, options?: RequestInit) {
  // If API_BASE is not set, on the browser we'll fall back to a relative path
  // so the app can call the same-origin API routes. On the server we require
  // the env var to be set (backend host) and will throw a helpful error.
  let base = API_BASE
  if (!base) {
    if (typeof window !== 'undefined') {
      // client runtime: allow relative calls and warn
      // e.g. calling '/api/...' will go to the same host where frontend runs
      // which is convenient for local development when using a proxy or same-origin API.
      // eslint-disable-next-line no-console
      console.warn('NEXT_PUBLIC_API_URL is not set — using relative paths for apiFetcher')
      base = ''
    } else {
      throw new Error('NEXT_PUBLIC_API_URL is not set. Set it in your environment or .env.local')
    }
  }

  const url = `${base}${path}`

  // Client detection to allow localStorage and console debug only in browser
  const isClient = typeof window !== 'undefined'

  let res: Response
  try {
    const defaultHeaders = { 'Content-Type': 'application/json' }
    const providedHeaders = (options && (options as any).headers) || {}
    const headers = { ...defaultHeaders, ...providedHeaders }

    // Public API paths that must not carry Authorization
    const publicPaths = [
      '/api/usuarios/request-register',
      '/api/usuarios/login',
      '/api/usuarios/verify-register',
      '/api/usuarios/complete-register',
      '/auth/login',
      '/auth/refresh',
      '/auth/logout'
    ]
    const isPublic = publicPaths.includes(path) || path.startsWith('/actuator')

    // Remove Authorization header if present but falsy/empty
    const existingAuthKey = Object.keys(headers).find(k => k.toLowerCase() === 'authorization')
    if (existingAuthKey) {
      const val = headers[existingAuthKey]
      if (!val || String(val).trim() === '' || String(val).trim().toLowerCase() === 'bearer') {
        delete headers[existingAuthKey]
      }
    }

    // When running in the browser, automatically attach a stored JWT if present
    if (isClient) {
      try {
        const token = localStorage.getItem('token')
        const hasAuth = Object.keys(headers).some(k => k.toLowerCase() === 'authorization')
        if (token && !hasAuth && !isPublic) {
          headers['Authorization'] = `Bearer ${token}`
        }
      } catch (e) {
        // ignore localStorage issues
      }
    }

    if (isClient) console.debug('[apiFetcher] fetch', url, { ...options, headers })

    res = await fetch(url, {
      headers,
      ...options,
    })
  } catch (err: any) {
    // Network/connection error — wrap in ApiError with status 0 so callers can
    // detect network failures separately from HTTP errors.
    // eslint-disable-next-line no-console
    console.error('[apiFetcher] network error', err)
    throw new ApiError(0, null, err?.message || 'Network error')
  }

  const text = await res.text()

  if (!res.ok) {
    let parsedBody: string | object | null = null
    try {
      parsedBody = text ? JSON.parse(text) : null
    } catch (e) {
      parsedBody = text
    }
    // attach status and body to the error for easier debugging in callers
    throw new ApiError(res.status, parsedBody, typeof parsedBody === 'string' ? parsedBody : undefined)
  }

  const contentType = res.headers.get('content-type')
  if (contentType && contentType.includes('application/json')) {
    try {
      return JSON.parse(text)
    } catch (e) {
      return text
    }
  }

  return text
}

export default apiFetcher
