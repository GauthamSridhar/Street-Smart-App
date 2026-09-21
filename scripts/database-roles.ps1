# Provision runtime-only logins after Flyway has migrated every database.
$ErrorActionPreference = 'Stop'
$repoPath = Split-Path $PSScriptRoot -Parent
$roleFile = Join-Path $repoPath '.env.roles'
$names = @('users','shops','approvals','ratings','favorites')
if (-not (Test-Path -LiteralPath $roleFile)) {
  $lines = foreach ($name in $names) {
    $bytes = New-Object byte[] 48
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    $name.ToUpperInvariant() + '_DB_PASSWORD=' + [Convert]::ToBase64String($bytes)
  }
  [IO.File]::WriteAllLines($roleFile, $lines, (New-Object Text.UTF8Encoding($false)))
}
$secrets = @{}
foreach ($line in [IO.File]::ReadAllLines($roleFile)) {
  if ($line -match '^([A-Z_]+)=(.*)$') { $secrets[$matches[1]] = $matches[2] }
}
Push-Location $repoPath
try {
  foreach ($name in $names) {
    $password = $secrets[$name.ToUpperInvariant() + '_DB_PASSWORD']
    if ($password -notmatch '^[A-Za-z0-9+/=]{64}$') { throw 'Invalid generated role credential format' }
    $role = 'app_' + $name
    $sql = "SELECT 'CREATE ROLE $role LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE' WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='$role')\gexec`n"
    $sql += "ALTER ROLE $role PASSWORD '$password';`nREVOKE CONNECT ON DATABASE $name FROM PUBLIC;`nGRANT CONNECT ON DATABASE $name TO $role;`n"
    $sql += "REVOKE CREATE ON SCHEMA public FROM PUBLIC;`nGRANT USAGE ON SCHEMA public TO $role;`nGRANT SELECT,INSERT,UPDATE,DELETE ON ALL TABLES IN SCHEMA public TO $role;`n"
    $sql += "GRANT USAGE,SELECT ON ALL SEQUENCES IN SCHEMA public TO $role;`nALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT,INSERT,UPDATE,DELETE ON TABLES TO $role;`n"
    $sql += "REVOKE ALL ON TABLE flyway_schema_history FROM $role;`n"
    $sql | & docker compose exec -T database sh -c 'exec psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$1"' sh $name | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "Role provisioning failed for $name; .env.roles is preserved for retry" }
  }
  Write-Host 'Runtime database roles provisioned. Keep .env.roles private; use compose.roles.yml after migrations.'
} finally { Pop-Location }
