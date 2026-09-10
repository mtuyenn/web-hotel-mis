# API contract V1

Đây là contract HTTP chuẩn của backend. `rule.md` là nguồn nghiệp vụ; tài liệu
này là nguồn tra cứu endpoint/DTO cho frontend, agent và test.

## Quy ước chung

- Base URL: `/api`.
- JSON request/response dùng `lower_snake_case` (`employee_id`,
  `check_in_at`, `payment_method`). Không tạo alias camelCase.
- Thời gian dùng `LocalDateTime` theo múi giờ nghiệp vụ `Asia/Ho_Chi_Minh`.
- Tiền tệ là VND. Số tiền dùng JSON number; chỉ làm tròn tổng cuối hóa đơn
  theo quy tắc trong `rule.md`.
- Endpoint ghi dữ liệu phải đi qua controller → service → DAO và chịu kiểm tra
  actor/RBAC tại middleware hoặc controller.

## Response lỗi

Lỗi nghiệp vụ/validation từ backend có dạng:

```json
{
  "timestamp": "2026-09-08T10:00:00Z",
  "status": 422,
  "code": "BUSINESS_ERROR_CODE",
  "message": "Mô tả lỗi cho người dùng",
  "details": []
}
```

Các mã nền tảng hiện có: `VALIDATION_ERROR` (400), `DATA_CONFLICT` (409),
`ACCESS_DENIED` (403). Lỗi xác thực JWT dùng response riêng của middleware;
client phải xử lý cả 401 và 403.

## Endpoint và quyền

