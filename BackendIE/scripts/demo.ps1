# Demo script para la presentación: ejecuta CRUD básico y llamadas ML
# Requiere: app corriendo en http://localhost:8080

$base = "http://localhost:8080"

Write-Host "1) Registrar empresa"
$body = @{
    admin = 1
    categoriaId = "cat1"
    nombre = "Empresa Demo"
    codigoEmpresa = "EMP-001"
    ubicacion = "Santiago"
    descripcion = "Empresa demo para entrega"
} | ConvertTo-Json -Compress
$res = curl -Method Post -Uri "$base/api/empresas/registrar" -Body $body -ContentType 'application/json'
$res | ConvertTo-Json | Write-Host

Write-Host "2) Crear riesgo"
$riesgo = @{
    titulo = "Incumplimiento higiene"
    descripcion = "Falta registro de temperatura"
    categoria = "compliance"
    probabilidad = "media"
    impacto = "medio"
    nivelRiesgo = "Medio"
}
$rr = curl -Method Post -Uri "$base/api/riesgos" -Body ($riesgo | ConvertTo-Json -Compress) -ContentType 'application/json'
$rr | ConvertTo-Json | Write-Host

Write-Host "3) Listar riesgos"
$rlist = curl -Method Get -Uri "$base/api/riesgos"
$rlist | ConvertTo-Json | Write-Host

Write-Host "4) Crear auditoría via Ollama (ejemplo)"
$aud = @{
    empresaId = 1
    tipo = "sanitaria"
    objetivo = "Revisión inicial"
    auditorLiderId = 1
    idsDePoliticasAEvaluar = @()
}
$audRes = curl -Method Post -Uri "$base/api/ollama/crearAuditoria" -Body ($aud | ConvertTo-Json -Compress) -ContentType 'application/json'
$audRes | ConvertTo-Json | Write-Host

Write-Host "Demo finalizado"

