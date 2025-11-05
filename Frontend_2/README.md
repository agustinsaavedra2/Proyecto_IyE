# ComplianceAI Frontend

Modern, AI-powered compliance management system frontend built with cutting-edge technologies.

## 🚀 Technologies Used

### Core Framework
- **Next.js 16** - React framework with App Router for server-side rendering and optimal performance
- **React 19** - Latest React with improved concurrent features and server components
- **TypeScript** - Type-safe development for better code quality and developer experience

### Styling & UI
- **Tailwind CSS v4** - Utility-first CSS framework for rapid UI development
- **Shadcn/ui** - High-quality, accessible component library built on Radix UI
- **Framer Motion** - Production-ready animation library for smooth, performant animations

### Data Management
- **SWR** - React Hooks for data fetching with built-in caching, revalidation, and optimistic UI
- **Fetch API** - Native browser API for HTTP requests to Spring Boot backend

### Design Features
- **Dark Theme** - Professional dark mode with neon/glow accents
- **Responsive Design** - Mobile-first approach with breakpoints for all screen sizes
- **Loading States** - Skeleton loaders and spinners for better UX
- **Smooth Animations** - Framer Motion for entrance animations, hover effects, and transitions
- **Glow Effects** - Custom CSS for neon-style borders and text effects

## 📁 Project Structure

\`\`\`
├── app/
│   ├── page.tsx              # Landing page
│   ├── dashboard/            # Dashboard pages
│   ├── regulations/          # Regulations management
│   ├── risks/                # Risk management
│   ├── audits/               # AI audit generation
│   └── auth/                 # Authentication pages
├── components/
│   ├── dashboard/            # Dashboard components
│   ├── regulations/          # Regulation components
│   ├── risks/                # Risk components
│   ├── audits/               # Audit components
│   ├── auth/                 # Auth forms
│   └── ui/                   # Reusable UI components
└── lib/
    └── utils.ts              # Utility functions
\`\`\`

## 🔌 Backend Integration

This frontend connects to a Spring Boot backend with the following endpoints:

- **Users**: `/api/usuarios` - User registration and management
- **Regulations**: `/api/regulaciones` - CRUD operations for regulations
- **Risks**: `/api/riesgos` - Risk management and AI generation
- **Audits**: `/api/ollama/crearAuditoria` - AI-powered audit generation
- **Protocols**: `/api/ollama/protocolo` - Protocol creation with AI
- **Subscriptions**: `/api/suscripcion` - Company subscription management

## 🛠️ Setup & Installation

1. **Install dependencies**:
\`\`\`bash
npm install
\`\`\`

2. **Configure backend URL**:
Update the API base URL in components (default: `http://localhost:8080`)

3. **Run development server**:
\`\`\`bash
npm run dev
\`\`\`

4. **Open browser**:
Navigate to `http://localhost:3000`

## 🎨 Key Features

### Performance Optimizations
- Server-side rendering with Next.js App Router
- Automatic code splitting
- Image optimization
- SWR caching for reduced API calls

### User Experience
- Instant loading states with skeleton screens
- Optimistic UI updates
- Smooth page transitions
- Responsive design for all devices

### Animations
- Entrance animations with staggered delays
- Hover effects on interactive elements
- Loading spinners for async operations
- Glow effects on primary actions

### Accessibility
- Semantic HTML structure
- ARIA labels and roles
## ComplianceAI Frontend (Guía en español)

Proyecto frontend para la gestión de cumplimiento (Compliance) con soporte de IA.

### Tecnologías principales

- Next.js 16 (App Router)
- React 19
- TypeScript
- Tailwind CSS v4
- SWR para fetching y caching
- Framer Motion para animaciones

## Estructura del proyecto

Directorio relevante:

```
app/            # Rutas y páginas (App Router)
components/     # Componentes UI reutilizables
    ├─ auth/       # Formularios de autenticación
    ├─ regulations/
    └─ risks/
lib/            # Utilidades (por ahora: utils.ts para clases)
```

## Conexión con el backend (Spring Boot)

Este frontend se comunica con un backend en Spring Boot. Las rutas que el proyecto usa son (rutas relativas en el servidor):

- Usuarios: `/api/usuarios`
- Regulaciones: `/api/regulaciones`
- Riesgos: `/api/riesgos`
- Auditorías (IA): `/api/ollama/crearAuditoria`
- Protocolos (IA): `/api/ollama/protocolo`
- Suscripciones: `/api/suscripcion`

Recomendación: centraliza la URL base del backend en una sola variable de entorno y un helper para evitar cadenas hardcodeadas por todo el proyecto.

### Archivo de entorno (.env)

Usa variables de entorno. En Next.js pon variables locales en `.env.local` (no subir al repositorio). Ejemplo mínimo:

