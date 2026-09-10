# Hướng dẫn đọc mã nguồn Web Hotel MIS

Tài liệu này giải thích cấu trúc và luồng xử lý chính của dự án bằng tiếng
Việt. Tên class, biến, method, bảng và cột vẫn dùng tiếng Anh để thống nhất;
các nhãn hiển thị cho người dùng mới dùng tiếng Việt.

## 1. Luồng tổng quát

```text
Frontend React/TypeScript
        -> HTTP JSON API
Spring Boot Controller
        -> Service (nghiệp vụ và transaction)
DAO/Repository (Spring Data JPA)
        -> Entity JPA
MySQL (schema do Flyway quản lý)
```

- `frontend/src`: giao diện React, route, form, kiểu dữ liệu API và HTTP client.
- `backend/src/main/java/.../controller`: nhận request, kiểm tra quyền cơ bản,
  gọi service và trả response; controller không truy cập database trực tiếp.
- `backend/src/main/java/.../service`: nơi chứa use case, quy tắc nghiệp vụ,
  transaction, kiểm tra trạng thái và phối hợp nhiều repository.
- `backend/src/main/java/.../dao`: repository và adapter truy cập dữ liệu.
- `backend/src/main/java/.../entity`: mô hình domain được JPA ánh xạ xuống bảng.
- `backend/src/main/java/.../dto`: kiểu request/response ở ranh giới HTTP,
  không để entity database lộ trực tiếp ra API.
- `backend/src/main/java/.../middleware`: JWT, actor hiện tại và authorization.
- `backend/src/main/resources/db/migration`: các migration Flyway, là nguồn
  chính thức để tạo và thay đổi schema.

## 2. Quy tắc đặt tên

- Java/TypeScript: `Guest`, `fullName`, `findByPhone`.
- Database: `guests`, `full_name`, `room_id`.
- JSON API: `full_name`, `scheduled_date`.
- Giao diện: dùng nhãn tiếng Việt, ví dụ “Họ tên”, “Số điện thoại”.
- Không đổi các giá trị enum nghiệp vụ đang được lưu/trao đổi như
  `CHUA_XU_LY`, vì chúng là dữ liệu tương thích với schema và API hiện tại.

## 3. Các module nghiệp vụ

- `identity` và `auth`: nhân viên, tài khoản khách hàng, đăng nhập, JWT và refresh token.
- `guest`: hồ sơ khách, hạng thành viên và lịch sử thay đổi hạng.
- `room`: loại phòng, phòng, trạng thái phòng, thiết bị trong phòng.
- `reservation`: đặt phòng, nhận phòng, trả phòng, gia hạn, hủy và chuyển phòng.
- `billing`: dịch vụ, dòng dịch vụ đã dùng, hóa đơn, thanh toán và biên lai.
- `operations`: bảo trì, sự cố thiết bị, tồn kho và chuyển phòng.
- `finance`: giao ca tiền mặt, chi phí và công nợ đối tác.
- `governance`: audit log, approval và kiểm soát các thao tác nhạy cảm.

## 4. Ví dụ luồng tạo phiếu bảo trì

1. `MaintenanceController` nhận `POST /api/operations/maintenance`.
2. Request được ánh xạ vào `MaintenanceDtos.CreateRequest`.
3. `MaintenanceService.create` kiểm tra mã phiếu và khóa phòng.
4. Nếu phòng hợp lệ, service tạo `MaintenanceWorkOrder` với trạng thái
   `CHUA_XU_LY`, chuyển phòng sang `MAINTENANCE` và lưu trong một transaction.
5. `AuditService` ghi actor, hành động và dữ liệu liên quan.
6. Response được chuyển về JSON theo tên field snake_case.

Trạng thái phiếu chỉ đi theo chiều:

```text
CHUA_XU_LY -> DANG_BAO_TRI -> DA_HOAN_THANH
```

Khi hoàn tất, phòng trở lại `READY`. Việc giữ các mã trạng thái cũ giúp dữ
liệu và API không bị thay đổi chỉ vì refactor tên class/biến.

## 5. Database và migration

`V1__baseline_schema.sql` là schema gốc. Hibernate đang dùng
`ddl-auto: validate`, nghĩa là Hibernate chỉ xác nhận mapping khớp database,
không tự tạo hoặc sửa bảng. Nếu schema thay đổi sau khi đã triển khai, tạo
migration Flyway mới dạng `V2__...sql`; không sửa migration đã chạy trên môi
trường có dữ liệu.

## 6. Khi sửa code

1. Xác định controller và endpoint liên quan.
2. Đọc DTO để biết dữ liệu vào/ra.
3. Đọc service để hiểu quy tắc nghiệp vụ và transaction.
4. Đọc entity/repository để hiểu dữ liệu được lưu thế nào.
5. Cập nhật test, migration hoặc tài liệu nếu contract thay đổi.
6. Chạy test backend và kiểm tra JPA/Flyway validation trước khi commit.