| Method | Path | Quyền hiện tại |
|---|---|---|
| POST | `/auth/login` | Public |
| POST | `/auth/refresh` | Public |
| POST | `/auth/logout` | Đã xác thực |
| POST | `/auth/employees` | MANAGER |
| POST | `/auth/employees/{employee_id}/password` | MANAGER |
| GET | `/guests`, `/guests/{id}` | ADMIN, DIRECTOR, MANAGER, FRONT_DESK |
| POST | `/guests` | MANAGER, FRONT_DESK |
| GET | `/rooms`, `/rooms/availability` | ADMIN, DIRECTOR, MANAGER, FRONT_DESK, HOUSEKEEPING, TECHNICAL, STAFF |
| PATCH | `/rooms/{id}/status` | ADMIN, DIRECTOR, MANAGER, FRONT_DESK, HOUSEKEEPING, TECHNICAL |
| GET/POST | `/rooms/{room_id}/equipment` | GET: MANAGER, HOUSEKEEPING, TECHNICAL, FRONT_DESK; POST: MANAGER, TECHNICAL |
| POST | `/reservations` | MANAGER, FRONT_DESK |
| GET | `/reservations?status=&guest_id=&page=&size=` | Có `RESERVATION_READ`; front_desk/manager/director/admin xem toàn khách sạn, role khác xem phạm vi actor; mặc định 20, tối đa 100 bản ghi/trang; trả `items`, `page`, `size`, `total_elements`, `total_pages` |
| GET | `/reservations/{id}` | ADMIN, DIRECTOR, MANAGER, ACCOUNTING, FRONT_DESK, HOUSEKEEPING, TECHNICAL, STAFF |
| POST | `/reservations/{id}/check-in` | MANAGER, FRONT_DESK |
| POST | `/reservations/{id}/check-out` | MANAGER, FRONT_DESK |
| POST | `/reservations/{id}/cancel` | MANAGER, FRONT_DESK |
| POST | `/reservations/{id}/no-show` | MANAGER, FRONT_DESK; chỉ sau khi hết giờ trả dự kiến, giữ tiền cọc |
| POST | `/reservations/{id}/extend` | MANAGER, FRONT_DESK |
| POST | `/reservations/{id}/services` | MANAGER, FRONT_DESK |
| POST | `/reservations/{id}/equipment-incidents` | MANAGER, FRONT_DESK, HOUSEKEEPING |
| GET | `/invoices/reservation/{reservation_id}` | MANAGER, ACCOUNTING, FRONT_DESK |
| POST | `/invoices/reservation/{reservation_id}/deposit/refund` | MANAGER, ACCOUNTING, FRONT_DESK; mọi lần hoàn cọc cần approval do DIRECTOR duyệt |
| POST | `/invoices/{invoice_id}/adjust` | MANAGER, ACCOUNTING; bắt buộc `Idempotency-Key` và approval exact payload |
| GET | `/services` | ADMIN, DIRECTOR, MANAGER, ACCOUNTING, FRONT_DESK, HOUSEKEEPING, KITCHEN |
| POST | `/services` | ADMIN, DIRECTOR, MANAGER, ACCOUNTING, KITCHEN |
| POST | `/services/{id}/stock` | ADMIN, DIRECTOR, MANAGER, ACCOUNTING, FRONT_DESK, HOUSEKEEPING, KITCHEN |
| GET | `/operations/maintenance/room/{room_id}` | ADMIN, DIRECTOR, MANAGER, FRONT_DESK, HOUSEKEEPING, TECHNICAL |
| POST | `/operations/maintenance` | MANAGER, HOUSEKEEPING |
| PATCH | `/operations/maintenance/{id}/status` | MANAGER, HOUSEKEEPING |
| POST | `/operations/reservations/{reservation_id}/room-transfers` | MANAGER, FRONT_DESK |
| POST | `/operations/reservations/{reservation_id}/equipment-incidents` | MANAGER, FRONT_DESK, HOUSEKEEPING |
| POST | `/governance/approvals` | MANAGER, ACCOUNTING, FRONT_DESK |
| POST | `/governance/approvals/{id}/approve` | Có `APPROVAL_APPROVE`, khác requester; action refund bắt buộc role DIRECTOR |
| GET | `/governance/approvals?status=PENDING` | Có `APPROVAL_APPROVE` |
| POST | `/governance/approvals/{id}/reject` | Có `APPROVAL_APPROVE`, khác requester; action refund bắt buộc role DIRECTOR |
| GET/POST | `/invoices/{invoice_id}/payments` | MANAGER, ACCOUNTING, FRONT_DESK |
| GET/POST | `/invoices/{invoice_id}/receipts` | MANAGER, ACCOUNTING, FRONT_DESK |
| GET/POST | `/services/{service_id}/inventory-movements` | ADMIN, DIRECTOR, MANAGER, ACCOUNTING, FRONT_DESK, HOUSEKEEPING, KITCHEN |
| GET | `/finance/cash-handovers`, `/finance/expenses`, `/finance/partner-debts` | MANAGER, ACCOUNTING, DIRECTOR |
| POST | `/finance/cash-handovers`, `/finance/expenses`, `/finance/partner-debts` | MANAGER, ACCOUNTING, DIRECTOR; actor giao ca lấy từ JWT |
| GET | `/governance/audit` | ADMIN, DIRECTOR, MANAGER, ACCOUNTING; scoped by actor for accounting |
| POST | `/auth/customers/register` | Public |
| GET | `/auth/customers/me` | CUSTOMER; only the authenticated guest |

## DTO chính

- Auth: `login`, `refresh_token`, `logout`, `token_response`, `provision` và
  `employee_response` dùng các field snake_case tương ứng.
- Guest: `full_name`, `birth_year`, `identity_number`, `phone`, `email`,
  `address`, `membership_tier`, `total_spend`, `late_cancellation_count`,
  `late_checkout_count`, `booking_blocked`.
- Reservation create: `guest_id`, `employee_id`, `deposit`, `rental_type`,
  `rooms[]`; mỗi room có `room_id`, `expected_check_in`,
  `expected_check_out`. Các action dùng `at`, `new_expected_check_out`,
  `service_id`, `quantity`, `used_at`.
- Room: `room_id`, `room_type_id`, `room_type_name`, `daily_price`, `floor`,
  `status`, `available`.
