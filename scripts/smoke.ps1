param([string]$BaseUrl='http://localhost:8080', [string]$OtpCode='')
$ErrorActionPreference='Stop'
$repoPath=Split-Path -Parent $PSScriptRoot
$settings=@{}
Get-Content -LiteralPath (Join-Path $repoPath '.env') | ForEach-Object {
    if ($_ -match '^([A-Z_]+)=(.*)$') { $settings[$matches[1]]=$matches[2] }
}
if ($OtpCode) {
    $settings.ADMIN_EMAIL='acceptance-admin@example.test'
    $settings.ADMIN_PASSWORD='Acceptance-only-2026!'
}
function Invoke-Api {
    param([string]$Method,[string]$Path,$Body=$null,[string]$Token='')
    $headers=@{}
    if ($Token) { $headers.Authorization='Bearer '+$Token }
    $params=@{Method=$Method;Uri=$BaseUrl+$Path;Headers=$headers;TimeoutSec=20}
    if ($null -ne $Body) { $params.Body=ConvertTo-Json -InputObject $Body -Depth 8 -Compress; $params.ContentType='application/json' }
    Invoke-RestMethod @params
}
function Wait-Until {
    param([scriptblock]$Check,[string]$Description)
    $deadline=[DateTime]::UtcNow.AddSeconds(120)
    do {
        try { if (& $Check) { return } } catch { }
        Start-Sleep -Seconds 2
    } while ([DateTime]::UtcNow -lt $deadline)
    throw ('Timed out: '+$Description)
}
function New-DemoAccount {
    param([string]$Role)
    $suffix=[Guid]::NewGuid().ToString('N')
    $digits=-join (1..10 | ForEach-Object {Get-Random -Minimum 0 -Maximum 10})
    $body=@{email=$suffix+'@example.test';password='Demo-only-'+$suffix;fullName='Smoke Test';phoneNumber='+91'+$digits;role=$Role}
    if ((Invoke-Api GET '/api/sms/config').enabled) {
        if (-not $OtpCode) { throw 'OTP is enabled. Use the isolated e2e stack and supply -OtpCode; no real SMS is sent by its test provider.' }
        Invoke-Api POST '/api/sms/send' @{phoneNumber=$body.phoneNumber} | Out-Null
        $verification=Invoke-Api POST '/api/sms/verify' @{phoneNumber=$body.phoneNumber;otpCode=$OtpCode}
        $body.phoneVerificationToken=$verification.verificationToken
    }
    $account=Invoke-Api POST '/api/users/register' $body
    $login=Invoke-Api POST '/api/users/login' @{identifier=$body.email;password=$body.password}
    return @{Id=$account.id;Token=$login.jwt}
}
Wait-Until { $null -ne (Invoke-Api GET '/api/sms/config').enabled } 'registration API readiness'
Wait-Until {
    $script:admin=Invoke-Api POST '/api/users/login' @{identifier=$settings.ADMIN_EMAIL;password=$settings.ADMIN_PASSWORD}
    return !!$script:admin.jwt
} 'service discovery and administrator login'
$owner=New-DemoAccount 'SHOPKEEPER'
$customer=New-DemoAccount 'USER'
$shop=Invoke-Api POST ('/api/shops/register?userId='+$owner.Id) @{
    name='Smoke Test Grocery';description='Local shop created by the backend smoke test';
    category='Grocery';address='Test Road';latitude=10;longitude=76
} $owner.Token
Wait-Until { @((Invoke-Api GET '/api/approvals/pending' $null $admin.jwt) | Where-Object shopId -eq $shop.id).Count -gt 0 } 'approval request delivery'
Invoke-Api POST ('/api/approvals/'+$shop.id+'/approve') $null $admin.jwt | Out-Null
Wait-Until { (Invoke-Api GET ('/api/shops/'+$shop.id) $null $owner.Token).status -eq 'APPROVED' } 'approval decision delivery'
$product=Invoke-Api POST ('/api/products?shopId='+$shop.id) @{name='Milk';available=$true} $owner.Token
$updatedName='Fresh Milk '+$shop.id
$updated=Invoke-Api PUT ('/api/products/'+$product.id) @{name=$updatedName;available=$false} $owner.Token
if ($updated.name -ne $updatedName -or $updated.available) { throw 'Product update did not apply the supplied values' }
$query=[Uri]::EscapeDataString($updatedName)
 $matches=Invoke-Api GET ('/api/products/search?q='+$query+'&category=Grocery&availableOnly=false') $null $customer.Token
 if (@($matches.content | Where-Object shopId -eq $shop.id).Count -ne 1) { throw 'Product search did not return the matching shop inventory' }
 $available=Invoke-Api GET ('/api/products/search?q='+$query+'&availableOnly=true') $null $customer.Token
 if (@($available.content | Where-Object shopId -eq $shop.id).Count -ne 0) { throw 'Available-only search returned unavailable stock' }
 $wrongCategory=Invoke-Api GET ('/api/products/search?q='+$query+'&category=Books&availableOnly=false') $null $customer.Token
 if (@($wrongCategory.content | Where-Object shopId -eq $shop.id).Count -ne 0) { throw 'Category and product filters were not combined' }
