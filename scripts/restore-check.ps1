param([string]$BackupDirectory = (Join-Path $PSScriptRoot '../artifacts/backups'))
$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
  foreach ($name in @('users','shops','approvals','ratings','favorites')) {
    $dump = Get-ChildItem -LiteralPath $BackupDirectory -Filter ($name + '-*.dump') | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (-not $dump) { throw "No backup found for $name" }
    $database = 'restore_' + [Guid]::NewGuid().ToString('N')
    $remote = '/tmp/' + $database + '.dump'
    & docker compose cp $dump.FullName ('test-database:' + $remote)
    if ($LASTEXITCODE -ne 0) { throw 'Copy to isolated test container failed' }
    & docker compose exec -T test-database createdb -U street_smart_test $database
    if ($LASTEXITCODE -ne 0) { throw 'Creating isolated restore database failed' }
    try {
      & docker compose exec -T test-database pg_restore --exit-on-error --no-owner --no-privileges -U street_smart_test -d $database $remote
      if ($LASTEXITCODE -ne 0) { throw "Restore failed for $name" }
      $tables = & docker compose exec -T test-database psql -U street_smart_test -d $database -Atc "select count(*) from information_schema.tables where table_schema='public'"
      if ($LASTEXITCODE -ne 0 -or [int]$tables -lt 1) { throw 'Restored schema is empty' }
      Write-Host "PASS: $name restored successfully into isolated test database ($tables tables)."
    } finally {
      # Only delete the uniquely named disposable database created in this iteration.
      & docker compose exec -T test-database dropdb -U street_smart_test $database
      if ($LASTEXITCODE -ne 0) { Write-Warning "Test database $database needs cleanup" }
    }
  }
} finally { Pop-Location }
