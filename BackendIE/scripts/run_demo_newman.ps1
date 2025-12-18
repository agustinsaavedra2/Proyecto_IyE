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
$baseUrl = $env["BASE_URL"] -or $env["BASEURL"] -or $env["BASE_URL"] -or $env["APP_BASE_URL"] -or $env["OLLAMA_BASE_URL"] -or "http://localhost:8080"
# Prefer collection variable 'baseUrl' in collection, but we pass it explicitly

# Token resolution: parameter -> .env -> prompt
if (-not $Token) {
    if ($env.ContainsKey('TOKEN') -and $env['TOKEN']) { $Token = $env['TOKEN'] }
    elseif ($env.ContainsKey('TOKEN_SECRET') -and $env['TOKEN_SECRET']) { $Token = $env['TOKEN_SECRET'] }
}

if (-not $Token) {
    Write-Host "No se encontró token en parámetros ni en .env. Ingresa token (ENTER para cancelar):"
    $Token = Read-Host "Token"
}

if (-not $Token) {
    Write-Host "Token no provisto. Ejecutando la colección sin token (algunas requests pueden fallar)." -ForegroundColor Yellow
}

# Other useful variables
$empresaId = $env['empresaId'] -or $env['EMPRESA_ID'] -or 1
$riesgoId = $env['riesgoId'] -or $env['RIESGO_ID'] -or 1

# Ensure reports folder
if (-not (Test-Path $reportsDir)) { New-Item -ItemType Directory -Path $reportsDir | Out-Null }
$reportHtml = Join-Path $reportsDir "demo_newman_report.html"

# Check newman availability
function Test-Newman {
    try {
        $v = & newman -v 2>&1
        return $true
    } catch {
        return $false
    }
}

if (-not (Test-Newman)) {
    Write-Host "newman no está instalado o no está en PATH." -ForegroundColor Red
    Write-Host "Instalar con: npm install -g newman newman-reporter-htmlextra" -ForegroundColor Yellow
    exit 1
}

# Prefer htmlextra reporter if available
$useHtmlExtra = $false
try {
    # Try running node to resolve module. If it resolves, htmlextra is available.
    $nodeCmd = "node -e \"require.resolve('newman-reporter-htmlextra')\""
    $res = cmd /c $nodeCmd 2>$null
    if ($LASTEXITCODE -eq 0) { $useHtmlExtra = $true }
} catch {
    $useHtmlExtra = $false
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

# reporters
$newmanArgs += "--reporters";
if ($useHtmlExtra -and -not $NoHtmlReport) { $newmanArgs += "cli,htmlextra" } elseif (-not $NoHtmlReport) { $newmanArgs += "cli,html" } else { $newmanArgs += "cli" }

# reporter output
if (-not $NoHtmlReport) {
    if ($useHtmlExtra) { $newmanArgs += "--reporter-htmlextra-export"; $newmanArgs += "`"$reportHtml`"" }
    else { $newmanArgs += "--reporter-html-export"; $newmanArgs += "`"$reportHtml`"" }
}

Write-Host "Ejecutando newman con colección: $collectionPath"
Write-Host "baseUrl=$baseUrl empresaId=$empresaId riesgoId=$riesgoId"

# Run newman
$psi = [System.Diagnostics.ProcessStartInfo]::new('newman')
$psi.Arguments = ($newmanArgs -join ' ')
$psi.RedirectStandardOutput = $true
$psi.RedirectStandardError = $true
$psi.UseShellExecute = $false
$proc = [System.Diagnostics.Process]::Start($psi)
$stdout = $proc.StandardOutput.ReadToEnd()
$stderr = $proc.StandardError.ReadToEnd()
$proc.WaitForExit()

Write-Host $stdout
if ($stderr) { Write-Host $stderr -ForegroundColor Red }

if ($proc.ExitCode -eq 0) {
    Write-Host "Newman finalizó correctamente." -ForegroundColor Green
    if (-not $NoHtmlReport) { Write-Host "Reporte HTML generado en: $reportHtml" -ForegroundColor Green }
    exit 0
} else {
    Write-Host "Newman falló con código $($proc.ExitCode)" -ForegroundColor Red
    exit $proc.ExitCode
}

