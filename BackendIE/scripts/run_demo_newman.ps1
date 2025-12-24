<#
run_demo_newman.ps1

Descripción:
- Lee variables desde el archivo `.env` en la raíz del proyecto (formato KEY=VALUE)
- Permite sobreescribir la variable `token` pidiéndola por consola si no está en `.env`
- Verifica que `newman` esté instalado y ejecuta la colección `scripts/postman_collection.json`
- Genera un reporte HTML en `reports/demo_newman_report.html` si el reporter `htmlextra` está disponible, si no intenta con `html` o al menos con `cli`.

Uso:
- PowerShell: abrir en modo Administrador (si es necesario) y ejecutar:
  .\scripts\run_demo_newman.ps1

Opciones:
- Puedes pasar un token como parámetro: .\scripts\run_demo_newman.ps1 -Token "eyJ..."
- Puedes forzar que no genere HTML con -NoHtmlReport
#>
param(
    [string]$Token,
    [string]$BaseUrl,
    [string]$LoginEmail,
    [string]$LoginPassword,
    [switch]$NoHtmlReport
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$projectRoot = Resolve-Path (Join-Path $scriptDir "..")
$envPath = Join-Path $projectRoot ".env"
$collectionPath = Join-Path $scriptDir "postman_collection.json"
$reportsDir = Join-Path $projectRoot "reports"

if (-not (Test-Path $collectionPath)) {
    Write-Host "Colección no encontrada en: $collectionPath" -ForegroundColor Red
    exit 1
}

# Parse .env into a hashtable
$env = @{}
if (Test-Path $envPath) {
    Get-Content $envPath | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq "" -or $line.StartsWith('#')) { return }
        if ($line -notmatch '=') { return }
        $parts = $line -split '=',2
        $k = $parts[0].Trim()
        $v = $parts[1].Trim()
        # remove optional surrounding quotes
        if ($v.StartsWith('"') -and $v.EndsWith('"')) { $v = $v.Substring(1, $v.Length-2) }
        if ($v.StartsWith("'") -and $v.EndsWith("'")) { $v = $v.Substring(1, $v.Length-2) }
        $env[$k] = $v
    }
} else {
    Write-Host ".env no encontrado en $envPath. Continúo pidiendo variables manualmente." -ForegroundColor Yellow
}

# Resolve baseUrl and defaults
# Prefer explicit backend variables; do NOT use OLLAMA_BASE_URL here (it's the AI service)
$baseUrlCandidates = @($env['BASE_URL'], $env['BASEURL'], $env['APP_BASE_URL'])
$baseUrl = $baseUrlCandidates | Where-Object { $_ -and $_ -ne "" } | Select-Object -First 1
if (-not $baseUrl) { $baseUrl = "http://localhost:8080" }

# Allow override from script parameter
if ($BaseUrl) { $baseUrl = $BaseUrl }

# Quick health check on target baseUrl (actuator health) to avoid hitting wrong service
try {
    $healthUrl = $baseUrl.TrimEnd('/') + "/actuator/health"
    $resp = Invoke-WebRequest -Uri $healthUrl -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    if ($resp.StatusCode -ne 200) {
        Write-Host "Advertencia: $healthUrl devolvió código $($resp.StatusCode). Asegúrate que este es el backend correcto." -ForegroundColor Yellow
        $answer = Read-Host "Continuar de todos modos? (y/N)"
        if ($answer.ToLower() -ne 'y') { Write-Host "Cancelado por usuario."; exit 2 }
    }
} catch {
    Write-Host "Advertencia: no se pudo alcanzar $baseUrl (intentando $healthUrl). Puede que el backend no esté corriendo o el puerto sea distinto." -ForegroundColor Yellow
    $answer = Read-Host "Continuar de todos modos? (y/N)"
    if ($answer.ToLower() -ne 'y') { Write-Host "Cancelado por usuario."; exit 2 }
}

# Token resolution: parameter -> .env -> prompt
if (-not $Token) {
    if ($env.ContainsKey('TOKEN') -and $env['TOKEN']) { $Token = $env['TOKEN'] }
    elseif ($env.ContainsKey('TOKEN_SECRET') -and $env['TOKEN_SECRET']) { $Token = $env['TOKEN_SECRET'] }
}

