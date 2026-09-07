# Hotel MIS backend

Spring Boot 3.4 modular monolith (Java 21) for the Hotel MIS web system. The
canonical Java package is `com.hospitality.mis`; modules expose API,
application, domain and adapter boundaries.

## Chạy local

1. Khởi động MySQL local:
   `docker compose up -d mysql`.
2. Cấu hình `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`; không commit credential.
3. Chạy `mvn spring-boot:run` trong thư mục `backend`.

Flyway quản lý schema theo thứ tự `V1` đến phiên bản hiện tại. V1 tại
`src/main/resources/db/migration/V1__baseline_schema.sql` là canonical schema
V1; các migration sau mở rộng cùng một schema contract. Hibernate chỉ validate
mapping bằng `ddl-auto=validate`, không tự tạo hoặc sửa bảng.

## API lõi

- `GET /api/rooms`, `GET /api/rooms/availability?from=&to=&type=`
- `POST /api/guests`, `GET /api/guests?q=`, `GET /api/guests/{id}`
- `POST /api/reservations`, `GET /api/reservations/{id}`
- `POST /api/reservations/{id}/check-in`
- `POST /api/reservations/{id}/check-out`
- `POST /api/reservations/{id}/extend`, `/cancel`, `/services`
- `GET /api/invoices/reservation/{reservationId}`
- `GET /api/services`, `POST /api/services`, `POST /api/services/{id}/stock`
- `POST /api/governance/approvals`, `POST /api/governance/approvals/{id}/approve`

Các use case ghi dữ liệu đều có transaction. Tạo đặt phòng khóa từng phòng
bằng pessimistic lock, kiểm tra khoảng giao nhau
`existingIn < requestedOut && existingOut > requestedIn`, hỗ trợ idempotency
qua `idempotencyKey`, giới hạn 3 phòng và thuê giờ tối thiểu 3 giờ.

## Policy mặc định

`HOTEL_HOURLY_MINIMUM=3`, miễn checkout trễ tối đa 20 phút, VIP giảm 10%, phí
trễ theo các mốc 15/20/50/100%. Có thể override bằng biến môi trường; các
ngưỡng chưa chốt được giữ ngoài code nghiệp vụ.

## Authentication, JWT và RBAC

`POST /api/auth/login` và `POST /api/auth/refresh` là các endpoint công khai để
cấp hoặc làm mới cặp access/refresh token. Access token là JWT Bearer; các API
còn lại yêu cầu `Authorization: Bearer <token>`.

JWT dùng mã nhân viên trong `sub`. Spring Security tạo authenticated principal
từ JWT và nạp lại quyền cùng trạng thái tài khoản từ identity store. RBAC dùng
các role như `MANAGER`, `FRONT_DESK`, `ACCOUNTING` và `HOUSEKEEPING`; việc cấp
nhân viên hoặc đặt lại mật khẩu yêu cầu `MANAGER`.

Actor audit lấy từ authenticated principal trong security context.
`X-Actor-Id`, nếu client gửi, chỉ là header do client cung cấp và không được
tin cậy hoặc dùng làm identity/audit actor.

## Database ownership

Backend là DB writer duy nhất. Mọi ghi dữ liệu nghiệp vụ phải đi qua
application service, transaction boundary, authorization và audit policy; các
client không được dùng credential MySQL để ghi trực tiếp.
