# User Manager Backend API

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

- Java 21
- Maven
- MySQL (หรือ Docker)

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

### 3. รันด้วย Docker

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
    "data": {
        "accessToken": "eyJ...",
        "refreshToken": "eyJh...",
        "user": {
            "createdAt": "2026-03-30T01:20:36.593958",
            "email": "user@test.com",
            "id": 4,
            "isActive": true,
            "name": "Test User",
            "role": "user",
            "updatedAt": "2026-03-30T01:20:36.593991"
        }
    },
    "message": "Registered successfully",
    "success": true,
    "timestamp": "2026-03-30T01:20:36.602721982Z"
}
```

Response `409` (Email ซ้ำ):
```json
{
    "message": "Email already exists",
    "success": false,
    "timestamp": "2026-03-30T01:21:41.822228945Z"
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
    "data": {
        "accessToken": "eyJ...",
        "refreshToken": "eyJ...",
        "user": {
            "createdAt": "2026-03-29T17:01:55",
            "email": "admin@example.com",
            "id": 1,
            "isActive": true,
            "name": "Admin",
            "role": "admin",
            "updatedAt": "2026-03-30T01:22:27.301486"
        }
    },
    "message": "Login successful",
    "success": true,
    "timestamp": "2026-03-30T01:22:27.319505938Z"
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
    "data": {
        "pagination": {
            "currentPage": 1,
            "itemsPerPage": 10,
            "totalItems": 4,
            "totalPages": 1
        },
        "users": [
            {
                "createdAt": "2026-03-30T01:20:37",
                "email": "user@test.com",
                "id": 4,
                "isActive": true,
                "name": "Test User",
                "role": "user",
                "updatedAt": "2026-03-30T01:20:37"
            },
            ...
        ]
    },
    "message": "OK",
    "success": true,
    "timestamp": "2026-03-30T01:24:08.248566738Z"
}
```

Response `403` (ไม่ใช่ Admin):
```json
{
    "success": false,
    "message": "Unauthorized"
}
```

#### DELETE `/api/users/{id}`

Response `200`:
```json
{
    "message": "User deleted",
    "success": true,
    "timestamp": "2026-03-30T01:29:50.987514880Z"
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
    "errors": [
        {
            "field": "email",
            "message": "Invalid email format"
        },
        {
            "field": "password",
            "message": "Must be at least 8 characters"
        }
    ],
    "message": "Validation failed",
    "success": false,
    "timestamp": "2026-03-30T01:32:15.781153009Z"
}
```

---

## การรัน Tests

```bash
# รันทุก Test
./mvnw test

# รัน Test เฉพาะ Class
./mvnw test -Dtest=AuthControllerTest
./mvnw test -Dtest=UserControllerTest
```

Test ทั้งหมดใช้ **H2 In-memory Database** และครอบคลุม 15 กรณี:

- ✅ Case 1 — สมัครสมาชิกสำเร็จ
- ✅ Case 2 — สมัครด้วย Email ที่มีอยู่แล้ว
- ✅ Case 3 — สมัครด้วย Email รูปแบบไม่ถูกต้อง
- ✅ Case 4 — สมัครด้วยรหัสผ่านสั้นเกินไป
- ✅ Case 5 — เข้าสู่ระบบสำเร็จ
- ✅ Case 6 — เข้าสู่ระบบด้วยรหัสผ่านผิด
- ✅ Case 7 — เข้าสู่ระบบด้วย Email ที่ไม่มีในระบบ
- ✅ Case 8 — เข้าถึง Protected Route โดยไม่มี Token
- ✅ Case 9 — เข้าถึง Protected Route ด้วย Token ไม่ถูกต้อง
- ✅ Case 10 — Admin ดูรายชื่อผู้ใช้ทั้งหมดได้
- ✅ Case 11 — User ทั่วไปดูรายชื่อผู้ใช้ทั้งหมดไม่ได้ (Forbidden)
- ✅ Case 12 — User ดูโปรไฟล์ตัวเองได้
- ✅ Case 13 — User ดูโปรไฟล์คนอื่นไม่ได้ (Forbidden)
- ✅ Case 14 — Admin ลบผู้ใช้ได้
- ✅ Case 15 — User ทั่วไปลบผู้ใช้คนอื่นไม่ได้ (Forbidden)

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
