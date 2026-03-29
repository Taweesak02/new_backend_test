# Backend API

REST API ที่พัฒนาด้วย Spring Boot 4 รองรับการยืนยันตัวตนด้วย JWT, การควบคุมสิทธิ์ตาม Role และมีเอกสาร API ครบถ้วนผ่าน Swagger UI

---

## เทคโนโลยีที่ใช้

- **Java 21** + **Spring Boot 4**
- **Spring Security** — ยืนยันตัวตนด้วย JWT Bearer Token
- **Spring Data JPA** + **MySQL**
- **Flyway** — จัดการ Database Migration
- **springdoc-openapi** — เอกสาร API ผ่าน Swagger UI
- **H2** — ฐานข้อมูลในหน่วยความจำสำหรับ Testing
- **Lombok**
- **Docker** + **Docker Compose**

---

## การติดตั้งและรันโปรเจกต์

### สิ่งที่ต้องมีก่อน

- [Docker](https://www.docker.com/products/docker-desktop)
- [Docker Compose](https://docs.docker.com/compose/)

### 1. Clone โปรเจกต์

```bash
git clone https://github.com/Taweesak02/new_backend_test.git
cd new_backend_test
```

### 2. ตั้งค่า Environment

คัดลอก `.env.example` เป็น `.env` แล้วกรอกข้อมูลให้ครบ:

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=your_database
DB_USERNAME=your_username
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
```

### 3. รันด้วย Docker Compose

```bash
docker-compose up --build
```

เซิร์ฟเวอร์จะเริ่มทำงานที่ `http://localhost:8080`

### หยุดการทำงาน

```bash
docker-compose down
```

---

## เอกสาร API (Swagger UI)

เข้าถึงได้ที่:

```
http://localhost:8080/swagger-ui.html
```

### วิธีใช้งาน Authentication ใน Swagger

1. เรียก `POST /api/auth/login` เพื่อรับ Token
2. กดปุ่ม **Authorize** (มุมบนขวา)
3. กรอก `Bearer <your_token>`
4. กด Authorize แล้วทดสอบ Endpoint ได้เลย

> Endpoint ที่ต้องการสิทธิ์จะมีไอคอนกุญแจ 🔒

---

## Endpoints ทั้งหมด

### Auth (ไม่ต้องใช้ Token)

| Method | Endpoint | คำอธิบาย |
|--------|----------|----------|
| POST | `/api/auth/register` | สมัครสมาชิกใหม่ |
| POST | `/api/auth/login` | เข้าสู่ระบบและรับ Token |

#### POST `/api/auth/register`

Request:
```json
{
  "name": "Test User",
  "email": "user@test.com",
  "password": "password123"
}
```

Response `200`:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "accessToken": "eyJ..."
  }
}
```

Response `409` (Email ซ้ำ):
```json
{
  "success": false,
  "message": "Email already exists"
}
```

#### POST `/api/auth/login`

Request:
```json
{
  "email": "admin@example.com",
  "password": "password"
}
```

Response `200`:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ..."
  }
}
```

---

### Users (ต้องใช้ Token)

| Method | Endpoint | คำอธิบาย | สิทธิ์ที่ต้องการ |
|--------|----------|----------|----------------|
| GET | `/api/users` | ดูรายชื่อผู้ใช้ทั้งหมด | ADMIN เท่านั้น |
| GET | `/api/users/{id}` | ดูข้อมูลผู้ใช้ตาม ID | ADMIN หรือ เจ้าของบัญชี |
| DELETE | `/api/users/{id}` | ลบผู้ใช้ | ADMIN เท่านั้น |

#### GET `/api/users`

Response `200`:
```json
{
  "success": true,
  "data": [
    { "id": 1, "name": "Admin", "email": "admin@example.com" }
  ]
}
```

Response `403` (ไม่ใช่ Admin):
```json
{
  "success": false,
  "message": "Forbidden"
}
```

#### DELETE `/api/users/{id}`

Response `200`:
```json
{
  "success": true,
  "message": "User deleted successfully"
}
```

---

## Error Response ทั้งหมด

| Status | ความหมาย |
|--------|----------|
| 400 | Bad Request — ข้อมูลไม่ถูกต้อง (Validation failed) |
| 401 | Unauthorized — ไม่มี Token หรือ Token ไม่ถูกต้อง |
| 403 | Forbidden — ไม่มีสิทธิ์เข้าถึง |
| 404 | Not Found — ไม่พบข้อมูล |
| 409 | Conflict — Email ซ้ำในระบบ |

ตัวอย่าง `400`:
```json
{
  "success": false,
  "errors": {
    "email": "must be a valid email",
    "password": "size must be between 6 and 255"
  }
}
```

---



## Database Migration

Flyway จัดการ Schema อัตโนมัติเมื่อ Start โปรเจกต์ ไฟล์ Migration อยู่ที่:

```
src/main/resources/db/migration/
```

---

## โครงสร้างโปรเจกต์

```
src/
├── main/
│   ├── java/com/test/backend/
│   │   ├── config/         # Security Config, Swagger Config
│   │   ├── controller/     # AuthController, UserController
│   │   ├── dto/            # Request / Response DTOs
│   │   ├── model/          # JPA Entities
│   │   ├── repository/     # Spring Data Repositories
│   │   └── service/        # Business Logic
│   └── resources/
│       ├── db/migration/   # Flyway SQL Migrations
│       └── application.properties
└── test/
    └── java/com/test/backend/
        ├── auth/           # AuthControllerTest
        └── user/           # UserControllerTest
```