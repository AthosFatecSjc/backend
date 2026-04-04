# Script de teste para POST /login com dados de teste

$BASE_URL = "http://localhost:8181"
$ENDPOINT = "/login"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Teste de Autenticação BACK-03" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Teste 1: Login com usuário ATIVO (deve retornar 200 + token)
Write-Host "1️⃣  Teste ATIVO - Deve retornar HTTP 200:" -ForegroundColor Green
Write-Host "---"
$response = Invoke-WebRequest -Uri "$BASE_URL$ENDPOINT" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '{"email":"active@example.com","senha":"senha123456"}' `
  -PassThru
Write-Host ($response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10)
Write-Host "HTTP Status: $($response.StatusCode)" -ForegroundColor Yellow
Write-Host ""

# Teste 2: Login com usuário PENDENTE (deve retornar 403)
Write-Host "2️⃣  Teste PENDENTE - Deve retornar HTTP 403:" -ForegroundColor Yellow
Write-Host "---"
try {
  $response = Invoke-WebRequest -Uri "$BASE_URL$ENDPOINT" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body '{"email":"pending@example.com","senha":"senha123456"}' `
    -PassThru
} catch {
  Write-Host ($_.Exception.Response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10)
  Write-Host "HTTP Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
}
Write-Host ""

# Teste 3: Login com usuário REJEITADO (deve retornar 403)
Write-Host "3️⃣  Teste REJEITADO - Deve retornar HTTP 403:" -ForegroundColor Yellow
Write-Host "---"
try {
  $response = Invoke-WebRequest -Uri "$BASE_URL$ENDPOINT" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body '{"email":"rejected@example.com","senha":"senha123456"}' `
    -PassThru
} catch {
  Write-Host ($_.Exception.Response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10)
  Write-Host "HTTP Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
}
Write-Host ""

# Teste 4: Credenciais inválidas (deve retornar 401)
Write-Host "4️⃣  Teste COM CREDENCIAL INVÁLIDA - Deve retornar HTTP 401:" -ForegroundColor Red
Write-Host "---"
try {
  $response = Invoke-WebRequest -Uri "$BASE_URL$ENDPOINT" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body '{"email":"active@example.com","senha":"senhaerrada"}' `
    -PassThru
} catch {
  Write-Host ($_.Exception.Response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10)
  Write-Host "HTTP Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
}
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
