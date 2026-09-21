param(
  [Parameter(Mandatory)][ValidateSet('Status','Start','Stop','Connect')][string]$Action,
  [Parameter(Mandatory)][ValidatePattern('^i-[0-9a-f]{8,17}$')][string]$InstanceId,
  [Parameter(Mandatory)][string]$Region
)
$ErrorActionPreference = 'Stop'
switch ($Action) {
  'Status' { & aws ec2 describe-instances --instance-ids $InstanceId --region $Region --query 'Reservations[].Instances[].{State:State.Name,Type:InstanceType}' --output table }
  'Start' {
    & aws ec2 start-instances --instance-ids $InstanceId --region $Region
    if ($LASTEXITCODE -ne 0) { throw 'Start failed' }
    Write-Host 'Wait for instance checks and SSM readiness. The two-hour stop timer starts at boot.'
  }
  'Stop' { & aws ec2 stop-instances --instance-ids $InstanceId --region $Region }
  'Connect' {
    & aws ssm start-session --target $InstanceId --region $Region --document-name AWS-StartPortForwardingSession --parameters 'portNumber=8088,localPortNumber=8088'
  }
}
if ($LASTEXITCODE -ne 0) { throw "AWS $Action failed" }
