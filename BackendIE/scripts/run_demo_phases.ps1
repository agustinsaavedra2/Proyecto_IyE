<#
run_demo_phases.ps1

Orquesta una demostración en dos fases para la colección Postman:
 - Fase 1 (fallos esperados): ejecuta la colección SIN token (debe dar 401/errores para los endpoints protegidos)
 - Fase 2 (éxitos esperados): obtiene un JWT con credenciales seed y ejecuta la colección CON token

Genera reportes JSON en `reports/demo_newman_phase1.json` y `reports/demo_newman_phase2.json`.

Uso:
    .\scripts\run_demo_phases.ps1 -BaseUrl "http://localhost:8080" -LoginEmail "maria.lopez@..." -LoginPassword "password123"

Opciones:
    -BaseUrl         URL base del backend (default se intenta leer de .env o http://localhost:8080)
    -LoginEmail      Email del usuario para login (por defecto: maria.lopez@bancocontinental.com)
    -LoginPassword   Password para login (por defecto: password123)
    -NoPause         No pausar entre fases
#>
param(
    [string]$BaseUrl,
    [string]$LoginEmail = "maria.lopez@bancocontinental.com",
    [string]$LoginPassword = "password123",
    [switch]$NoPause
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$projectRoot = Resolve-Path (Join-Path $scriptDir "..")
$envPath = Join-Path $projectRoot ".env"
$collectionPath = Join-Path $scriptDir "postman_collection_full.json"
$reportsDir = Join-Path $projectRoot "reports"

if (-not (Test-Path $collectionPath)) {
    Write-Host "Colección no encontrada en: $collectionPath" -ForegroundColor Red
    exit 1
}

# Leer .env (si existe)
$env = @{}
if (Test-Path $envPath) {
    Get-Content $envPath | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq '' -or $line.StartsWith('#')) { return }
        if ($line -notmatch '=') { return }
        $parts = $line -split '=',2
        $k = $parts[0].Trim()
        $v = $parts[1].Trim()
        if ($v.StartsWith('"') -and $v.EndsWith('"')) { $v = $v.Substring(1, $v.Length-2) }
        if ($v.StartsWith("'") -and $v.EndsWith("'")) { $v = $v.Substring(1, $v.Length-2) }
        $env[$k] = $v
    }
}

if (-not $BaseUrl) {
    $BaseUrl = $env['BASE_URL'] -or $env['BASEURL'] -or $env['APP_BASE_URL'] -or 'http://localhost:8080'
}

# Ensure reports folder
if (-not (Test-Path $reportsDir)) { New-Item -ItemType Directory -Path $reportsDir | Out-Null }
$reportPhase1 = Join-Path $reportsDir "demo_newman_phase1.json"
$reportPhase2 = Join-Path $reportsDir "demo_newman_phase2.json"

# Find newman launcher (prefer newman.cmd or npx)
function Get-NewmanLauncher {
    $cmd = Get-Command newman -ErrorAction SilentlyContinue
    if ($cmd) {
        $source = $cmd.Source
        try {
            $dir = [System.IO.Path]::GetDirectoryName($source)
            $cmdShim = [System.IO.Path]::Combine($dir, 'newman.cmd')
            if (Test-Path $cmdShim) { return @{ FilePath = $cmdShim; PrefixArgs = @() } }
        } catch { }
        if ($source -and $source.ToLower().EndsWith('.ps1')) {
            $ps = (Get-Command powershell -ErrorAction SilentlyContinue).Source
            return @{ FilePath = $ps; PrefixArgs = @('-NoProfile','-ExecutionPolicy','Bypass','-File',$source) }
        }
        return @{ FilePath = $source; PrefixArgs = @() }
    }
    $cmd = Get-Command npx -ErrorAction SilentlyContinue
    if ($cmd) { return @{ FilePath = $cmd.Source; PrefixArgs = @('newman') } }
    return $null
}

$launcher = Get-NewmanLauncher
if (-not $launcher) {
    Write-Host "newman o npx no encontrado en PATH. Instalar con: npm install -g newman newman-reporter-htmlextra" -ForegroundColor Red
    exit 1
}

