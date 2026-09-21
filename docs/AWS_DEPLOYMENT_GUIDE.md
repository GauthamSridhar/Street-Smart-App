# Deploy Street Smart to AWS: private, on-demand lab

This guide deploys Street Smart as a **private learning/demo environment** on one EC2 instance. You access it through an encrypted AWS Systems Manager (SSM) tunnel, not through a public URL. Start it only when you want to learn or demo, then stop the instance when finished.

It is intentionally not a high-availability production architecture. It does not create a load balancer, NAT Gateway, RDS, domain name, or inbound firewall rule. See [AWS_LAB.md](AWS_LAB.md) for the design and cost boundaries.

## What this creates

| Resource | Why it exists | Cost behavior |
| --- | --- | --- |
| One t3.large EC2 instance | Runs Docker, PostgreSQL and all application containers | Compute charges stop when the instance is stopped |
| 40 GB encrypted gp3 EBS root volume | Holds Docker images and PostgreSQL data | Continues to cost money while the instance is stopped |
| VPC, subnet, route table, internet gateway | Allows package downloads and SSM | No NAT Gateway or load balancer is created |
| IAM role for EC2 | Lets the instance register with Systems Manager | No access keys are stored on the server |

The CloudFormation template adds a timer which stops the instance **two hours after every boot**. Treat it as a safety net, not as a billing cap.

## Before you begin

1. Use an IAM user, role or IAM Identity Center permission set; **do not use the AWS root user**. Enable MFA.
2. Choose a region close to you, for example `ap-south-1`. Check EC2, EBS and public IPv4 pricing before continuing.
3. Create a small AWS Budget alert. It notifies you; it cannot automatically prevent every charge.
4. Push this repository to a Git remote you can access from the VM. Confirm that `.env`, `.env.roles`, `node_modules`, `target` and `dist` are not committed:

   ```powershell
   git status --ignored --short .env .env.roles
   ```

5. Install these on your Windows laptop:
   - [AWS CLI v2](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)
   - [Session Manager plugin](https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager-working-with-install-plugin.html)
   - Git and a modern PowerShell
6. Configure an AWS CLI profile and verify it:

   ```powershell
   aws configure sso --profile street-smart-lab
   $env:AWS_PROFILE = 'street-smart-lab'
   $region = 'ap-south-1'
   aws sts get-caller-identity --region $region
   session-manager-plugin
   ```

   The final command only verifies that the plugin can start; stop it with `Ctrl+C` if it waits for input. One-time deployment permissions must allow CloudFormation to create the EC2, VPC, IAM role and instance profile declared in `infra/aws/lab.yml`. Daily-use permissions can be limited to describing, starting/stopping this instance and opening SSM sessions.

## Step 1: validate and create the lab

Run these commands from the repository root. Keep the stack name unless you deliberately choose another name.

```powershell
$stack = 'street-smart-lab'
aws cloudformation validate-template `
  --template-body file://infra/aws/lab.yml `
  --region $region

aws cloudformation deploy `
  --template-file infra/aws/lab.yml `
  --stack-name $stack `
  --capabilities CAPABILITY_IAM `
  --region $region
```

Wait for `Successfully created/updated stack`. It creates billable resources. Retrieve and save only the instance ID (it is not a secret):

```powershell
$instanceId = aws cloudformation describe-stacks `
  --stack-name $stack --region $region `
  --query "Stacks[0].Outputs[?OutputKey=='InstanceId'].OutputValue" `
  --output text
$instanceId
.\scripts\aws-lab.ps1 -Action Status -InstanceId $instanceId -Region $region
```

The first boot installs Docker and Git, so wait several minutes after EC2 shows `running`. The instance must also become available in Systems Manager before you can open a shell.

## Step 2: open a secure server shell

There is no SSH security-group rule and no SSH key pair. Use SSM:

```powershell
aws ssm start-session --target $instanceId --region $region
```

