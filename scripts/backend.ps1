param([ValidateSet('Init','Test','TestPostgres','Start','Stop')][string]$Action = 'Test')
$ErrorActionPreference = 'Stop'
$repoPath = Split-Path -Parent $PSScriptRoot
function Invoke-BackendMaven {
    if ($env:OS -eq 'Windows_NT') { & (Join-Path $repoPath 'UserService/mvnw.cmd') @args }
    else { & bash (Join-Path $repoPath 'UserService/mvnw') @args }
}
Push-Location $repoPath
try {
    switch ($Action) {
        'Init' {
            if (Test-Path -LiteralPath '.env') { Write-Host '.env already exists; kept existing values.'; break }
            function New-RandomSecret {
                $bytes = New-Object byte[] 48
                $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
                try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
                return [Convert]::ToBase64String($bytes)
            }
            $config = @"
# Private local configuration. Do not commit this file.
DB_USER=street_smart
DB_PASSWORD=GENERATE_ME
JWT_SECRET=GENERATE_ME_BASE64
INTERNAL_API_KEY=GENERATE_ME
ADMIN_EMAIL=admin@example.test
ADMIN_PASSWORD=GENERATE_ME
ADMIN_PHONE=+919999999999
ALLOWED_ORIGINS=http://localhost:4200
SMS_ENABLED=false
SMS_ACCOUNT_SID=
SMS_AUTH_TOKEN=
SMS_API_KEY_SID=
SMS_API_KEY_SECRET=
SMS_VERIFY_SERVICE_SID=
SMS_FROM=
GOOGLE_MAPS_API_KEY=
"@.Trim()
            $config = $config.Replace('GENERATE_ME_BASE64', (New-RandomSecret))
            while ($config.Contains('GENERATE_ME')) {
                $position = $config.IndexOf('GENERATE_ME')
                $config = $config.Substring(0,$position) + (New-RandomSecret) + $config.Substring($position + 11)
            }
            [IO.File]::WriteAllText((Join-Path $repoPath '.env'), $config, (New-Object Text.UTF8Encoding($false)))
            Write-Host 'Created private .env with random local secrets. Admin credentials are in this file.'
        }
        'Test' {
            Invoke-BackendMaven -B -ntp verify
            if ($LASTEXITCODE -ne 0) { throw 'Backend verification failed' }
        }
        'TestPostgres' {
            & docker compose --profile test up -d --wait test-database
            if ($LASTEXITCODE -ne 0) { throw 'Test PostgreSQL startup failed' }
            $previousUrl = $env:TEST_DB_URL
            $previousUser = $env:TEST_DB_USER
            $previousPassword = $env:TEST_DB_PASSWORD
            try {
                $env:TEST_DB_USER = 'street_smart_test'
                $env:TEST_DB_PASSWORD = 'local-test-only'
                foreach ($item in @(@('UserService','users'),@('ShopService','shops'),@('ShopApprovalService','approvals'),@('RatingService','ratings'),@('FavoriteService','favorites'))) {
                    $env:TEST_DB_URL = 'jdbc:postgresql://localhost:55441/' + $item[1]
                    Invoke-BackendMaven -B -ntp -pl $item[0] -am test
                    if ($LASTEXITCODE -ne 0) { throw ('PostgreSQL tests failed for ' + $item[0]) }
                }
            } finally {
                $env:TEST_DB_URL = $previousUrl
                $env:TEST_DB_USER = $previousUser
                $env:TEST_DB_PASSWORD = $previousPassword
            }
        }
        'Start' {
            & docker compose up -d --build --wait
            if ($LASTEXITCODE -ne 0) { throw 'Backend startup failed' }
            Write-Host 'Gateway is available at http://localhost:8080. Run scripts/smoke.ps1 next.'
        }
        'Stop' {
            & docker compose stop
            if ($LASTEXITCODE -ne 0) { throw 'Backend stop failed' }
        }
    }
} finally { Pop-Location }