function Run-NewmanJson($envVars, $outJsonPath) {
    # envVars: hashtable of env vars to pass
    $args = @('run', "`"$collectionPath`"")
    foreach ($k in $envVars.Keys) {
        $v = $envVars[$k]
        $args += '--env-var'
        $args += "`"$($k)=$($v)`""
    }
    # reporters -> json export
    $args += '--reporters'
    $args += 'cli,json'
    $args += '--reporter-json-export'
    $args += "`"$outJsonPath`""

    $allArgs = @()
    if ($launcher.PrefixArgs) { $allArgs += $launcher.PrefixArgs }
    $allArgs += $args

    Write-Host "Ejecutando: $($launcher.FilePath) $($allArgs -join ' ')"

    $outFile = [System.IO.Path]::GetTempFileName()
    $errFile = [System.IO.Path]::GetTempFileName()
    try {
        $proc = Start-Process -FilePath $launcher.FilePath -ArgumentList $allArgs -RedirectStandardOutput $outFile -RedirectStandardError $errFile -NoNewWindow -PassThru -Wait
    } catch {
        Write-Host "No se pudo iniciar newman: $_" -ForegroundColor Red
        return @{ ExitCode = 1; StdOut = ''; StdErr = $_ }
    }
    $stdout = ''
    $stderr = ''
    if (Test-Path $outFile) { $stdout = Get-Content $outFile -Raw -ErrorAction SilentlyContinue }
    if (Test-Path $errFile) { $stderr = Get-Content $errFile -Raw -ErrorAction SilentlyContinue }
    if (Test-Path $outFile) { Remove-Item $outFile -ErrorAction SilentlyContinue }
    if (Test-Path $errFile) { Remove-Item $errFile -ErrorAction SilentlyContinue }

    return @{ ExitCode = $proc.ExitCode; StdOut = $stdout; StdErr = $stderr }
}

# Do-Request: helper para llamadas HTTP con manejo de errores y extracción de status
function Do-Request($method, $path, $body, $headers) {
    $url = $BaseUrl.TrimEnd('/') + $path
    try {
        if ($method -eq 'GET') {
            $resp = Invoke-RestMethod -Uri $url -Method Get -Headers $headers -UseBasicParsing -TimeoutSec 30 -ErrorAction Stop
            return @{ status = 200; body = $resp }
        } else {
            $json = $null
            if ($body -ne $null) { $json = $body | ConvertTo-Json -Depth 10 }
            $resp = Invoke-RestMethod -Uri $url -Method $method -Headers $headers -Body $json -ContentType 'application/json' -UseBasicParsing -TimeoutSec 60 -ErrorAction Stop
            return @{ status = 200; body = $resp }
        }
    } catch {
        $status = $null
        $desc = $null
        # extraer detalles si la excepción tiene Response
        try {
            if ($_.Exception.Response -ne $null) {
                $respObj = $_.Exception.Response
                if ($respObj.StatusCode -ne $null) { $status = [int]$respObj.StatusCode.Value__ }
                try { $reader = New-Object System.IO.StreamReader($respObj.GetResponseStream()); $desc = $reader.ReadToEnd() } catch {}
            }
        } catch { }
        return @{ status = $status; error = $_.Exception.Message; responseDescription = $desc }
    }
}

function Print-Step($title, $dataUsed, $error, $successMessage) {
    Write-Host "`n=================================================" -ForegroundColor DarkCyan
    Write-Host "Paso: $title" -ForegroundColor Cyan
    Write-Host "Datos usados: $dataUsed"
    if ($error -and $error -ne '') { Write-Host "Error: $error" -ForegroundColor Red } else { Write-Host "Error: -" }
    if ($successMessage -and $successMessage -ne '') { Write-Host "Acierto: $successMessage" -ForegroundColor Green } else { Write-Host "Acierto: -" }
    Write-Host "=================================================`n" -ForegroundColor DarkCyan
}

Write-Host "Demo en dos fases:" -ForegroundColor Cyan
Write-Host "  BaseUrl: $BaseUrl" -ForegroundColor Cyan
Write-Host "  LoginEmail: $LoginEmail" -ForegroundColor Cyan
Write-Host "Reportes generados en: $reportPhase1 y $reportPhase2`n" -ForegroundColor Cyan