```
# .env.local (para desarrollo)
NEXT_PUBLIC_API_URL=http://localhost:8080

# Variables que no deben ser públicas (ejemplo) - NO debe llevar NEXT_PUBLIC_
# API_SECRET_KEY=mi_clave_secreta
```

Notas importantes:
- Las variables que empiezan con `NEXT_PUBLIC_` estarán disponibles en el navegador (cliente). No pongas secretos allí.
- Guarda secretos (JWT_SECRET, claves de terceros, credenciales de DB) en variables sin el prefijo `NEXT_PUBLIC_` y accede a ellas sólo desde código que corre en el servidor.
- Nunca subas `.env.local` ni archivos con credenciales al control de versiones. Añade `.env*` en `.gitignore` (ya está en el repositorio).

### Dónde poner la URL base y los helpers API

Actualmente hay componentes con URLs completas hardcodeadas. Reemplázalos por un helper central. Archivos detectados con `http://localhost:8080`:

- `components/risks/risks-list.tsx`
- `components/regulations/regulations-list.tsx`
- `components/auth/register-form.tsx`

Recomendación minimalista: crea `lib/api.ts` con algo así (ejemplo):

```ts
// lib/api.ts
export const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080'

export async function apiFetcher(path: string, options?: RequestInit) {
    const res = await fetch(`${API_BASE}${path}`, {
        headers: { 'Content-Type': 'application/json' },
        ...options,
    })
    if (!res.ok) throw new Error(await res.text())
    return res.json()
}

// Usos ejemplos:
// useSWR('/api/riesgos', (url) => apiFetcher(url))
// await apiFetcher('/api/usuarios/registerAdmin', { method: 'POST', body: JSON.stringify(data) })
```

Si no quieres crear `lib/api.ts`, al menos reemplaza las URLs hardcodeadas por `process.env.NEXT_PUBLIC_API_URL` en los componentes.

Ejemplo de reemplazo en componente con SWR:

```ts
// antes
// useSWR("http://localhost:8080/api/riesgos", fetcher)

// después (usando helper)
import { API_BASE } from '@/lib/api'
useSWR(`${API_BASE}/api/riesgos`, fetcher)

// o, mejor aún, usar el fetcher centralizado:
useSWR('/api/riesgos', (url) => apiFetcher(url))
```

## Cómo configurar y ejecutar (desarrollo)

1) Instala dependencias:

```bash
npm install
```

2) Crea `.env.local` en la raíz del proyecto y define al menos `NEXT_PUBLIC_API_URL` apuntando a tu backend local:

```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

También puedes usar el archivo de ejemplo `.env.example` incluido en este repositorio: copia el archivo a `.env.local` y actualiza los valores:

```
cp .env.example .env.local
# (Windows PowerShell)
Copy-Item .env.example -Destination .env.local
```

3) Ejecuta la app en modo desarrollo:

```bash
npm run dev
```

4) Abre en el navegador: `http://localhost:3000`

## Buenas prácticas y seguridad

- NO poner secretos en variables `NEXT_PUBLIC_`.
- Mantén las llamadas al backend en un único helper (`lib/api.ts`) para poder centralizar cabeceras, manejo de errores y autenticación.
- Validar y sanitizar entradas en el backend (esta guía asume que el backend maneja validaciones y seguridad).

## Qué editar si el backend cambia

- Si cambian rutas, actualiza `lib/api.ts` o la variable `NEXT_PUBLIC_API_URL`.
- Para endpoints concretos, actualiza sólo el helper; los componentes deben usar rutas relativas con el helper (ej. `/api/riesgos`).

## Endpoints útiles (recordatorio)

- POST `/api/usuarios/registerAdmin` - registro (ejemplo usado en `components/auth/register-form.tsx`)
- GET `/api/regulaciones` - listado de regulaciones (`components/regulations/regulations-list.tsx`)
- GET `/api/riesgos` - listado de riesgos (`components/risks/risks-list.tsx`)
- POST `/api/ollama/crearAuditoria` - crear auditoría (IA)
- POST `/api/ollama/protocolo` - crear protocolo (IA)

## Desarrollo y producción

- Para desarrollo usa `.env.local`.
- Para producción usa `.env.production` o el sistema de variables de tu proveedor de hosting.

## Recursos

- Next.js: https://nextjs.org/docs
- SWR: https://swr.vercel.app

---

Resumen: he traducido y ampliado el README para que sea claro cómo conectar al backend, qué variables poner en un archivo `.env`, qué NO publicar y dónde centralizar las llamadas al API (recomiendo crear `lib/api.ts` y reemplazar las URLs hardcodeadas en los componentes listados arriba).