- Invoice: `reservation_id`, `room_total`, `service_total`, `late_surcharge`,
  `compensation`, `extension_total`, `adjustment_total`, `discount`, `deposit`, `payable`,
  `payment_method`, `status`.
- Payment transaction: `amount`, `method`, `type`, `reference`, `idempotency_key` (bắt buộc, tối đa 35 ký tự; retry phải giữ nguyên payload và actor).
- Cash handover request: `shift_code`, `from_actor`, `to_actor`, `actual_amount`,
  `note`. Backend tự tính `expected_amount` từ các giao dịch CASH hoàn tất của
  actor kể từ lần giao ca trước; client không gửi trường này.

## Trạng thái chuẩn

- Reservation: `DRAFT → DEPOSIT_PAID/CONFIRMED → CHECKED_IN → CHECKED_OUT`;
  `DRAFT`, `DEPOSIT_PAID`, `CONFIRMED` có thể chuyển `CANCELLED`. Chỉ
  `DEPOSIT_PAID`/`CONFIRMED` chưa check-in mới được chuyển `NO_SHOW`, và chỉ
  sau giờ trả dự kiến; tiền cọc bị giữ.
- Room: `READY`, `OCCUPIED`, `CLEANING`, `MAINTENANCE`, `OUT_OF_SERVICE`,
  `RESERVED`, `RETURNED`, `CANCELLED`. Giá trị JSON hiện giữ mã database như
  `SAN_SANG`, `DANG_O`, `DANG_DON_DEP`.
- Payment: `DA_THANH_TOAN`, `CHUA_THANH_TOAN`, `DU_KIEN`.

## Điểm cần hoàn thiện trong bước nghiệp vụ kế tiếp

Contract đã chốt quy tắc trong `rule.md`. Checkout lập hóa đơn theo số dư còn
phải thu; payment transaction mới làm giảm số dư và chuyển trạng thái đã thanh
toán. Các điểm chưa chốt (VIP partial stay và phí hủy ngoài cọc) vẫn không được
tự suy đoán. `NO_SHOW` chỉ được ghi nhận sau giờ trả dự kiến khi khách chưa
check-in và tiền cọc bị giữ. Các endpoint mới phải được thêm vào bảng contract trước
khi frontend sử dụng.

## Phạm vi tài khoản

`employees` và `customer_accounts` là hai aggregate account riêng, cùng dùng
số điện thoại duy nhất làm định danh đăng nhập. Customer account phải gắn đúng
một guest; quyền `CUSTOMER` chỉ được xem hoặc thay đổi dữ liệu thuộc guest của
mình. Luồng đăng ký khách hàng, login bằng phone cho cả hai loại account và
customer ownership authorization vẫn cần triển khai ở middleware/service trước
khi mở frontend production.

## Cập nhật quyền đặt phòng ngày 10/09/2026

Theo mục 11–12 của `rule.md`, FRONT_DESK và MANAGER được tạo và thay đổi đặt
phòng, nhận/trả phòng, gia hạn, hủy, thêm dịch vụ và chuyển phòng. ADMIN và
DIRECTOR vẫn có quyền đọc nhưng không được tự động thừa hưởng các command này.
ACCOUNTING không được tạo đặt phòng hoặc checkout, kể cả là người tạo đơn.
Service kiểm tra principal và role trước khi xử lý idempotency; không nhận actor
chưa xác thực. `employee_id` lúc tạo phải khớp principal; các thao tác tiếp theo
không thay nhân viên tạo ban đầu và ghi audit bằng actor đang thao tác.

FRONT_DESK xem danh sách/chi tiết đặt phòng và chứng từ hóa đơn, thanh toán,
biên lai của ca khác để tiếp tục vận hành. Quyền này không cấp quyền đọc báo cáo
tài chính. Mọi refund, gồm hoàn cọc khi hủy miễn phí, chỉ được thực thi sau khi
DIRECTOR duyệt đúng payload và số tiền; approval được tiêu thụ một lần.
