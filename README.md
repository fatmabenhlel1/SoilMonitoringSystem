# 🌱 Soil Monitoring System

<div align="center">

![Project Status](https://img.shields.io/badge/status-production-success)
![SSL Rating](https://img.shields.io/badge/SSL-A+-brightgreen)
![TLS Version](https://img.shields.io/badge/TLS-1.3-blue)
![License](https://img.shields.io/badge/license-MIT-blue)

**A comprehensive IoT-based agricultural monitoring platform for real-time soil analysis and smart farming**

[Live Demo](https://www.soilmonitoring.me) • [API Docs](#api-documentation) • [Report Bug](https://github.com/yourusername/soil-monitoring/issues) • [Request Feature](https://github.com/yourusername/soil-monitoring/issues)

</div>

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Live Deployment](#live-deployment)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Authentication & Authorization](#authentication--authorization)
- [IoT Integration](#iot-integration)
- [Security](#security)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

---

## 🌟 Overview

The **Soil Monitoring System** is an enterprise-grade agricultural IoT platform that enables farmers and agricultural professionals to monitor soil conditions in real-time. The system collects data from IoT sensors, analyzes soil parameters, triggers alerts for anomalies, and provides actionable insights for precision agriculture.

### Key Highlights

- 📊 **Real-time Monitoring** - Live sensor data streaming via WebSocket
- 🔔 **Smart Alerts** - Automated notifications for critical soil conditions
- 🔐 **Enterprise Security** - OAuth 2.0/OIDC with JWT authentication
- 📱 **Progressive Web App** - Works offline, installable on any device
- 🌍 **Multi-tenant** - Support for multiple organizations and farms
- 📈 **Analytics Dashboard** - Historical trends and data visualization
- 🔌 **MQTT Integration** - Industry-standard IoT communication protocol

---

## ✨ Features

### For Farmers

- ✅ **Multi-Field Management** - Monitor multiple agricultural fields from one dashboard
- ✅ **Sensor Registration** - Easy sensor setup and configuration
- ✅ **Real-time Data** - Live soil parameters (NPK, pH, moisture, temperature)
- ✅ **Alert System** - Instant notifications for critical conditions
- ✅ **Historical Analysis** - Track trends and make data-driven decisions
- ✅ **Mobile-First Design** - Responsive interface for smartphones and tablets

### For Administrators

- ✅ **User Management** - Role-based access control (Admin, Farmer)
- ✅ **Tenant Management** - Multi-organization support
- ✅ **System Monitoring** - Health checks and performance metrics
- ✅ **Audit Logs** - Complete activity tracking
- ✅ **Configuration** - Flexible alert thresholds and rules

### Technical Features

- ✅ **RESTful API** - Complete CRUD operations for all resources
- ✅ **WebSocket Streaming** - Real-time bidirectional communication
- ✅ **MQTT Broker Integration** - HiveMQ Cloud for sensor data ingestion
- ✅ **JWT Authentication** - Secure token-based auth with refresh tokens
- ✅ **MongoDB Atlas** - Scalable NoSQL cloud database
- ✅ **Email Notifications** - SMTP integration for alerts and activation
- ✅ **PWA Support** - Offline capability and installability

---

## 🏗️ Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         User Clients                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │ Web App  │  │  Mobile  │  │  Tablet  │  │  Desktop │       │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘       │
│       │             │              │             │              │
└───────┼─────────────┼──────────────┼─────────────┼──────────────┘
        │             │              │             │
        └─────────────┴──────────────┴─────────────┘
                          │
                     HTTPS (TLS 1.3)
                          │
        ┌─────────────────┴─────────────────┐
        │      Nginx Reverse Proxy          │
        │  (SSL Termination, Load Balance)  │
        └─────────────────┬─────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
┌───────▼────────┐ ┌─────▼──────┐ ┌────────▼────────┐
│   Frontend     │ │    IAM     │ │   API Service   │
│  Static Files  │ │  Service   │ │   (WildFly)     │
│     (Nginx)    │ │ (WildFly)  │ │   Port 8080     │
└────────────────┘ │ Port 8180  │ └────────┬────────┘
                   └────────────┘          │
                                           │
        ┌──────────────────────────────────┼──────────────┐
        │                                  │              │
┌───────▼────────┐              ┌─────────▼──────┐  ┌───▼────────┐
│  MongoDB Atlas │              │  MQTT Broker   │  │   SMTP     │
│   (Database)   │              │  (HiveMQ)      │  │  (Gmail)   │
└────────────────┘              └────────┬───────┘  └────────────┘
                                         │
                                ┌────────▼────────┐
                                │   IoT Sensors   │
                                │  (NPK, pH, etc) │
                                └─────────────────┘
```

### Microservices Architecture

The system is built as three independent microservices:

1. **Frontend Service** (`www.soilmonitoring.me`)
   - Progressive Web Application
   - Static files served by Nginx
   - Service Worker for offline capability
   - Real-time WebSocket connection

2. **IAM Service** (`iam.soilmonitoring.me`)
   - Identity and Access Management
   - OAuth 2.0 / OpenID Connect provider
   - User registration and activation
   - JWT token generation and validation
   - Role-based access control

3. **API Service** (`api.soilmonitoring.me`)
   - Core business logic
   - Sensor data management
   - MQTT message processing
   - Alert system
   - WebSocket broadcasting

---

## 🛠️ Technology Stack

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| **WildFly** | 38.0.0 | Jakarta EE application server |
| **Jakarta EE** | 10 | Enterprise Java platform |
| **JAX-RS** | 3.1 | RESTful web services |
| **CDI** | 4.0 | Dependency injection |
| **JNoSQL** | 1.1.2 | NoSQL database abstraction |
| **MicroProfile** | 6.1 | Cloud-native Java specifications |
| **Eclipse Paho** | 1.2.5 | MQTT client library |
| **Argon2** | 2.11 | Password hashing |
| **JWT (jose4j)** | 0.9.6 | JSON Web Tokens |

### Frontend

| Technology | Purpose |
|------------|---------|
| **HTML5** | Semantic markup |
| **CSS3** | Responsive styling |
| **JavaScript ES6+** | Client-side logic |
| **Chart.js** | Data visualization |
| **Service Workers** | PWA offline support |
| **WebSocket API** | Real-time communication |

### Database & External Services

| Service | Purpose |
|---------|---------|
| **MongoDB Atlas** | Cloud NoSQL database |
| **HiveMQ Cloud** | MQTT broker (IoT messaging) |
| **Gmail SMTP** | Email notifications |
| **Let's Encrypt** | SSL/TLS certificates |

### DevOps & Infrastructure

| Technology | Purpose |
|------------|---------|
| **AWS EC2** | Cloud hosting (t2.large) |
| **Amazon Linux 2023** | Operating system |
| **Nginx** | Reverse proxy & SSL termination |
| **Certbot** | SSL certificate management |
| **Maven** | Build automation |
| **Git** | Version control |

---

## 🌐 Live Deployment

### Production URLs

| Service | URL | Status |
|---------|-----|--------|
| **Frontend** | https://www.soilmonitoring.me | ![Status](https://img.shields.io/badge/status-online-success) |
| **API** | https://api.soilmonitoring.me/api | ![Status](https://img.shields.io/badge/status-online-success) |
| **IAM** | https://iam.soilmonitoring.me/iam | ![Status](https://img.shields.io/badge/status-online-success) |
| **WebSocket** | wss://api.soilmonitoring.me/ws/sensor-data | ![Status](https://img.shields.io/badge/status-online-success) |

### Infrastructure

- **Hosting**: AWS EC2 (US East 1)
- **Instance Type**: t2.large (2 vCPU, 8GB RAM)
- **IP Address**: Elastic IP (permanent)
- **SSL Rating**: A+ (SSL Labs)
- **Uptime**: 99.9% target

---

## 🚀 Getting Started

### Prerequisites

- **JDK 21+** (Amazon Corretto or OpenJDK)
- **Maven 3.8+**
- **WildFly 38** (or compatible Jakarta EE 10 server)
- **MongoDB** (local or Atlas account)
- **MQTT Broker** (HiveMQ Cloud or Mosquitto)
- **Git**

### Clone the Repository

```bash
git clone https://github.com/yourusername/soil-monitoring-system.git
cd soil-monitoring-system
```

### Project Structure

```
soil-monitoring-system/
├── soilmonitoring-api/          # Core API service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/me/soilmonitoring/api/
│   │   │   │   ├── boundaries/      # REST endpoints
│   │   │   │   ├── entities/        # Domain models
│   │   │   │   ├── controllers/     # Business logic
│   │   │   │   ├── services/        # Application services
│   │   │   │   ├── mqtt/            # MQTT integration
│   │   │   │   ├── websocket/       # WebSocket endpoints
│   │   │   │   ├── observers/       # Event observers
│   │   │   │   └── config/          # Configuration
│   │   │   └── resources/
│   │   │       └── META-INF/
│   │   │           └── microprofile-config.properties
│   │   └── test/
│   └── pom.xml
│
├── soilmonitoring-iam/          # IAM service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/me/soilmonitoring/iam/
│   │   │   │   ├── boundaries/      # Authentication endpoints
│   │   │   │   ├── entities/        # Identity models
│   │   │   │   ├── controllers/     # IAM logic
│   │   │   │   ├── services/        # Auth services
│   │   │   │   ├── security/        # JWT, Argon2, etc.
│   │   │   │   ├── enums/           # Roles, scopes
│   │   │   │   └── config/          # CORS, filters
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
│
├── frontend/                    # Progressive Web App
│   ├── index.html              # Landing page
│   ├── pages/                  # Application pages
│   │   ├── login.html
│   │   ├── register.html
│   │   ├── user.html           # Main dashboard
│   │   └── callback.html       # OAuth callback
│   ├── js/                     # JavaScript modules
│   │   ├── config.js           # API configuration
│   │   ├── api-service.js      # API client
│   │   ├── auth.js             # Authentication
│   │   └── user.js             # Dashboard logic
│   ├── css/                    # Stylesheets
│   ├── assets/                 # Images, icons
│   ├── manifest.json           # PWA manifest
│   └── sw.js                   # Service worker
│
├── DEPLOYMENT.md               # Deployment guide
├── QUICK-REFERENCE.md          # Command reference
├── README.md                   # This file
└── .gitignore
```

---

## 💻 Local Development Setup

### 1. Set Up Environment Variables

Create `microprofile-config.properties` in both API and IAM projects:

**API Service** (`soilmonitoring-api/src/main/resources/META-INF/`):

```properties
# MongoDB
jnosql.document=document
jnosql.document.database=soilmonitoring_db
jnosql.mongodb.url=mongodb://localhost:27017
document.provider=org.eclipse.jnosql.databases.mongodb.communication.MongoDBDocumentConfiguration

# MQTT
mqtt.broker.url=localhost
mqtt.broker.port=1883
mqtt.username=admin
mqtt.password=password
mqtt.client.id=soil-monitoring-api-local
mqtt.use.tls=false

# IAM Service
iam.service.url=http://localhost:8180/iam
iam.jwk.url=http://localhost:8180/iam/jwk
iam.issuer=urn:cot-app-sec:iam
```

**IAM Service** (`soilmonitoring-iam/src/main/resources/META-INF/`):

```properties
# MongoDB
jnosql.document=document
jnosql.document.database=soilmonitoring_db
jnosql.mongodb.url=mongodb://localhost:27017
document.provider=org.eclipse.jnosql.databases.mongodb.communication.MongoDBDocumentConfiguration

# JWT
jwt.realm=soilmonitoring
jwt.lifetime.duration=1020
jwt.claim.roles=groups

# Roles
roles=Admin,Farmer

# SMTP (for local testing, use Gmail)
smtp.host=smtp.gmail.com
smtp.port=587
smtp.username=your-email@gmail.com
smtp.password=your-app-password
smtp.starttls.enable=true
```

### 2. Build the Projects

```bash
# Build API
cd soilmonitoring-api
mvn clean package

# Build IAM
cd ../soilmonitoring-iam
mvn clean package
```

This generates:
- `soilmonitoring-api/target/api-1.0.war`
- `soilmonitoring-iam/target/iam-1.0.war`

### 3. Deploy to WildFly

#### Option A: Single WildFly Instance (Quick Start)

```bash
# Start WildFly
cd /path/to/wildfly/bin
./standalone.sh

# Deploy (in another terminal)
cp soilmonitoring-api/target/api-1.0.war /path/to/wildfly/standalone/deployments/
cp soilmonitoring-iam/target/iam-1.0.war /path/to/wildfly/standalone/deployments/
```

Access:
- API: http://localhost:8080/api/
- IAM: http://localhost:8080/iam/

#### Option B: Separate Instances (Recommended)

Run IAM on port 8180 using port offset:

```bash
# Terminal 1 - API
./standalone.sh

# Terminal 2 - IAM (with port offset)
./standalone.sh -Djboss.socket.binding.port-offset=100
```

Access:
- API: http://localhost:8080/api/
- IAM: http://localhost:8180/iam/

### 4. Run Frontend Locally

```bash
cd frontend

# Update config.js with local URLs
# API: http://localhost:8080/api/
# IAM: http://localhost:8180/iam/

# Serve with any HTTP server
python -m http.server 5500
# or
npx http-server -p 5500
```

Access: http://localhost:5500

---

## 📡 API Documentation

### Base URLs

- **Production API**: `https://api.soilmonitoring.me/api`
- **Production IAM**: `https://iam.soilmonitoring.me/iam`
- **Local API**: `http://localhost:8080/api`
- **Local IAM**: `http://localhost:8180/iam`

### IAM Endpoints

#### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/iam/register` | Register new user | ❌ |
| POST | `/iam/activate` | Activate account with code | ❌ |
| GET | `/iam/authorize` | OAuth 2.0 authorization | ❌ |
| POST | `/iam/token` | Exchange code for tokens | ❌ |
| POST | `/iam/token` (refresh) | Refresh access token | ❌ |
| GET | `/iam/jwk` | JSON Web Key Set | ❌ |
| GET | `/iam/.well-known/openid-configuration` | OpenID Discovery | ❌ |

#### User Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/iam/users` | List all users | ✅ Admin |
| GET | `/iam/users/{id}` | Get user details | ✅ |
| PUT | `/iam/users/{id}` | Update user | ✅ |
| DELETE | `/iam/users/{id}` | Delete user | ✅ Admin |

### API Service Endpoints

#### Fields

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/fields` | List user's fields | ✅ |
| POST | `/api/fields` | Create new field | ✅ |
| GET | `/api/fields/{id}` | Get field details | ✅ |
| PUT | `/api/fields/{id}` | Update field | ✅ |
| DELETE | `/api/fields/{id}` | Delete field | ✅ |

#### Sensors

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/sensors` | List all sensors | ✅ |
| POST | `/api/sensors` | Register new sensor | ✅ |
| GET | `/api/sensors/{id}` | Get sensor details | ✅ |
| PUT | `/api/sensors/{id}` | Update sensor | ✅ |
| DELETE | `/api/sensors/{id}` | Delete sensor | ✅ |
| GET | `/api/sensors/field/{fieldId}` | Get sensors by field | ✅ |

#### Sensor Data (Readings)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/readings` | List readings | ✅ |
| GET | `/api/readings/field/{fieldId}` | Readings by field | ✅ |
| GET | `/api/readings/sensor/{sensorId}` | Readings by sensor | ✅ |
| GET | `/api/readings/latest/{sensorId}` | Latest reading | ✅ |
| POST | `/api/readings/simulate` | Simulate sensor data | ✅ Admin |

#### Alerts

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/alerts` | List user's alerts | ✅ |
| GET | `/api/alerts/field/{fieldId}` | Alerts by field | ✅ |
| PUT | `/api/alerts/{id}` | Mark alert as read | ✅ |
| DELETE | `/api/alerts/{id}` | Delete alert | ✅ |

#### WebSocket

| Endpoint | Description | Auth Required |
|----------|-------------|---------------|
| `wss://api.soilmonitoring.me/ws/sensor-data` | Real-time sensor updates | ✅ |

**WebSocket Message Format:**

```json
{
  "type": "SENSOR_DATA",
  "payload": {
    "id": "reading-uuid",
    "fieldId": "field-uuid",
    "sensorId": "sensor-uuid",
    "temperature": 25.5,
    "humidity": 65.2,
    "nitrogen": 45.0,
    "phosphorus": 30.0,
    "potassium": 35.0,
    "soilMoisture": 55.0,
    "pH": 6.8,
    "rainfall": 0.0,
    "timestamp": "2026-01-11T10:30:00"
  }
}
```

---

## 🔐 Authentication & Authorization

### OAuth 2.0 / OpenID Connect Flow

The system implements **Authorization Code Flow with PKCE** for secure authentication:

```
┌──────────┐                                  ┌──────────┐
│          │                                  │          │
│  Client  │                                  │   IAM    │
│  (PWA)   │                                  │ Service  │
│          │                                  │          │
└────┬─────┘                                  └────┬─────┘
     │                                             │
     │  1. Authorization Request                  │
     │    + PKCE code_challenge                   │
     │───────────────────────────────────────────>│
     │                                             │
     │  2. Login Page (if not authenticated)      │
     │<───────────────────────────────────────────│
     │                                             │
     │  3. User Credentials                       │
     │───────────────────────────────────────────>│
     │                                             │
     │  4. Authorization Code                     │
     │<───────────────────────────────────────────│
     │                                             │
     │  5. Token Request                          │
     │    + PKCE code_verifier                    │
     │───────────────────────────────────────────>│
     │                                             │
     │  6. Access Token + Refresh Token           │
     │<───────────────────────────────────────────│
     │                                             │
     │  7. API Request                            │
     │    Authorization: Bearer <token>           │
     │────────────────────────────────────>       │
     │                                    ┌────────▼──────┐
     │  8. API Response                   │      API      │
     │<───────────────────────────────────│    Service    │
     │                                    └───────────────┘
```

### Roles & Permissions

| Role | Permissions |
|------|-------------|
| **Admin** | Full system access, user management, sensor simulation |
| **Farmer** | Manage own fields, sensors, view readings, receive alerts |

### JWT Token Structure

**Access Token** (expires in 17 minutes):
```json
{
  "sub": "username",
  "groups": ["Farmer"],
  "iss": "urn:cot-app-sec:iam",
  "exp": 1736593200,
  "iat": 1736592180
}
```

**Refresh Token** (expires in 7 days):
- Used to obtain new access tokens without re-authentication

---

## 🔌 IoT Integration

### MQTT Protocol

The system uses **MQTT (Message Queuing Telemetry Transport)** for IoT sensor communication:

- **Broker**: HiveMQ Cloud (TLS 1.3 encrypted)
- **Port**: 8883 (TLS)
- **Topic Pattern**: `soil-monitoring/{fieldId}/{sensorId}/data`

### Sensor Data Format

```json
{
  "sensorId": "sensor-001",
  "fieldId": "field-uuid",
  "timestamp": "2026-01-11T10:30:00Z",
  "data": {
    "temperature": 25.5,
    "humidity": 65.2,
    "nitrogen": 45.0,
    "phosphorus": 30.0,
    "potassium": 35.0,
    "soilMoisture": 55.0,
    "pH": 6.8,
    "rainfall": 0.0
  }
}
```

### Supported Sensor Types

- **NPK Sensors** (Nitrogen, Phosphorus, Potassium)
- **pH Sensors**
- **Soil Moisture Sensors**
- **Temperature & Humidity Sensors**
- **Rain Gauges**

### Alert Thresholds

The system monitors sensor readings and triggers alerts when values exceed thresholds:

| Parameter | Critical Low | Warning Low | Optimal Range | Warning High | Critical High |
|-----------|--------------|-------------|---------------|--------------|---------------|
| **Nitrogen (N)** | < 20 mg/kg | 20-30 | 30-50 | 50-60 | > 60 |
| **Phosphorus (P)** | < 10 mg/kg | 10-15 | 15-40 | 40-50 | > 50 |
| **Potassium (K)** | < 20 mg/kg | 20-30 | 30-50 | 50-60 | > 60 |
| **pH** | < 5.5 | 5.5-6.0 | 6.0-7.5 | 7.5-8.0 | > 8.0 |
| **Soil Moisture** | < 20% | 20-30% | 30-60% | 60-70% | > 70% |
| **Temperature** | < 10°C | 10-15°C | 15-30°C | 30-35°C | > 35°C |

---

## 🛡️ Security

### Security Features

✅ **TLS 1.3 Encryption** - All traffic encrypted with latest TLS protocol  
✅ **HSTS Enabled** - HTTP Strict Transport Security with preload  
✅ **CAA Records** - Certificate Authority Authorization configured  
✅ **CORS Protection** - Controlled cross-origin access  
✅ **JWT Authentication** - Stateless token-based auth  
✅ **Argon2 Password Hashing** - Industry-standard password security  
✅ **PKCE Flow** - Proof Key for Code Exchange for public clients  
✅ **XSS Protection** - X-XSS-Protection headers  
✅ **CSRF Protection** - Token-based CSRF prevention  
✅ **Input Validation** - Server-side validation for all inputs  
✅ **SQL Injection Prevention** - NoSQL with JNoSQL abstraction  
✅ **Rate Limiting** - (Planned) API request throttling  

### SSL/TLS Configuration

**Protocols**: TLS 1.3, TLS 1.2  
**Cipher Suites**:
- TLS_AES_256_GCM_SHA384
- TLS_CHACHA20_POLY1305_SHA256
- TLS_AES_128_GCM_SHA256
- ECDHE-RSA-AES256-GCM-SHA384
- ECDHE-RSA-CHACHA20-POLY1305

**SSL Rating**: [A+ on SSL Labs](https://www.ssllabs.com/ssltest/analyze.html?d=www.soilmonitoring.me)

### Security Headers

```
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
```

---

## 🚀 Deployment

### Production Deployment

The application is deployed on **AWS EC2** with the following architecture:

- **Web Server**: Nginx (reverse proxy, SSL termination)
- **Application Servers**: 
  - WildFly API (Port 8080)
  - WildFly IAM (Port 8180)
- **Database**: MongoDB Atlas (cloud)
- **MQTT Broker**: HiveMQ Cloud
- **SSL Certificates**: Let's Encrypt (auto-renewal)

**Detailed deployment instructions**: See [DEPLOYMENT.md](DEPLOYMENT.md)

**Quick reference**: See [QUICK-REFERENCE.md](QUICK-REFERENCE.md)

### Infrastructure as Code

**DNS Configuration (Namecheap)**:
```
www.soilmonitoring.me  →  A Record  →  44.212.102.155
api.soilmonitoring.me  →  A Record  →  44.212.102.155
iam.soilmonitoring.me  →  A Record  →  44.212.102.155

CAA Records:
@ → 0 issue "letsencrypt.org"
@ → 0 issuewild "letsencrypt.org"
```

### Continuous Deployment

**Manual Deployment Process**:

1. Build WAR files locally:
   ```bash
   mvn clean package
   ```

2. Upload via WinSCP:
   - `api-1.0.war` → `/opt/wildfly/standalone/deployments/`
   - `iam-1.0.war` → `/opt/wildfly-iam/standalone/deployments/`

3. WildFly auto-deploys on file change

4. Verify deployment:
   ```bash
   curl https://api.soilmonitoring.me/api/test
   ```

**Future Improvement**: CI/CD pipeline with GitHub Actions

---

## 📊 Database Schema

### Collections (MongoDB)

#### **Identity** (IAM Database)
```json
{
  "_id": "uuid",
  "username": "john_farmer",
  "password": "$argon2id$...",  // Hashed
  "email": "john@example.com",
  "roles": "Farmer",
  "scopes": "resource:read,resource:write",
  "accountActivated": true,
  "creationDate": "2026-01-10"
}
```

#### **Tenant** (IAM Database)
```json
{
  "_id": "uuid",
  "name": "Green Valley Farms",
  "description": "Organic farming cooperative",
  "createdAt": "2026-01-10T10:00:00Z",
  "updatedAt": "2026-01-10T10:00:00Z"
}
```

#### **Grant** (IAM Database)
```json
{
  "_id": "uuid",
  "identityId": "user-uuid",
  "tenantId": "tenant-uuid",
  "roles": ["Farmer"],
  "grantedAt": "2026-01-10T10:00:00Z"
}
```

#### **Field**
```json
{
  "_id": "uuid",
  "name": "North Field",
  "location": "45.4215° N, 75.6972° W",
  "area": 5.5,
  "cropType": "Wheat",
  "userId": "user-uuid",
  "createdAt": "2026-01-10T10:00:00Z"
}
```

#### **Sensor**
```json
{
  "_id": "uuid",
  "name": "NPK-001",
  "type": "NPK",
  "fieldId": "field-uuid",
  "status": "ACTIVE",
  "lastReading": "2026-01-11T10:30:00Z",
  "createdAt": "2026-01-10T10:00:00Z"
}
```

#### **SensorReading**
```json
{
  "_id": "uuid",
  "sensorId": "sensor-uuid",
  "fieldId": "field-uuid",
  "timestamp": "2026-01-11T10:30:00Z",
  "data": {
    "temperature": 25.5,
    "humidity": 65.2,
    "nitrogen": 45.0,
    "phosphorus": 30.0,
    "potassium": 35.0,
    "soilMoisture": 55.0,
    "pH": 6.8,
    "rainfall": 0.0
  }
}
```

#### **Alert**
```json
{
  "_id": "uuid",
  "fieldId": "field-uuid",
  "message": "High nitrogen levels detected",
  "severity": "WARNING",
  "alertType": "NITROGEN_HIGH",
  "isRead": false,
  "createdAt": "2026-01-11T10:35:00Z"
}
```

---

## 🧪 Testing

### Manual Testing

```bash
# Health checks
curl https://api.soilmonitoring.me/api/test
curl https://iam.soilmonitoring.me/iam/test/health

# Test authentication flow
# 1. Register user
curl -X POST https://iam.soilmonitoring.me/iam/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test123!","email":"test@example.com"}'

# 2. Activate account (check email for code)
curl -X POST https://iam.soilmonitoring.me/iam/activate \
  -H "Content-Type: application/json" \
  -d '{"code":"123456"}'

# 3. Get authorization code (browser-based OAuth flow)

# 4. Exchange code for token
curl -X POST https://iam.soilmonitoring.me/iam/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code&code=AUTH_CODE&..."

# 5. Test API with token
curl https://api.soilmonitoring.me/api/fields \
  -H "Authorization: Bearer ACCESS_TOKEN"
```

### WebSocket Testing

```javascript
// Browser console
const ws = new WebSocket('wss://api.soilmonitoring.me/ws/sensor-data');

ws.onopen = () => {
  console.log('✅ Connected');
};

ws.onmessage = (event) => {
  console.log('📡 Message:', JSON.parse(event.data));
};

ws.onerror = (error) => {
  console.error('❌ Error:', error);
};

ws.onclose = () => {
  console.log('🔌 Disconnected');
};
```

---

## 🤝 Contributing

We welcome contributions! Here's how you can help:

### Development Workflow

1. **Fork the repository**
2. **Create a feature branch**:
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Make your changes**
4. **Commit with descriptive messages**:
   ```bash
   git commit -m "feat: add sensor calibration feature"
   ```
5. **Push to your fork**:
   ```bash
   git push origin feature/amazing-feature
   ```
6. **Open a Pull Request**

### Commit Message Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `style:` - Code style changes (formatting)
- `refactor:` - Code refactoring
- `test:` - Adding tests
- `chore:` - Maintenance tasks

### Code Style

- **Java**: Follow Oracle Java Code Conventions
- **JavaScript**: Use ES6+ features, 2-space indentation
- **Comments**: Document complex logic
- **Naming**: Use descriptive variable/function names

---

## 📝 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2026 Soil Monitoring System

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 📧 Contact

**Project Maintainer**: fatma ezzahra ben helal 
**Email**: fatmaezzahra.benhelal@supcom.tn
**GitHub**: fatmabenhlel1

---

## 🙏 Acknowledgments

- **Jakarta EE Community** - For the excellent enterprise Java platform
- **WildFly Team** - For the robust application server
- **MongoDB** - For scalable NoSQL database
- **HiveMQ** - For reliable MQTT broker
- **Let's Encrypt** - For free SSL certificates
- **AWS** - For cloud infrastructure
- **All Contributors** - Thank you for making this project better!

---

## 📈 Project Status

- ✅ **Core Features**: Complete
- ✅ **Production Deployment**: Live
- ✅ **SSL/TLS**: A+ Rating


---

<div align="center">

**⭐ Star this repo if you find it helpful! ⭐**

Made with ❤️ for sustainable agriculture

[Back to Top](#-soil-monitoring-system)

</div>