# Fase 1: Ejecutar sin token (fallos esperados en endpoints protegidos)
Write-Host "=== FASE 1: Ejecutando colección SIN token (fallos esperados) ===" -ForegroundColor Yellow
$env1 = @{ baseUrl = $BaseUrl; empresaId = 1; riesgoId = 1 }
$res1 = Run-NewmanJson -envVars $env1 -outJsonPath $reportPhase1
Write-Host $res1.StdOut
if ($res1.StdErr) { Write-Host $res1.StdErr -ForegroundColor Red }
Write-Host "Fase 1 ExitCode: $($res1.ExitCode)`n"
if (-not $NoPause) { Read-Host "Presiona ENTER para continuar a la Fase 2 (o Ctrl+C para cancelar)" }

# Fase 2: Obtener token y ejecutar con token (éxitos esperados)
Write-Host "=== FASE 2: Obteniendo token y ejecutando colección CON token (éxitos esperados) ===" -ForegroundColor Yellow
try {
    $loginPayload = @{ email = $LoginEmail; password = $LoginPassword } | ConvertTo-Json
    Write-Host "Login: POST $BaseUrl/api/usuarios/login"
    $resp = Invoke-RestMethod -Uri "$BaseUrl/api/usuarios/login" -Method Post -Body $loginPayload -ContentType 'application/json' -TimeoutSec 20 -ErrorAction Stop
    if ($null -eq $resp) { throw "Respuesta de login vacía" }
    $token = $resp.token
    if (-not $token) { throw "No se recibió token en la respuesta de login" }
    Write-Host "Token obtenido: $(if ($token) { $token.Substring(0,30) + '...' } else { '<none>' })"

    # Obtener lista de usuarios para encontrar un adminId válido
    try {
        $users = Invoke-RestMethod -Uri "$BaseUrl/api/usuarios/users" -Method Get -Headers @{ Authorization = "Bearer $token" } -TimeoutSec 10 -ErrorAction Stop
        # users debería ser una lista de objetos con 'id' y 'rol'
        $adminUser = $null
        foreach ($u in $users) {
            if ($u.rol -and $u.rol -eq 'admin') { $adminUser = $u; break }
        }
        if ($adminUser -ne $null) { $adminId = $adminUser.id } else { $adminId = 1 }
        Write-Host "Admin encontrado: $adminId"
    } catch {
        Write-Host "No se pudo obtener lista de usuarios para determinar adminId: $_" -ForegroundColor Yellow
        $adminId = 1
    }

    # --- NUEVO: Crear admin dinámico para la demo (si quieres forzar un admin nuevo) ---
    $timestamp = [int][double]::Parse((Get-Date -UFormat %s))
    $dynamicAdminEmail = "admin.demo.$timestamp@example.com"
    $dynamicAdminPayload = @{ nombre = "Admin Demo $timestamp"; email = $dynamicAdminEmail; password = "Pass!$timestamp" }
    Write-Host "Creando admin dinámico: $dynamicAdminEmail"
    $rAdmin = Do-Request -method 'POST' -path '/api/usuarios/registerAdmin' -body $dynamicAdminPayload -headers @{ Authorization = "Bearer $token" }
    if ($rAdmin.status -and $rAdmin.status -ge 200 -and $rAdmin.status -lt 300) {
        try { $newAdminId = $rAdmin.body.id } catch { $newAdminId = $null }
        if (-not $newAdminId) { try { $newAdminId = $rAdmin.body | Select-Object -ExpandProperty id -ErrorAction SilentlyContinue } catch { } }
        if ($newAdminId) { Print-Step 'Crear Admin Dinámico' (ConvertTo-Json $dynamicAdminPayload -Compress) '' ("Admin dinámico creado: $newAdminId") } else { Print-Step 'Crear Admin Dinámico' (ConvertTo-Json $dynamicAdminPayload -Compress) 'No se pudo extraer id del admin creado' 'Admin creado (id no extraido)' }
        $adminId = $newAdminId -or $adminId
    } else {
        $err = "Status=$($rAdmin.status); resp=$($rAdmin.responseDescription)"
        Print-Step 'Crear Admin Dinámico' (ConvertTo-Json $dynamicAdminPayload -Compress) $err ''
        Write-Host "No se pudo crear admin dinámico (status: $($rAdmin.status)). Se usará adminId detectado: $adminId" -ForegroundColor Yellow
    }

    # Obtener categoría para asignar (fallback a 1)
    $categoriaId = 1
    try {
        $cats = Invoke-RestMethod -Uri "$BaseUrl/api/categorias/listar" -Method Get -Headers @{ Authorization = "Bearer $token" } -TimeoutSec 10 -ErrorAction Stop
        if ($cats -and $cats.Count -gt 0) { $categoriaId = $cats[0].id }
    } catch { $categoriaId = 1 }

    # Crear empresa con adminId
    $empresaPayload = @{ admin = $adminId; categoriaId = $categoriaId; nombre = "Demo Empresa $timestamp"; codigoEmpresa = "DEMO-$timestamp"; ubicacion = "Santiago"; descripcion = "Empresa automática para demo" }
    Write-Host "Creando empresa con adminId=$adminId, categoriaId=$categoriaId"
    $rEmpresa = Do-Request -method 'POST' -path '/api/empresas/registrar' -body $empresaPayload -headers @{ Authorization = "Bearer $token" }
    if ($rEmpresa.status -and $rEmpresa.status -ge 200 -and $rEmpresa.status -lt 300) {
        try { $empresaId = $rEmpresa.body.id } catch { $empresaId = $null }
        if (-not $empresaId) { try { $empresaId = $rEmpresa.body | Select-Object -ExpandProperty id -ErrorAction SilentlyContinue } catch {} }
        Print-Step 'Crear Empresa' (ConvertTo-Json $empresaPayload -Compress) '' ("Empresa creada: $empresaId")
    } else {
        $err = "status=$($rEmpresa.status) resp=$($rEmpresa.responseDescription)"
        Print-Step 'Crear Empresa' (ConvertTo-Json $empresaPayload -Compress) $err ''
        Write-Host "Fallo al crear empresa: status=$($rEmpresa.status) resp=$($rEmpresa.responseDescription)" -ForegroundColor Red
        throw "No se pudo crear empresa"
    }

    # Suscribirse para activar la empresa (usa el controller /api/suscripcion/suscribirse)
    $susPayload = @{ empresaId = $empresaId; plan = "Estándar"; adminId = $adminId }
    Write-Host "Suscribiendo empresa $empresaId al plan Estándar para activar..."
    $rSus = Do-Request -method 'POST' -path '/api/suscripcion/suscribirse' -body $susPayload -headers @{ Authorization = "Bearer $token" }
    if ($rSus.status -and $rSus.status -ge 200 -and $rSus.status -lt 300) {
        Print-Step 'Suscribir y Activar Empresa' (ConvertTo-Json $susPayload -Compress) '' 'Suscripción creada correctamente (empresa activada)'
    } else {
        $err = "status=$($rSus.status) resp=$($rSus.responseDescription)"
        Print-Step 'Suscribir y Activar Empresa' (ConvertTo-Json $susPayload -Compress) $err ''
        Write-Host "Fallo al crear suscripción: status=$($rSus.status) resp=$($rSus.responseDescription)" -ForegroundColor Yellow
    }

    # Crear usuario (agregar reporte estructurado)
    $userPayload = @{ empresaId = $empresaId; nombre = "Usuario Demo"; email = "usuario.demo+$timestamp@example.com"; password = "password123"; rol = "complianceofficer"; adminId = $adminId }
    Write-Host "Creando usuario con email: $($userPayload.email)"
    $rUser = Do-Request -method 'POST' -path '/api/usuarios/registerUser' -body $userPayload -headers @{ Authorization = "Bearer $token" }
    if ($rUser.status -and $rUser.status -ge 200 -and $rUser.status -lt 300) {
        try { $usuarioId = $rUser.body.id } catch { $usuarioId = $null }
        if (-not $usuarioId) { try { $usuarioId = $rUser.body | Select-Object -ExpandProperty id -ErrorAction SilentlyContinue } catch {} }
        if ($usuarioId) { Print-Step 'Crear Usuario' (ConvertTo-Json $userPayload -Compress) '' ("Usuario creado: $usuarioId") } else { Print-Step 'Crear Usuario' (ConvertTo-Json $userPayload -Compress) 'Usuario creado pero no se pudo extraer id' '' }
    } else {
        $err = "status=$($rUser.status) resp=$($rUser.responseDescription)"
        Print-Step 'Crear Usuario' (ConvertTo-Json $userPayload -Compress) $err ''
    }

} catch {
    Write-Host "Error al obtener token: $_" -ForegroundColor Red
    Write-Host "No se podrá ejecutar la Fase 2 sin token. Revisa credenciales o el endpoint /api/usuarios/login" -ForegroundColor Red
    exit 2
}