$rating=Invoke-Api POST ('/api/ratings/add?userId='+$customer.Id+'&shopId='+$shop.id) @{rating=5;review='Helpful service'} $customer.Token
 $mine=Invoke-Api GET ('/api/ratings/mine/'+$shop.id) $null $customer.Token
 if ($mine.userId -ne $customer.Id -or $mine.rating -ne 5) { throw 'Own-review lookup returned an incorrect record' }
for ($i=0;$i -lt 2;$i++) { Invoke-Api POST ('/api/favorites/'+$shop.id+'?userId='+$customer.Id) $null $customer.Token | Out-Null }
$count=Invoke-Api GET ('/api/favorites/count/'+$customer.Id) $null $customer.Token
if ($count -ne 1) { throw 'Favourite retry created duplicate data' }
$secondOwner=New-DemoAccount 'SHOPKEEPER'
$reporter=New-DemoAccount 'USER'
$report=Invoke-Api POST ('/api/ratings/'+$rating.id+'/reports') @{reason='Smoke moderation check'} $reporter.Token
$reportPage=Invoke-Api GET '/api/ratings/reports?page=0&size=100' $null $admin.jwt
if (@($reportPage.content | Where-Object id -eq $report.id).Count -ne 1) { throw 'Moderation queue did not return the new report' }
Invoke-Api POST ('/api/ratings/reports/'+$report.id+'/resolve') @{action='REMOVE'} $admin.jwt | Out-Null
$audit=Invoke-Api GET '/api/ratings/reports?status=REMOVED&page=0&size=100' $null $admin.jwt
if (@($audit.content | Where-Object { $_.id -eq $report.id -and $_.review -eq 'Helpful service' }).Count -ne 1) { throw 'Moderation removed the report audit snapshot' }
$rejected=Invoke-Api POST ('/api/shops/register?userId='+$secondOwner.Id) @{
    name='Rejected Test Shop';description='Testing the rejected approval workflow';
    category='Grocery';address='Unknown Road';latitude=10;longitude=76
} $secondOwner.Token
Wait-Until { @((Invoke-Api GET '/api/approvals/pending' $null $admin.jwt) | Where-Object shopId -eq $rejected.id).Count -gt 0 } 'second approval request'
Invoke-Api POST ('/api/approvals/'+$rejected.id+'/reject?reason=Invalid%20address') $null $admin.jwt | Out-Null
Wait-Until { (Invoke-Api GET ('/api/shops/'+$rejected.id) $null $secondOwner.Token).status -eq 'REJECTED' } 'rejection decision delivery'
Invoke-Api POST ('/api/shops/'+$rejected.id+'/resubmit') $null $secondOwner.Token | Out-Null
Wait-Until { @((Invoke-Api GET '/api/approvals/pending' $null $admin.jwt) | Where-Object shopId -eq $rejected.id).Count -gt 0 } 'resubmission delivery'
Invoke-Api POST ('/api/approvals/'+$rejected.id+'/approve') $null $admin.jwt | Out-Null
Wait-Until { (Invoke-Api GET ('/api/shops/'+$rejected.id) $null $secondOwner.Token).status -eq 'APPROVED' } 'resubmission approval'
Invoke-Api POST '/api/users/logout' $null $customer.Token | Out-Null
$revoked = $false
try { Invoke-Api GET ('/api/products/search?q='+$query) $null $customer.Token | Out-Null }
catch { if ([int]$_.Exception.Response.StatusCode -eq 401) { $revoked=$true } else { throw } }
if (-not $revoked) { throw 'Logged out token was still accepted by Shop Service' }
Write-Host 'PASS: registration/OTP, login, routing, approval/rejection/resubmission delivery, product search and updates, reviews, favourites, moderation audit retention and cross-service logout revocation.'
Write-Host 'The test accounts and shops remain in the selected database for inspection.'
