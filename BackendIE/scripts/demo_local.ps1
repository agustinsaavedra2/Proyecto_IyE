# Demo local (PowerShell) - script de demostración para la presentación
# Requisitos: Backend corriendo en http://localhost:8080
# Este script muestra auth/login, creación de entidad, lectura con filtros, actualización y eliminación.

$base = "http://localhost:8080"

function Call-Api {
    param($method, $url, $body = $null, $token = $null)
    $headers = @{"Accept" = "application/json"}
    if ($token) { $headers["Authorization"] = "Bearer $token" }
    if ($body -ne $null) {
        return Invoke-RestMethod -Method $method -Uri $url -Headers $headers -Body ($body | ConvertTo-Json -Compress) -ContentType 'application/json' -ErrorAction Stop
    } else {
        return Invoke-RestMethod -Method $method -Uri $url -Headers $headers -ErrorAction Stop
    }
}

Write-Host "=== DEMO LOCAL API ==="

# 1) Registrar Admin (si no existe) - usar /api/usuarios/registerAdmin
try {
    $admin = @{nombre = "Admin Demo"; email = "admin.demo@example.com"; password = "password123"}
    $resp = Call-Api -method Post -url "$base/api/usuarios/registerAdmin" -body $admin
    Write-Host "Admin creado:"; $resp
} catch {
    Write-Host "Registrar admin falló (posible que ya exista):" $_.Exception.Message
}

# 2) Login admin
$token = $null
try {
    $login = @{email = "admin.demo@example.com"; password = "password123"}
    $loginResp = Call-Api -method Post -url "$base/api/usuarios/login" -body $login
    $token = $loginResp.token
    Write-Host "Login OK. Token length:" $token.Length
} catch {
    Write-Host "Login falló:" $_.Exception.Message
}

# 3) Crear Empresa
try {
    $empresaBody = @{admin = 1; categoriaId = 1; nombre = "Demo S.A."; codigoEmpresa = "DEMO-01"; ubicacion = "Santiago"; descripcion = "Empresa demo"}
    $empresa = Call-Api -method Post -url "$base/api/empresas/registrar" -body $empresaBody -token $token
    Write-Host "Empresa creada:" ($empresa | ConvertTo-Json -Compress)
} catch {
    Write-Host "Crear empresa falló:" $_.Exception.Message
}

# 4) Listar empresas (sin filtro)
try {
    $all = Call-Api -method Get -url "$base/api/empresas/dto" -token $token
    Write-Host "Empresas (sin filtro):" ($all | ConvertTo-Json -Compress)
} catch {
    Write-Host "Listar empresas falló:" $_.Exception.Message
}

# 5) Listar empresas con header multitenant (X-Categoria-id=1)
try {
    $headers = @{"Authorization" = "Bearer $token"; "X-Categoria-id" = "1"}
    $res = Invoke-RestMethod -Method Get -Uri "$base/api/empresas/dto" -Headers $headers -ErrorAction Stop
    Write-Host "Empresas con X-Categoria-id=1:" ($res | ConvertTo-Json -Compress)
} catch {
    Write-Host "Listar con header falló:" $_.Exception.Message
}

# 6) Crear Riesgo (CRUD)
try {
    $riesgo = @{empresaId = 1; titulo = "Riesgo demo"; descripcion = "Demo"; categoria = "compliance"; probabilidad = "baja"; impacto = "bajo"; nivelRiesgo = "Bajo"}
    $r = Call-Api -method Post -url "$base/api/riesgos" -body $riesgo -token $token
    Write-Host "Riesgo creado:" ($r | ConvertTo-Json -Compress)
} catch {
    Write-Host "Crear riesgo falló:" $_.Exception.Message
}

# 7) Actualizar riesgo (si existe id)
try {
    if ($r -and $r.id) {
        $update = @{id = $r.id; nivelRiesgo = "Medio"}
        $updated = Call-Api -method Put -url "$base/api/riesgos/$($r.id)" -body $update -token $token
        Write-Host "Riesgo actualizado:" ($updated | ConvertTo-Json -Compress)
    }
} catch {
    Write-Host "Actualizar riesgo falló:" $_.Exception.Message
}

# 8) Eliminar riesgo
try {
    if ($r -and $r.id) {
        $del = Call-Api -method Delete -url "$base/api/riesgos/$($r.id)" -token $token
        Write-Host "Riesgo eliminado (respuesta):" ($del | ConvertTo-Json -Compress)
    }
} catch {
    Write-Host "Eliminar riesgo falló:" $_.Exception.Message
}

Write-Host "Demo script finalizado."