# Ejecutar newman con token
$env2 = @{ baseUrl = $BaseUrl; token = $token; empresaId = $empresaId; riesgoId = 1 }
# Incluir adminId detectado para la colección
$env2['adminId'] = $adminId
# Si usuarioId fue creado úsalo; sino usa adminId como fallback para las requests que requieren usuarioId
if ($null -ne $usuarioId -and $usuarioId -ne '') { $env2['usuarioId'] = $usuarioId } else { $env2['usuarioId'] = $adminId }
$res2 = Run-NewmanJson -envVars $env2 -outJsonPath $reportPhase2
Write-Host $res2.StdOut
if ($res2.StdErr) { Write-Host $res2.StdErr -ForegroundColor Red }
Write-Host "Fase 2 ExitCode: $($res2.ExitCode)`n"

# Mostrar breve resumen de los reportes (status codes por request) con formato amigable
function Summarize-Report($jsonPath, [string]$phaseName = '') {
    if (-not (Test-Path $jsonPath)) { Write-Host "Reporte $jsonPath no encontrado" -ForegroundColor Yellow; return }
    $j = Get-Content $jsonPath -Raw | ConvertFrom-Json
    if ($phaseName -ne '') {
        Write-Host "=== $phaseName ===" -ForegroundColor Yellow
    }
    $label = $phaseName
    if ([string]::IsNullOrEmpty($label)) { $label = $jsonPath }
    Write-Host ("Resumen {0}:" -f $label) -ForegroundColor Green
    foreach ($exec in $j.run.executions) {
        $name = $exec.item.name
        $code = $null
        try { $code = $exec.response.code } catch { try { $code = $exec.response.status } catch { $code = 0 } }
        if (-not $code) { $code = 0 }
        if ($code -ge 200 -and $code -lt 300) { $statusText = 'OK' } else { $statusText = 'FAIL' }
        $reason = ''
        try {
            $body = ''
            if ($exec.response -and $exec.response.body) { $body = $exec.response.body }
            elseif ($exec.response -and $exec.response.stream) { $body = ($exec.response.stream | Out-String) }
            if ($body -and $body -ne '') {
                try {
                    $bjson = $body | ConvertFrom-Json -ErrorAction SilentlyContinue
                    if ($bjson -ne $null) {
                        if ($bjson.error) { $reason = $bjson.error }
                        elseif ($bjson.message) { $reason = $bjson.message }
                        elseif ($bjson.timestamp -and $bjson.status -and $bjson.error) { $reason = $bjson.error }
                    } else {
                        $reason = ($body -replace "\r|\n"," ").Substring(0,[Math]::Min(120,$body.Length))
                    }
                } catch { $reason = ($body -replace "\r|\n"," ").Substring(0,[Math]::Min(120,$body.Length)) }
            }
        } catch { $reason = '' }
        if ($reason -and $reason -ne '') {
            Write-Host (" - {0} -> {1} ({2}) - {3}" -f $name, $code, $statusText, $reason)
        } else {
            Write-Host (" - {0} -> {1} ({2})" -f $name, $code, $statusText)
        }
    }
    Write-Host "`n"
}