# Try auto-login when credentials are available (params override .env)
if (-not $Token) {
    if (-not $LoginEmail) { $LoginEmail = $env['LOGIN_EMAIL'] -or $env['EMAIL'] -or $env['USER_EMAIL'] }
    if (-not $LoginPassword) { $LoginPassword = $env['LOGIN_PASSWORD'] -or $env['PASSWORD'] -or $env['USER_PASSWORD'] }

    if ($LoginEmail -and $LoginPassword) {
        Write-Host "Intentando login automático con $LoginEmail..."
        try {
            $loginPayload = @{ email = $LoginEmail; password = $LoginPassword } | ConvertTo-Json
            $loginUrl = $baseUrl.TrimEnd('/') + "/api/usuarios/login"
            $loginResp = Invoke-RestMethod -Uri $loginUrl -Method Post -Body $loginPayload -ContentType 'application/json' -TimeoutSec 15 -ErrorAction Stop
            if ($loginResp -and $loginResp.token) {
                $Token = $loginResp.token
                Write-Host "Login automático exitoso. Token obtenido." -ForegroundColor Green
            } else {
                Write-Host "Login automático no devolvió token. Continuando para pedir token manualmente." -ForegroundColor Yellow
            }
        } catch {
            Write-Host "Login automático falló: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
}

# If still no token, fall back to prompt (existing behavior)
if (-not $Token) {
    Write-Host "No se encontró token en parámetros ni en .env ni login automático. Ingresa token (ENTER para cancelar):"
    $Token = Read-Host "Token"
}

if (-not $Token) {
    Write-Host "Token no provisto. Ejecutando la colección sin token (algunas requests pueden fallar)." -ForegroundColor Yellow
}

# Other useful variables
#$empresaId = $env['empresaId'] -or $env['EMPRESA_ID'] -or 1
#$riesgoId = $env['riesgoId'] -or $env['RIESGO_ID'] -or 1
$empresaIdCandidates = @($env['empresaId'], $env['EMPRESA_ID'])
$empresaId = $empresaIdCandidates | Where-Object { $_ -and $_ -ne "" } | Select-Object -First 1
if (-not $empresaId) { $empresaId = 1 }
$riesgoIdCandidates = @($env['riesgoId'], $env['RIESGO_ID'])
$riesgoId = $riesgoIdCandidates | Where-Object { $_ -and $_ -ne "" } | Select-Object -First 1
if (-not $riesgoId) { $riesgoId = 1 }

# Ensure reports folder
if (-not (Test-Path $reportsDir)) { New-Item -ItemType Directory -Path $reportsDir | Out-Null }
# Default report paths
$reportHtml = Join-Path $reportsDir "demo_newman_report.html"
$reportJson = Join-Path $reportsDir "demo_newman_report.json"

# Detect which command to use to run newman: prefer 'newman', fall back to 'npx'
function Get-NewmanLauncher {
    $cmd = Get-Command newman -ErrorAction SilentlyContinue
    if ($cmd) {
        $source = $cmd.Source
        # On Windows, npm may install newman.ps1 and newman.cmd in the same folder. Prefer .cmd.
        try {
            $dir = [System.IO.Path]::GetDirectoryName($source)
            $cmdShim = [System.IO.Path]::Combine($dir, 'newman.cmd')
            if (Test-Path $cmdShim) { return @{ FilePath = $cmdShim; PrefixArgs = @() } }
        } catch {
            # ignore
        }
        # If the source is a .ps1, we can run it via powershell.exe
        if ($source -and $source.ToLower().EndsWith('.ps1')) {
            return @{ FilePath = (Get-Command powershell -ErrorAction SilentlyContinue).Source; PrefixArgs = @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $source) }
        }
        return @{ FilePath = $source; PrefixArgs = @() }
    }
    $cmd = Get-Command npx -ErrorAction SilentlyContinue
    if ($cmd) { return @{ FilePath = $cmd.Source; PrefixArgs = @('newman') } }
    return $null
}

$launcher = Get-NewmanLauncher
if (-not $launcher) {
    Write-Host "newman no está instalado o no está en PATH (ni npx disponible)." -ForegroundColor Red
    Write-Host "Instalar con: npm install -g newman newman-reporter-htmlextra" -ForegroundColor Yellow
    exit 1
}

# Prefer htmlextra reporter if available (only try if node exists). If not, check for 'html' reporter. If none, fallback to 'json' exporter.
$useHtmlExtra = $false
$useHtml = $false
$nodeCmd = Get-Command node -ErrorAction SilentlyContinue
if ($nodeCmd) {
    try {
        $nodeCheck = 'node -e "require.resolve(''newman-reporter-htmlextra'')"'
        $res = cmd /c $nodeCheck 2>$null
        if ($LASTEXITCODE -eq 0) { $useHtmlExtra = $true }
    } catch {
        $useHtmlExtra = $false
    }
    if (-not $useHtmlExtra) {
        try {
            $nodeCheck2 = 'node -e "require.resolve(''newman-reporter-html'')"'
            $res2 = cmd /c $nodeCheck2 2>$null
            if ($LASTEXITCODE -eq 0) { $useHtml = $true }
        } catch {
            $useHtml = $false
        }
    }
}

# Build newman args
$newmanArgs = @()
$newmanArgs += "run"
$newmanArgs += "`"$collectionPath`""
# pass variables
if ($Token) { $newmanArgs += "--env-var"; $newmanArgs += "`"token=$Token`"" }
if ($baseUrl) { $newmanArgs += "--env-var"; $newmanArgs += "`"baseUrl=$baseUrl`"" }
if ($empresaId) { $newmanArgs += "--env-var"; $newmanArgs += "`"empresaId=$empresaId`"" }
if ($riesgoId) { $newmanArgs += "--env-var"; $newmanArgs += "`"riesgoId=$riesgoId`"" }

# reporters and outputs
$newmanArgs += "--reporters"
if ($useHtmlExtra -and -not $NoHtmlReport) {
    $newmanArgs += "cli,htmlextra"
    $newmanArgs += "--reporter-htmlextra-export"; $newmanArgs += "`"$reportHtml`""
} elseif ($useHtml -and -not $NoHtmlReport) {
    $newmanArgs += "cli,html"
    $newmanArgs += "--reporter-html-export"; $newmanArgs += "`"$reportHtml`""
} elseif (-not $NoHtmlReport) {
    # Fallback to JSON reporter which is usually available
    $newmanArgs += "cli,json"
    $newmanArgs += "--reporter-json-export"; $newmanArgs += "`"$reportJson`""
} else {
    $newmanArgs += "cli"
}

Write-Host "Ejecutando newman con colección: $collectionPath"
Write-Host "baseUrl=$baseUrl empresaId=$empresaId riesgoId=$riesgoId"

# Run newman
# Use Start-Process with temporary files to capture stdout/stderr reliably on Windows
$outFile = [System.IO.Path]::GetTempFileName()
$errFile = [System.IO.Path]::GetTempFileName()
$allArgs = @()
if ($launcher.PrefixArgs) { $allArgs += $launcher.PrefixArgs }
$allArgs += $newmanArgs
try {
    $proc = Start-Process -FilePath $launcher.FilePath -ArgumentList $allArgs -RedirectStandardOutput $outFile -RedirectStandardError $errFile -NoNewWindow -PassThru -Wait
} catch {
    Write-Host "No se pudo iniciar el proceso para '$($launcher.FilePath)'. Revisa que esté en el PATH y que Node esté instalado." -ForegroundColor Red
    if (Test-Path $outFile) { Remove-Item $outFile -ErrorAction SilentlyContinue }
    if (Test-Path $errFile) { Remove-Item $errFile -ErrorAction SilentlyContinue }
    exit 1
}
$stdout = ""
$stderr = ""
if (Test-Path $outFile) { $stdout = Get-Content $outFile -Raw -ErrorAction SilentlyContinue }
if (Test-Path $errFile) { $stderr = Get-Content $errFile -Raw -ErrorAction SilentlyContinue }
if (Test-Path $outFile) { Remove-Item $outFile -ErrorAction SilentlyContinue }
if (Test-Path $errFile) { Remove-Item $errFile -ErrorAction SilentlyContinue }

Write-Host $stdout
if ($stderr) { Write-Host $stderr -ForegroundColor Red }

$exitCode = $null
try { $exitCode = $proc.ExitCode } catch { $exitCode = $LASTEXITCODE }

if ($exitCode -eq 0) {
    Write-Host "Newman finalizó correctamente." -ForegroundColor Green
    if (-not $NoHtmlReport) {
        if (Test-Path $reportHtml) {
            Write-Host "Reporte HTML generado en: $reportHtml" -ForegroundColor Green
        } elseif (Test-Path $reportJson) {
            Write-Host "Reporte JSON (fallback) generado en: $reportJson" -ForegroundColor Green
        } else {
            Write-Host "Advertencia: se solicitó generar un reporte pero no se encontró un archivo de salida. Revisa que el reporter esté instalado." -ForegroundColor Yellow
        }
    }
    exit 0
} else {
    Write-Host "Newman falló con código $($exitCode)" -ForegroundColor Red
    if (-not $NoHtmlReport) {
        if (Test-Path $reportHtml) { Write-Host "Reporte HTML generado en: $reportHtml" -ForegroundColor Green }
        elseif (Test-Path $reportJson) { Write-Host "Reporte JSON (fallback) generado en: $reportJson" -ForegroundColor Green }
    }
    exit $exitCode
}