If this says the instance is not connected, wait two to five minutes and retry. In the AWS console, open **Systems Manager -> Fleet Manager -> Managed nodes** to confirm the node is online. Do not add inbound port 22, 80 or 443 just to work around an SSM delay.

## Step 3: put the reviewed source on the server

Run the following **inside the SSM shell**. Replace the repository URL. A public repository is simplest for a learning lab. For a private repository, use a read-only deploy key or authenticate interactively; never put a personal access token in the URL, this guide, `.env`, shell history or Git config.

```bash
sudo mkdir -p /opt/street-smart
sudo chown "$USER":"$USER" /opt/street-smart
git clone https://github.com/YOUR_ACCOUNT/YOUR_REPOSITORY.git /opt/street-smart
cd /opt/street-smart
git status
```

Confirm `git status` is clean and `compose.yml`, `compose.lab.yml` and `infra/aws/lab.yml` exist before continuing.

## Step 4: create server-only configuration

Still inside the SSM shell, create a new deployment secret file. The values below are generated on the server and are different from your laptop values. Keep SMS and Google Maps disabled for the first deployment; the app works without them.

```bash
cd /opt/street-smart
umask 077
cat > .env <<EOF
DB_USER=street_smart
DB_PASSWORD=$(openssl rand -base64 32 | tr -d '\n')
JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')
INTERNAL_API_KEY=$(openssl rand -base64 48 | tr -d '\n')
ADMIN_EMAIL=replace-with-your-admin-email@example.com
ADMIN_PASSWORD=$(openssl rand -base64 32 | tr -d '\n')
ADMIN_PHONE=+919999999999
ALLOWED_ORIGINS=http://localhost:8088
SMS_ENABLED=false
SMS_ACCOUNT_SID=
SMS_AUTH_TOKEN=
SMS_API_KEY_SID=
SMS_API_KEY_SECRET=
SMS_VERIFY_SERVICE_SID=
SMS_FROM=
GOOGLE_MAPS_API_KEY=
EOF
chmod 600 .env
```

Change `ADMIN_EMAIL` and `ADMIN_PHONE` to your own values. Copy the generated admin password to a password manager before leaving the shell; it is the value you use at the application login page. Never paste the file into chat, GitHub, or a screen recording.

Confirm the file is private without printing its contents:

```bash
stat -c '%a %n' .env
grep -E '^(ADMIN_EMAIL|SMS_ENABLED|ALLOWED_ORIGINS)=' .env
```

Expected permissions are `600`. The second command must show only the three safe settings, not secrets.

## Step 5: build and start

The lab overlay publishes the frontend only on the VM loopback interface. Caddy serves the Angular app and forwards `/api` to the gateway.

```bash
cd /opt/street-smart
sudo docker compose -f compose.yml -f compose.lab.yml config --quiet
sudo docker compose -f compose.yml -f compose.lab.yml up -d --build --wait
sudo docker compose -f compose.yml -f compose.lab.yml ps
curl --fail --silent http://127.0.0.1:8088 > /dev/null && echo 'Web is ready'
```

The first build downloads base images and compiles the services, so it can take several minutes. If `--wait` fails, do not delete volumes. Inspect the failing service first:

```bash
sudo docker compose -f compose.yml -f compose.lab.yml logs --tail=120 gateway users shops
sudo docker compose -f compose.yml -f compose.lab.yml ps
```

## Step 6: view the application from your laptop

Exit the SSM shell with `exit`. On the laptop, start the port-forwarding session and keep this terminal open:

```powershell
.\scripts\aws-lab.ps1 -Action Connect -InstanceId $instanceId -Region $region
```

Open <http://localhost:8088> in your browser. Log in with `ADMIN_EMAIL` and `ADMIN_PASSWORD` from the server's `/opt/street-smart/.env` file. The browser talks to Caddy over the SSM tunnel; the application remains unavailable to the public internet.