# Reemplazar llamadas anteriores para imprimir con nombre de fase
Summarize-Report -jsonPath $reportPhase1 -phaseName 'FASE 1'
Summarize-Report -jsonPath $reportPhase2 -phaseName 'FASE 2'

Write-Host "Demo finalizada. Archivos: $reportPhase1 , $reportPhase2" -ForegroundColor Cyan

# Fase 3: Secuencia robusta de 10 endpoints mayormente de lectura para demostrar éxitos
Write-Host "`n=== FASE 3: Secuencia robusta (10 requests mayormente GET) ===" -ForegroundColor Yellow
$results = @()

# Reusar Do-Request definido arriba
# (la función Do-Request ya existe más arriba y maneja errores)

# Prepare headers
$authHeader = @{ Authorization = "Bearer $token" }
$tenantHeaderName = ($env['APP_MULTITENANT_HEADER'] -or 'X-Categoria-id')

# 1) Actuator health
$r = Do-Request -method 'GET' -path '/actuator/health' -body $null -headers @{}
$results += @{ name='actuator/health'; path='/actuator/health'; status=$r.status; resp=$r }

# 2) Listar categorias DTO (GET)
$r = Do-Request -method 'GET' -path '/api/categorias/dto' -body $null -headers $authHeader
$results += @{ name='Listar Categorias DTO'; path='/api/categorias/dto'; status=$r.status; resp=$r }

