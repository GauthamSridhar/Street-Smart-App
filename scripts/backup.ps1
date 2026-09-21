param([string]$OutputDirectory = (Join-Path $PSScriptRoot '../artifacts/backups'))
$ErrorActionPreference = 'Stop'
$backupPath = [IO.Path]::GetFullPath($OutputDirectory)
New-Item -ItemType Directory -Path $backupPath -Force | Out-Null
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
  $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
  foreach ($database in @('users','shops','approvals','ratings','favorites')) {
    $remote = '/tmp/street-smart-' + $database + '-' + $stamp + '.dump'
    & docker compose exec -T database sh -c 'pg_dump -U "$POSTGRES_USER" -Fc -d "$1" -f "$2"' sh $database $remote
    if ($LASTEXITCODE -ne 0) { throw "Backup failed for $database" }
    & docker compose cp ('database:' + $remote) (Join-Path $backupPath ($database + '-' + $stamp + '.dump'))
    if ($LASTEXITCODE -ne 0) { throw "Backup copy failed for $database" }
  }
  Write-Host "Backups saved to $backupPath. Encrypt before copying off host. Backups are per-database snapshots, not one atomic snapshot across services."
} finally { Pop-Location }
