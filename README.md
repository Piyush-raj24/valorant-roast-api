# 🎯 VALORANT ROAST API

<div align="center">

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

**A high-performance Spring Boot REST API that queries live Valorant match statistics and serves dynamic, savage, and hilarious performance reviews & roasts.**

[Endpoints](#-api-endpoints) • [Quick Start](#-quick-start) • [Environment Variables](#-configuration) • [Deployment](#-deployment-to-render)

</div>

---

## 📖 Overview

The **Valorant Roast API** integrates with the unofficial HenrikDev Valorant API (v4) to extract real-time combat stats (Kills, Deaths, Assists, Headshot %, Bodyshot %, Legshot %, Agent played, Map) and runs them through a dynamic **Roast Engine**. 

Depending on your performance, accuracy, and agent choice, the engine assigns you a competitive tier (from `Radiant Menace` to `Plastic 1`) and roasts your gameplay with brutal honesty.

---

## 🏛️ System Architecture

```mermaid
flowchart LR
    Client[valorant-roast-ui] -->|GET /api/roast/{region}/{name}/{tag}| API[RoastController]
    API --> Service[ValorantService]
    API --> Engine[RoastEngine]
    Service -->|size=1 & Bearer Key| HenrikDev[HenrikDev Valorant API v4]
    HenrikDev -->|Match JSON| Service
    Engine -->|Calculates Tier + Generates Burns| API
    API -->|Consolidated JSON| Client
```

---

## 🚀 API Endpoints

### 1. Health Check
Checks service uptime and readiness for cloud health probes.
```http
GET /api/health
```

#### Sample Response:
```json
{
  "service": "valorant-roast-api",
  "status": "UP",
  "version": "1.1.0",
  "timestamp": "2026-10-16T03:45:00.000Z"
}
```

---

### 2. Player Performance Roast
Fetches the most recent match data for the specified player and generates an analytical roast.

```http
GET /api/roast/{region}/{name}/{tag}
```

#### URL Parameters:
| Parameter | Type | Allowed Values | Description |
| :--- | :--- | :--- | :--- |
| `region` | `string` | `ap`, `na`, `eu`, `kr`, `latam`, `br` | Valorant server region |
| `name` | `string` | (e.g. `TenZ`, `WinSake`) | Riot ID Username |
| `tag` | `string` | (e.g. `SEN`, `INDIA`) | Riot ID Tagline (without `#`) |

#### Query Parameters:
| Parameter | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `demo` | `boolean` | `false` | When `true`, returns simulated demo stats without consuming external API rate limits |

#### Sample Request:
```bash
curl -X GET "http://localhost:8080/api/roast/na/TenZ/SEN"
```

#### Sample Response:
```json
{
  "player": "TenZ#SEN",
  "region": "NA",
  "agent": "Jett",
  "map": "Ascent",
  "kills": 28,
  "deaths": 11,
  "assists": 7,
  "kd": "2.55",
  "headshots": 24,
  "bodyshots": 16,
  "legshots": 2,
  "hsPercent": "57.1%",
  "tier": "Radiant Menace",
  "roast": "A 2.55 K/D and 57.1% HS on Jett? Okay, you carried the lobby. But your room smells like warm radiator water and you desperately need to touch grass."
}
```

---

## ⚙️ Configuration

Configure settings via `src/main/resources/application.properties` or system environment variables:

| Environment Variable | Default Value | Description |
| :--- | :--- | :--- |
| `VALORANT_API_KEY` | *(Built-in Demo Key)* | HenrikDev API Key (obtain free key via [HenrikDev Discord](https://discord.gg/X3GaVkX2YN)) |
| `PORT` | `8080` | Server binding port (automatically assigned by Render / Railway) |

---

## 💻 Local Development

### Prerequisites
- **Java 21** or later installed (`java -version`)
- Git

### Setup & Run
```bash
# 1. Clone the repository
git clone https://github.com/Piyush-raj24/valorant-roast-api.git
cd valorant-roast-api

# 2. Run with Maven Wrapper (Windows)
.\mvnw.cmd spring-boot:run

# Or on macOS / Linux:
./mvnw spring-boot:run
```

The server will start at `http://localhost:8080`.

---

## 🐳 Docker Containerization

### Build Image:
```bash
docker build -t valorant-roast-api .
```

### Run Container:
```bash
docker run -p 8080:8080 -e VALORANT_API_KEY=your_key_here valorant-roast-api
```

---

## ☁️ Deployment to Render

1. Create a new **Web Service** on [Render](https://render.com/).
2. Connect your GitHub repository: `Piyush-raj24/valorant-roast-api`.
3. Choose **Docker** as the Runtime environment (it automatically detects the included `Dockerfile`).
4. Under **Environment Variables**, add:
   - `VALORANT_API_KEY`: *(Your HenrikDev API key)*
5. Click **Create Web Service**. Render will build and deploy the container on your custom `.onrender.com` URL.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) - Copyright (c) 2026 **Piyush Raj**.
