param(
    [Parameter(Position = 0)]
    [ValidateSet('up', 'down', 'reset-db', 'logs', 'test', 'dev')]
    [string]$Action = 'up'
)

$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    switch ($Action) {
        'up' { docker compose up --build -d --wait }
        'down' { docker compose down --remove-orphans }
        'reset-db' { docker compose down --volumes --remove-orphans }
        'logs' { docker compose logs -f }
        'dev' { docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build -d --wait }
        'test' {
            docker build --target test --progress plain -t barstock-backend-tests ./backend
            if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
            docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal --add-host=host.docker.internal:host-gateway barstock-backend-tests
        }
    }
    $result = $LASTEXITCODE
} finally {
    Pop-Location
}
exit $result