# 3) Listar todas las categorias (listar)
$r = Do-Request -method 'GET' -path '/api/categorias/listar' -body $null -headers $authHeader
$results += @{ name='Listar Todas Categorias'; path='/api/categorias/listar'; status=$r.status; resp=$r }

# 4) Listar empresas resumen (GET)
$r = Do-Request -method 'GET' -path '/api/empresas/resumen' -body $null -headers $authHeader
$results += @{ name='Listar Empresas Resumen'; path='/api/empresas/resumen'; status=$r.status; resp=$r }

# 5) Listar usuarios (GET)
$r = Do-Request -method 'GET' -path '/api/usuarios/users' -body $null -headers $authHeader
$results += @{ name='Listar Usuarios'; path='/api/usuarios/users'; status=$r.status; resp=$r }

# 6) Listar riesgos (GET)
$r = Do-Request -method 'GET' -path '/api/riesgos' -body $null -headers $authHeader
$results += @{ name='Listar Riesgos'; path='/api/riesgos'; status=$r.status; resp=$r }

# 7) Crear riesgo (POST) - debe funcionar con datos seed
$useEmpresaId = 1
$riesgoPayload = @{ empresaId = $useEmpresaId; titulo = 'Riesgo demo'; descripcion = 'Demo'; categoria = 'cumplimiento'; probabilidad = 'media'; impacto = 'medio'; nivelRiesgo = 'Medio' }
$r = Do-Request -method 'POST' -path '/api/riesgos' -body $riesgoPayload -headers $authHeader
if ($r.status -eq 200 -and $r.body -ne $null) { try { $riesgoId = $r.body.id } catch { $riesgoId = $null } }
$results += @{ name='Crear Riesgo'; path='/api/riesgos'; status=$r.status; resp=$r }

# 8) Listar regulaciones (GET)
$r = Do-Request -method 'GET' -path '/api/regulaciones/' -body $null -headers $authHeader
$results += @{ name='Listar Regulaciones'; path='/api/regulaciones/'; status=$r.status; resp=$r }

# 9) Listar respuestas Ollama (GET)
$r = Do-Request -method 'GET' -path '/api/ollama' -body $null -headers $authHeader
$results += @{ name='Listar OllamaResponses'; path='/api/ollama'; status=$r.status; resp=$r }

# 10) Listar auditorias (GET)
$r = Do-Request -method 'GET' -path '/api/auditorias' -body $null -headers $authHeader
$results += @{ name='Listar Auditorias'; path='/api/auditorias'; status=$r.status; resp=$r }

# Guardar resultados en JSON
$manualReport = @{ runAt = (Get-Date).ToString(); baseUrl = $BaseUrl; results = $results }
$manualReportPath = Join-Path $reportsDir 'demo_manual_phase_success.json'
$manualReport | ConvertTo-Json -Depth 10 | Set-Content -Path $manualReportPath -Encoding UTF8

# Mostrar resumen
Write-Host "Resumen Fase 3 (robusta):" -ForegroundColor Green
foreach ($item in $results) {
    $nm = $item.name
    $st = $item.status
    $statText = if ($st -ge 200 -and $st -lt 300) { 'OK' } else { 'FAIL' }
    Write-Host (" - {0} -> {1} ({2})" -f $nm, $st, $statText)
}
Write-Host "Fase 3 JSON: $manualReportPath" -ForegroundColor Cyan

Write-Host "Demo finalizada. Archivos: $reportPhase1 , $reportPhase2, $manualReportPath" -ForegroundColor Cyan