## Step 7: stop it when finished

First end the port-forward session with `Ctrl+C`, then stop the EC2 instance from a second local terminal:

```powershell
.\scripts\aws-lab.ps1 -Action Stop -InstanceId $instanceId -Region $region
.\scripts\aws-lab.ps1 -Action Status -InstanceId $instanceId -Region $region
```

Wait for `stopped`. The Docker containers and database remain on the encrypted EBS volume and will start again automatically on the next boot. The automatic two-hour timer is a fallback; always stop the instance yourself after a demo.

## Start it again later

```powershell
.\scripts\aws-lab.ps1 -Action Start -InstanceId $instanceId -Region $region
.\scripts\aws-lab.ps1 -Action Status -InstanceId $instanceId -Region $region
.\scripts\aws-lab.ps1 -Action Connect -InstanceId $instanceId -Region $region
```

After `Start`, wait for EC2 checks, SSM and Docker before using `Connect`. Docker restarts the containers because the Compose services use `restart: unless-stopped`.

## Update the application safely

1. Make changes locally, run the relevant tests, commit and push reviewed code.
2. Start the instance and open an SSM shell.
3. Back up databases before a migration or major update.
4. Pull the reviewed commit, validate Compose and recreate only what changed:

   ```bash
   cd /opt/street-smart
   git fetch --all --prune
   git checkout YOUR_COMMIT_SHA
   sudo docker compose -f compose.yml -f compose.lab.yml config --quiet
   sudo docker compose -f compose.yml -f compose.lab.yml up -d --build --wait
   ```

5. Check `ps`, inspect logs if needed, then reconnect through the SSM tunnel.
6. Stop the instance when the update is verified.

Use a commit SHA rather than `git pull` so the deployed code is explicit and reproducible. Do not run `docker compose down -v`; `-v` deletes database volumes.

## Backup before deleting the lab

Stopping is reversible; deleting the CloudFormation stack terminates EC2 and deletes its root EBS volume, including PostgreSQL data and generated `.env`. Before deletion, open an SSM shell and run the repository backup procedure. The default Ubuntu lab does not install PowerShell, so either install it before using `scripts/backup.ps1` or use `pg_dump` from the PostgreSQL container. Copy encrypted backup files off the VM and test restoration in an isolated database before deleting anything. Then delete only the named stack:

```powershell
aws cloudformation delete-stack --stack-name $stack --region $region
aws cloudformation wait stack-delete-complete --stack-name $stack --region $region
```

Finally check the AWS console for leftover snapshots, EBS volumes or manually created resources. The template's root volume is deleted with the instance; manually created snapshots are not.

## Troubleshooting

| Symptom | Check first | Safe response |
| --- | --- | --- |
| `session-manager-plugin` not found | Local CLI/plugin installation | Install the Session Manager plugin, reopen PowerShell, retry. |
| SSM says instance is not connected | EC2 is running; wait for cloud-init | Wait a few minutes; verify the managed-node status in Systems Manager. |
| Docker Compose exits or a service is unhealthy | `docker compose ... ps` and `logs --tail=120 SERVICE` | Fix the reported service/configuration issue; do not delete volumes to hide it. |
| Browser cannot open localhost:8088 | The Connect command is still running | Keep the SSM port-forward terminal open and retry the browser. |
| Login does not work | `/opt/street-smart/.env` admin values and User Service logs | Confirm the configured email/password and restart only after correcting `.env`. |
| You are near the two-hour limit | `systemctl list-timers lab-stop.timer` | Finish the demo or stop/start later; the timer can interrupt builds. |

## When to use the public-production overlay

`compose.production.yml` is for a later public demo with a real domain, DNS and Caddy-managed TLS. It exposes ports 80/443 and requires `PUBLIC_DOMAIN`; it is not part of this cost-conscious private lab. Do not add it to the commands in this guide unless you intentionally want a public deployment and have completed DNS/TLS preparation.
