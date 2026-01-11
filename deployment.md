# Soil Monitoring System - Production Deployment Guide

## 🚀 Live Application
- **Frontend:** https://www.soilmonitoring.me
- **API Service:** https://api.soilmonitoring.me/api/
- **IAM Service:** https://iam.soilmonitoring.me/iam/
- **WebSocket:** wss://api.soilmonitoring.me/ws/sensor-data

## 📋 Table of Contents
1. [Infrastructure Overview](#infrastructure-overview)
2. [Prerequisites](#prerequisites)
3. [Server Setup](#server-setup)
4. [DNS Configuration](#dns-configuration)
5. [SSL/TLS Setup](#ssltls-setup)
6. [WildFly Configuration](#wildfly-configuration)
7. [Nginx Configuration](#nginx-configuration)
8. [Deployment Steps](#deployment-steps)
9. [Monitoring & Maintenance](#monitoring--maintenance)

---

## Infrastructure Overview

### AWS EC2 Instance
- **Instance Type:** t2.large
- **OS:** Amazon Linux 2023
- **Elastic IP:** 44.212.102.155 (permanent)
- **Storage:** 20 GB gp3

### Services Architecture
```
Internet
    ↓
Nginx (Port 443 - TLS Termination)
    ├─→ WildFly API (Port 8080)
    ├─→ WildFly IAM (Port 8180)
    └─→ Frontend Files
```

### External Services
- **Database:** MongoDB Atlas
- **MQTT Broker:** HiveMQ Cloud
- **Email:** Gmail SMTP
- **DNS:** Namecheap

---

## Prerequisites

### Required Accounts
- AWS account with EC2 access
- Domain name (soilmonitoring.me)
- MongoDB Atlas account
- HiveMQ Cloud account
- Gmail account with App Password

### Local Tools
- WinSCP (for file uploads)
- Git
- Maven 3.x
- JDK 21

---

## Server Setup

### 1. Launch EC2 Instance

```bash
# Instance Configuration
Instance Name: soilmonitoring-server
AMI: Amazon Linux 2023
Instance Type: t2.large
Key Pair: Create/download .ppk file
Storage: 20 GB gp3
```

### 2. Security Group Rules

| Type | Protocol | Port | Source | Description |
|------|----------|------|--------|-------------|
| SSH | TCP | 22 | Your IP | SSH access |
| HTTP | TCP | 80 | 0.0.0.0/0 | HTTP (redirects to HTTPS) |
| HTTPS | TCP | 443 | 0.0.0.0/0 | HTTPS traffic |

### 3. Allocate Elastic IP

```bash
# AWS Console → EC2 → Elastic IPs
# 1. Allocate Elastic IP
# 2. Associate with your instance
# Note the IP for DNS configuration
```

### 4. Initial Server Configuration

```bash
# Connect via EC2 Instance Connect or SSH

# Update system
sudo yum update -y

# Install Java 21
sudo yum install java-21-amazon-corretto-devel -y

# Install Nginx
sudo amazon-linux-extras install nginx1 -y

# Install Certbot
sudo yum install certbot python3-certbot-nginx -y

# Verify installations
java -version
nginx -v
certbot --version
```

---

## DNS Configuration

### Namecheap DNS Records

Go to: **Domain List → soilmonitoring.me → Advanced DNS**

Add these A Records:

| Type | Host | Value | TTL |
|------|------|-------|-----|
| A Record | www | YOUR_ELASTIC_IP | Automatic |
| A Record | api | YOUR_ELASTIC_IP | Automatic |
| A Record | iam | YOUR_ELASTIC_IP | Automatic |

Add CAA Records for Let's Encrypt:

| Type | Host | Tag | Value | TTL |
|------|------|-----|-------|-----|
| CAA | @ | issue | letsencrypt.org | Automatic |
| CAA | @ | issuewild | letsencrypt.org | Automatic |

**Wait 5-15 minutes for DNS propagation**

Verify DNS:
```bash
dig www.soilmonitoring.me +short
dig api.soilmonitoring.me +short
dig iam.soilmonitoring.me +short
# All should return your Elastic IP
```

---

## SSL/TLS Setup

### 1. Obtain SSL Certificates

```bash
# Stop Nginx to free port 80
sudo systemctl stop nginx

# Get certificate for IAM
sudo certbot certonly --standalone \
  -d iam.soilmonitoring.me \
  --agree-tos \
  --email admin@soilmonitoring.me \
  --non-interactive

# Get certificate for API
sudo certbot certonly --standalone \
  -d api.soilmonitoring.me \
  --agree-tos \
  --email admin@soilmonitoring.me \
  --non-interactive

# Get certificate for Frontend
sudo certbot certonly --standalone \
  -d www.soilmonitoring.me \
  --agree-tos \
  --email admin@soilmonitoring.me \
  --non-interactive
```

### 2. Verify Certificates

```bash
sudo certbot certificates
```

### 3. Auto-Renewal

Certbot automatically sets up renewal. Verify:

```bash
sudo systemctl list-timers | grep certbot
```

Test renewal (dry run):
```bash
sudo certbot renew --dry-run
```

---

## WildFly Configuration

### 1. Download and Install WildFly

```bash
cd /opt

# Download WildFly 38 Preview
sudo wget https://github.com/wildfly/wildfly/releases/download/38.0.0.Final/wildfly-preview-38.0.0.Final.tar.gz

# Extract
sudo tar xvf wildfly-preview-38.0.0.Final.tar.gz
sudo mv wildfly-preview-38.0.0.Final wildfly
sudo chown -R ec2-user:ec2-user /opt/wildfly

# Create IAM instance (separate)
sudo cp -r /opt/wildfly /opt/wildfly-iam
sudo chown -R ec2-user:ec2-user /opt/wildfly-iam
```

### 2. Configure Environment Variables

Edit `/opt/wildfly/bin/standalone.conf`:
```bash
# MongoDB Atlas
export JNOSQL_MONGODB_URL="mongodb+srv://..."

# MQTT HiveMQ
export MQTT_USERNAME="your_username"
export MQTT_PASSWORD="your_password"

# SMTP Gmail
export SMTP_PASSWORD="your_app_password"
```

Edit `/opt/wildfly-iam/bin/standalone.conf`:
```bash
# Add port offset for IAM (runs on 8180)
export JAVA_OPTS="$JAVA_OPTS -Djboss.socket.binding.port-offset=100"

# Add same environment variables as API
```

### 3. Create Systemd Services

**API Service:** `/etc/systemd/system/wildfly.service`
```ini
[Unit]
Description=WildFly API Application Server
After=network.target

[Service]
Type=simple
User=ec2-user
Group=ec2-user
ExecStart=/opt/wildfly/bin/standalone.sh -b=0.0.0.0
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

**IAM Service:** `/etc/systemd/system/wildfly-iam.service`
```ini
[Unit]
Description=WildFly IAM Application Server
After=network.target

[Service]
Type=simple
User=ec2-user
Group=ec2-user
ExecStart=/opt/wildfly-iam/bin/standalone.sh -b=0.0.0.0
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable services:
```bash
sudo systemctl daemon-reload
sudo systemctl enable wildfly
sudo systemctl enable wildfly-iam
```

---

## Nginx Configuration

### Complete Nginx Configuration

File: `/etc/nginx/nginx.conf`

```nginx
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log;
pid /run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';

    access_log  /var/log/nginx/access.log  main;

    sendfile            on;
    tcp_nopush          on;
    tcp_nodelay         on;
    keepalive_timeout   65;
    types_hash_max_size 4096;

    include             /etc/nginx/mime.types;
    default_type        application/octet-stream;

    # HTTP to HTTPS Redirect
    server {
        listen 80;
        listen [::]:80;
        server_name iam.soilmonitoring.me api.soilmonitoring.me www.soilmonitoring.me soilmonitoring.me;
        
        location /.well-known/acme-challenge/ {
            root /var/www/html;
        }
        
        location / {
            return 301 https://$host$request_uri;
        }
    }

    # IAM Server - HTTPS with TLS 1.3
    server {
        listen 443 ssl;
        listen [::]:443 ssl;
        http2 on;
        server_name iam.soilmonitoring.me;

        # TLS Configuration
        ssl_protocols TLSv1.3 TLSv1.2;
        ssl_prefer_server_ciphers off;
        ssl_ciphers 'ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-CHACHA20-POLY1305:ECDHE-RSA-CHACHA20-POLY1305:ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256';
        
        # SSL Certificates
        ssl_certificate /etc/letsencrypt/live/iam.soilmonitoring.me/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/iam.soilmonitoring.me/privkey.pem;
        
        # SSL Session
        ssl_session_timeout 1d;
        ssl_session_cache shared:SSL:10m;
        ssl_session_tickets off;
        
        # HSTS
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;
        
        # Security Headers
        add_header X-Frame-Options "DENY" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;
        
        # Proxy to WildFly IAM (Port 8180)
        location / {
            proxy_pass http://127.0.0.1:8180;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }

    # API Server - HTTPS with TLS 1.3
    server {
        listen 443 ssl;
        listen [::]:443 ssl;
        http2 on;
        server_name api.soilmonitoring.me;

        # TLS Configuration
        ssl_protocols TLSv1.3 TLSv1.2;
        ssl_prefer_server_ciphers off;
        ssl_ciphers 'ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-CHACHA20-POLY1305:ECDHE-RSA-CHACHA20-POLY1305:ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256';
        
        # SSL Certificates
        ssl_certificate /etc/letsencrypt/live/api.soilmonitoring.me/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/api.soilmonitoring.me/privkey.pem;
        
        # SSL Session
        ssl_session_timeout 1d;
        ssl_session_cache shared:SSL:10m;
        ssl_session_tickets off;
        
        # HSTS
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;
        
        # Security Headers
        add_header X-Frame-Options "DENY" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;
        
        # Proxy to WildFly API (Port 8080)
        location / {
            proxy_pass http://127.0.0.1:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
        
        # WebSocket Support
        location /ws/ {
            proxy_pass http://127.0.0.1:8080/ws/;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_read_timeout 86400s;
        }
    }

    # Frontend - HTTPS with TLS 1.3
    server {
        listen 443 ssl;
        listen [::]:443 ssl;
        http2 on;
        server_name www.soilmonitoring.me soilmonitoring.me;

        # TLS Configuration
        ssl_protocols TLSv1.3 TLSv1.2;
        ssl_prefer_server_ciphers off;
        ssl_ciphers 'ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-CHACHA20-POLY1305:ECDHE-RSA-CHACHA20-POLY1305:ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256';
        
        # SSL Certificates
        ssl_certificate /etc/letsencrypt/live/www.soilmonitoring.me/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/www.soilmonitoring.me/privkey.pem;
        
        # SSL Session
        ssl_session_timeout 1d;
        ssl_session_cache shared:SSL:10m;
        ssl_session_tickets off;
        
        # HSTS
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;
        
        # Security Headers
        add_header X-Frame-Options "DENY" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;
        
        # Root directory
        root /var/www/soilmonitoring-frontend;
        index index.html;
        
        # SPA fallback
        location / {
            try_files $uri $uri/ /index.html;
        }
        
        # Cache static assets
        location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
}
```

Enable and start Nginx:
```bash
sudo nginx -t
sudo systemctl enable nginx
sudo systemctl start nginx
```

---

## Deployment Steps

### 1. Build Applications Locally

```bash
# Build API
cd soil-monitoring-api
mvn clean package

# Build IAM
cd ../soil-monitoring-iam
mvn clean package
```

### 2. Prepare WAR Files

Ensure `jboss-web.xml` in both projects has:

**File:** `src/main/webapp/WEB-INF/jboss-web.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jboss-web>
    <context-root>/</context-root>
</jboss-web>
```

### 3. Update CORS Configuration

**IAM CORS Filter:**
```java
@Provider
public class CORSFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext request,
                       ContainerResponseContext response) {
        String origin = request.getHeaderString("Origin");
        
        if (origin != null && (
            origin.equals("https://www.soilmonitoring.me") ||
            origin.equals("http://127.0.0.1:5500") ||  // Local dev
            origin.equals("http://localhost:5500")
        )) {
            response.getHeaders().add("Access-Control-Allow-Origin", origin);
        }
        
        response.getHeaders().add("Access-Control-Allow-Credentials", "true");
        response.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}
```

Do the same for API CORS filter.

### 4. Upload WAR Files

Using WinSCP:
- Connect to EC2 (IP: Elastic IP, User: ec2-user, Key: .ppk file)
- Upload `api-1.0.war` → `/opt/wildfly/standalone/deployments/`
- Upload `iam-1.0.war` → `/opt/wildfly-iam/standalone/deployments/`

### 5. Start Services

```bash
sudo systemctl start wildfly
sudo systemctl start wildfly-iam
sudo systemctl start nginx
```

### 6. Verify Deployment

```bash
# Check service status
sudo systemctl status wildfly
sudo systemctl status wildfly-iam
sudo systemctl status nginx

# Check deployment logs
tail -f /opt/wildfly/standalone/log/server.log
tail -f /opt/wildfly-iam/standalone/log/server.log

# Test endpoints
curl http://localhost:8080/api/
curl http://localhost:8180/iam/
```

### 7. Deploy Frontend

```bash
# Create frontend directory
sudo mkdir -p /var/www/soilmonitoring-frontend
sudo chown -R ec2-user:ec2-user /var/www/soilmonitoring-frontend
```

Upload all frontend files via WinSCP to `/var/www/soilmonitoring-frontend/`

Update frontend config with production URLs:
- API: `https://api.soilmonitoring.me`
- IAM: `https://iam.soilmonitoring.me`
- WebSocket: `wss://api.soilmonitoring.me/ws/sensor-data`
- Redirect URI: `https://www.soilmonitoring.me/pages/callback.html`

---

## Monitoring & Maintenance

### View Logs

```bash
# WildFly API logs
sudo journalctl -u wildfly -f

# WildFly IAM logs
sudo journalctl -u wildfly-iam -f

# Nginx logs
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

### Restart Services

```bash
sudo systemctl restart wildfly
sudo systemctl restart wildfly-iam
sudo systemctl restart nginx
```

### Update Deployment

```bash
# Stop services
sudo systemctl stop wildfly
sudo systemctl stop wildfly-iam

# Upload new WAR files via WinSCP

# Start services
sudo systemctl start wildfly
sudo systemctl start wildfly-iam
```

### SSL Certificate Renewal

Automatic renewal is configured. Manual renewal:

```bash
sudo certbot renew
sudo systemctl reload nginx
```

### Security Testing

Test SSL/TLS configuration:
- https://www.ssllabs.com/ssltest/
- Target: A+ rating

### Backup Strategy

1. **Database:** MongoDB Atlas automatic backups
2. **Application:** Keep WAR files in version control
3. **Configuration:** Document all changes
4. **Environment Variables:** Store securely (not in Git)

---

## Troubleshooting

### Service Won't Start

```bash
# Check logs
sudo journalctl -u wildfly -n 50
sudo journalctl -u wildfly-iam -n 50

# Check port availability
sudo netstat -tulnp | grep -E '8080|8180|443'
```

### DNS Not Resolving

```bash
# Check DNS propagation
dig www.soilmonitoring.me +short
dig api.soilmonitoring.me +short
dig iam.soilmonitoring.me +short

# Flush local DNS (on client)
ipconfig /flushdns  # Windows
sudo systemd-resolve --flush-caches  # Linux
```

### SSL Certificate Issues

```bash
# Check certificates
sudo certbot certificates

# Test SSL
openssl s_client -connect www.soilmonitoring.me:443 -tls1_3

# Renew manually if needed
sudo certbot renew --force-renewal
```

### CORS Errors

Check:
1. CORS filter in Java code allows production domain
2. Nginx doesn't duplicate CORS headers
3. Frontend uses correct URLs

---

## Security Best Practices

✅ TLS 1.3 enabled  
✅ HSTS with preload  
✅ CAA records configured  
✅ Security headers (X-Frame-Options, X-Content-Type-Options, X-XSS-Protection)  
✅ Elastic IP (permanent)  
✅ Environment variables separated from code  
✅ Firewall configured (Security Groups)  
✅ Regular updates (`sudo yum update`)  

---

## Performance Optimization

### Current Configuration
- Instance: t2.large (2 vCPU, 8GB RAM)
- WildFly: 2 separate instances
- Nginx: HTTP/2 enabled
- Static asset caching: 1 year

### Future Improvements
- CloudFront CDN for static assets
- ElastiCache for session management
- Auto-scaling group
- Load balancer for multiple instances

---

## Cost Estimation

**Monthly AWS Costs:**
- EC2 t2.large: ~$70/month
- Elastic IP (in use): Free
- Data transfer: ~$5-10/month
- **Total: ~$75-80/month**

**External Services:**
- MongoDB Atlas: Free tier (512MB)
- HiveMQ Cloud: Free tier
- Domain: ~$10/year
- Let's Encrypt SSL: Free

---



---

## License

MIT License

---

**Last Updated:** January 11, 2026  
**SSL Rating:** A+ (SSL Labs)