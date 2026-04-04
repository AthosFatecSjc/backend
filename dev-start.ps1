# Script de inicialização para DESENVOLVIMENTO
# Executa a aplicação com variáveis do .env
# Certifique-se de ter um arquivo .env na raiz do projeto

Write-Host "🚀 Iniciando Backend em MODO DESENVOLVIMENTO..." -ForegroundColor Green
Write-Host ""

# Carrega variáveis do .env
$envFile = ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_
        if ($line -and -not $line.StartsWith("#")) {
            $key, $value = $line -split "=", 2
            if ($key) {
                [Environment]::SetEnvironmentVariable($key, $value, "Process")
            }
        }
    }
    Write-Host "✅ Arquivo .env carregado com sucesso" -ForegroundColor Green
} else {
    Write-Host "❌ Arquivo .env não encontrado. Crie um .env na raiz do projeto." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Configuração ativa:" -ForegroundColor Cyan
Write-Host "  PostgreSQL: $($env:SPRING_DATASOURCE_USERNAME)@$($env:SPRING_DATASOURCE_URL)" -ForegroundColor Cyan
Write-Host "  MongoDB: $($env:SPRING_DATA_MONGODB_URI)" -ForegroundColor Cyan
Write-Host "  Porta: $($env:SERVER_PORT)" -ForegroundColor Cyan
Write-Host "  Endpoint de teste: http://localhost:$($env:SERVER_PORT)/login" -ForegroundColor Cyan
Write-Host ""

Set-Location "backend-java"
mvn spring-boot:run
