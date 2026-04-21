# URL Shortener — Backend API

A URL shortening service with click analytics, built with Spring Boot and deployed on AWS with production-grade networking.

## Architecture

```
Internet
    ↓
Application Load Balancer (public subnet)
    ↓
EC2 — Spring Boot (private subnet)
    ├── RDS PostgreSQL (private subnet)
    └── ElastiCache Redis (private subnet)
```

## Tech Stack

- **Framework:** Spring Boot 4.0 (Java 21)
- **Database:** PostgreSQL 16 (AWS RDS)
- **Cache:** Redis 7 (AWS ElastiCache)
- **Migrations:** Flyway
- **Build:** Maven

## Features

**URL Shortening**
- Base62 encoding of database auto-increment IDs — no collisions, no extra DB roundtrips
- Input validation on URLs

**Redirect**
- `GET /{shortCode}` returns a 301 redirect
- Redis cache-aside pattern on the read path — first request hits Postgres, subsequent requests resolve in sub-millisecond from Redis
- 24-hour sliding TTL: frequently accessed URLs stay cached, cold ones expire naturally

**Click Analytics**
- Every redirect fires an async click event to a background thread pool — the user gets their 301 before the click is persisted
- Tracks device type (parsed from User-Agent), referrer, and timestamp
- IP addresses are SHA-256 hashed before storage (GDPR-friendly, still supports unique visitor counts)
- Analytics endpoint returns total clicks, device breakdown, and clicks-per-day time series

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/shorten` | Shorten a URL |
| `GET` | `/{shortCode}` | Redirect to original URL |
| `GET` | `/api/analytics/{shortCode}` | Get click analytics |

### Shorten a URL

```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"longUrl": "https://www.example.com"}'
```

Response:

```json
{
  "shortUrl": "http://localhost:8080/hfQO",
  "shortCode": "hfQO",
  "longUrl": "https://www.example.com"
}
```

### Get Analytics

```bash
curl http://localhost:8080/api/analytics/hfQO
```

Response:

```json
{
  "shortCode": "hfQO",
  "totalClicks": 42,
  "deviceBreakdown": { "desktop": 30, "mobile": 10, "bot": 2 },
  "clicksPerDay": [
    { "date": "2026-04-18", "clicks": 15 },
    { "date": "2026-04-19", "clicks": 27 }
  ]
}
```

## Local Development

### Prerequisites

- Java 21
- Maven 3.9+
- Docker

### Setup

1. Start Postgres and Redis:

```bash
docker compose up -d
```

2. Run the application:

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

## Database Schema

Managed by Flyway. Migrations are in `src/main/resources/db/migration/`.

**urls** — stores shortened URLs with Base62-encoded short codes

**clicks** — stores click events with device type, referrer, IP hash, and timestamp. Indexed on `short_code` and `clicked_at` for analytics queries.

## AWS Deployment

### Infrastructure

| Resource | Service | Spec |
|----------|---------|------|
| Networking | VPC | 2 public + 2 private subnets across 2 AZs |
| Load Balancer | ALB | Internet-facing, public subnets |
| Compute | EC2 | t3.micro, private subnet |
| Database | RDS PostgreSQL | db.t4g.micro, private subnet |
| Cache | ElastiCache Redis | cache.t4g.micro, private subnet |
| Artifacts | S3 | Deployment jar storage |

### Security

Security groups are chained: internet → ALB (port 80) → EC2 (port 8080) → RDS (port 5432) / Redis (port 6379). Nothing in the private subnets is directly accessible from the internet.

Production config uses environment variables for all credentials — no secrets in code or config files.

### Deploy

1. Build the jar:

```bash
mvn clean package -DskipTests
```

2. Upload to S3:

```bash
aws s3 cp target/url-shortner-0.0.1-SNAPSHOT.jar s3://your-bucket/app.jar
```

3. On EC2, pull and restart:

```bash
sudo aws s3 cp s3://your-bucket/app.jar /home/ec2-user/app.jar
sudo systemctl restart urlshortener
```

## Design Decisions

**Base62 encoding vs random codes** — Using the DB auto-increment ID as the source means no collision handling and no extra DB queries. The tradeoff is sequential codes are guessable; mitigated by starting the sequence at 1,000,000.

**Cache-aside vs write-through** — The app manages the cache explicitly. On writes, it pre-warms the cache. On reads, it checks Redis first, falls back to Postgres, then populates Redis. This keeps the cache logic visible and testable.

**Async click tracking** — Click events are processed on a dedicated thread pool (`@Async`), decoupled from the redirect response. The redirect completes in milliseconds regardless of database write latency. Designed to be swappable with SQS for production scale.

**301 vs 302 redirects** — Chose 301 (permanent). Browsers cache the redirect, reducing server load. Tradeoff: repeat visits from the same browser aren't tracked in analytics.

**IP hashing** — SHA-256 hash stored instead of raw IPs. Enables unique visitor counting without storing PII.
