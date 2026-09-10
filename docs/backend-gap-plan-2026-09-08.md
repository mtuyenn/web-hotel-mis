# Backend gap plan — trạng thái sau rà soát toàn bộ

> Cập nhật 10/09/2026: xem [đối chiếu rule mới](rule-audit-2026-09-10.md). Quyền xử lý liên ca đã chốt và được sửa; không dùng các số test cũ dưới đây làm nghiệm thu rule mới.

Ngày rà soát: **09/09/2026**. Phạm vi: mã Java backend, migration Flyway,
REST controller/service/DAO, authorization matrix, test suite và database
MySQL `QLKS`.

## Kết luận

Backend đã có nền tảng vận hành thật: JWT/session, RBAC theo bộ phận, guest,
room, reservation, payment ledger, invoice, receipt, approval, maintenance,
inventory, finance cơ bản và customer authentication.

Toàn bộ test hiện tại đã chạy đạt trên môi trường có MySQL:

```text
749 tests, 0 failures, 0 errors, 0 skipped
```

Trong đó có migration validation, billing workflow và security smoke chạy trực
tiếp trên MySQL `QLKS`. Kết quả này chứng minh các contract hiện có chạy được;
nó chưa chứng minh mọi use case nghiệp vụ đã đủ để bàn giao production. Các
đường còn thiếu dưới đây không được đánh dấu hoàn thành chỉ vì đã có entity,
controller hoặc test HTTP.

## Đã hoàn thành và có bằng chứng

### Nền tảng và bảo mật

- JWT access/refresh, refresh rotation, session-family validation, logout,
  account disabled/locked và failed-login audit.
- Capability matrix tập trung cho ADMIN, DIRECTOR, MANAGER, FRONT_DESK,
  ACCOUNTING, HOUSEKEEPING, TECHNICAL, KITCHEN và STAFF.
- CUSTOMER là principal riêng, không nhận quyền nhân viên.
- Actor lấy từ security context; request/header không thể giả mạo actor.
- Audit cho các mutation chính và các kết quả 401/403.
- Ma trận HTTP bao phủ 51 endpoint với 12 loại principal; 613 ca pass.

Bằng chứng chính: `SecurityIntegrationTest`,
`CustomerAuthenticationIntegrationTest`, `DepartmentAuthorizationMatrixTest`
và `MySqlSecuritySmokeTest`.

### Guest, room và reservation

- Guest search/detail/create và membership-history read.
- Room search, availability, status và room equipment read/add.
- Reservation create/get/list, giới hạn tối đa 3 phòng trong request, kiểm tra
  overlap, khóa phòng theo thứ tự, idempotency gắn actor + payload.
- Check-in, check-out, cancel, extend, add service và equipment incident.
- Cọc được kiểm tra theo 50% giá phòng dự kiến.
- Cancel trước 48 giờ hoàn cọc; cancel trong 48 giờ giữ cọc và tăng bộ đếm.
- Room transfer kiểm tra trạng thái, thời điểm và overlap phòng đích.
- Maintenance có transition `CHUA_XU_LY → DANG_BAO_TRI → DA_HOAN_THANH` và
  khóa/release trạng thái phòng.

### Billing và governance

- Payment transaction có ledger, idempotency, chống thu/hoàn vượt số dư và
  approval exact payload/amount cho refund cần phê duyệt.
- Invoice reconcile theo payment ledger; deposit/cancel/refund có transaction.
- Receipt chỉ được lập trong phần tiền đã thu nhưng chưa lập biên lai.
- Approval có requester/approver khác nhau, expiry và consume-once.
- Audit query cơ bản và finance list API đã có.

### Persistence và kiểm thử

- Flyway V1 và Hibernate mapping validate trên MySQL `QLKS`.
- MySQL billing workflow: 4/4 pass.
- MySQL migration validation: 2/2 pass.
- MySQL security smoke: 3/3 pass.
- Full Maven suite: 749/749 pass.

## Còn thiếu thật sự

### P0 — phải hoàn thành trước khi gọi backend production-ready

| ID | Khoảng trống | Hiện trạng | Bằng chứng nghiệm thu cần có |
|---|---|---|---|
| P0-1 | Concurrency booking và idempotency | Đã có MySQL/InnoDB test hai transaction thanh toán đồng thời cùng key và hai transaction đặt cùng phòng; xác nhận một ledger row, một booking và một `OVERBOOKING`. Create dùng `READ_COMMITTED` cùng khóa phòng để tránh gap-lock deadlock và stale snapshot. Vẫn thiếu concurrency proof cho check-in, extend, transfer và maintenance. | MySQL test chạy ít nhất hai transaction độc lập cho create, check-in, extend, transfer, maintenance và retry cùng key; không double-booking, không duplicate ledger, không audit một phần. |
| P0-2 | Invoice adjustment | **Đã hoàn thành:** `/adjust` ghi ledger append-only, khóa invoice, bắt buộc approval, audit và idempotency key; invoice reconcile có `adjustment_total`. | Test số dư, approval và append-only đã đạt; cần giữ trong regression suite. |
| P0-3 | Đối soát tài chính | Giao ca đã bỏ `expected_amount` khỏi request và tự tính CASH payment trừ refund theo actor/từ lần giao ca trước. Settlement riêng cho CARD/BANK_TRANSFER và tất toán partner debt vẫn thiếu. | Bổ sung settlement/reconciliation cho các phương thức không tiền mặt và test ranh giới ca đồng thời. |
| P0-4 | Snapshot giá và tính tiền nhiều phòng | **Đã hoàn thành:** service usage lưu `unit_price`; checkout tính room/extension/late theo từng phòng và giữ `original_check_out`. | Test số tiền cụ thể nhiều phòng, gia hạn, late surcharge, snapshot và rounding đã đạt. |
| P0-5 | Ranh giới thao tác reservation | Mutation hiện kiểm tra nhân viên tạo reservation; chưa có policy rõ cho người ca sau, manager, accounting và customer thao tác reservation của người khác. | Chủ sở hữu chốt scope; sau đó đưa policy vào service và test cross-shift/cross-department, không chỉ dựa vào controller role. |

### P1 — cần hoàn thiện để đủ API vận hành

| ID | Khoảng trống | Hiện trạng |
|---|---|---|
| P1-1 | Reservation lifecycle đầy đủ | Chưa có command update/confirm riêng; đã có flow `NO_SHOW` sau giờ trả dự kiến, giữ cọc và giải phóng phòng; list mới là search cơ bản, chưa đủ filter/sort theo nhu cầu vận hành. |
| P1-2 | Guest và identity administration | Guest mới có create/read; chưa có update nhạy cảm qua approval, PII masking policy và API quản lý employee/account (list, disable, role change). |
| P1-3 | Room administration | Có room read/status/equipment nhưng chưa có API quản trị room type, room, giá và vòng đời thiết bị đầy đủ. |
| P1-4 | Customer portal | Có register/login/refresh/logout/password reset và `/customers/me`; chưa có customer cập nhật profile, xem/tạo/hủy reservation hoặc ownership flow cho các command. |
| P1-5 | Inventory và equipment snapshot | Add service đã ghi ISSUE movement và snapshot giá ở line item; compensation vẫn nhận giá/ngày mua từ request thay vì luôn đối chiếu equipment đã quản lý. |
| P1-6 | Receipts và billing query | Receipt không có idempotency key riêng; invoice/payment/receipt query chưa có pagination/filter và chưa có workflow điều chỉnh finalized invoice. |
| P1-7 | Approval/audit vận hành | Approval queue có list/reject cơ bản; chưa có query theo target/action đầy đủ, audit query chưa có pagination/filter/correlation search. |
| P1-8 | Finance và reporting | Chưa có settlement partner debt, đối soát ca theo tender, báo cáo doanh thu thực thu, công nợ và phòng đến/đi. |
| P1-9 | Contract/API consistency | Cần đối chiếu lại `docs/api-contract.md`, `backend/README.md`, DTO và annotation sau mỗi endpoint mới; không giữ mô tả cũ như hourly/cancellation rule đã thay đổi. |
| P1-10 | Production operations | Chưa có bootstrap tài khoản quản trị, demo data riêng, backup/restore runbook, readiness/dependency checks và correlation ID xuyên request đầy đủ. |

## Quyết định nghiệp vụ còn chờ chủ sở hữu

Không tự triển khai các nhánh sau khi chưa có quyết định cuối:

1. Cách tính lượt VIP cho lưu trú một phần hoặc loại lưu trú khác.
2. Khoản phí hủy bổ sung ngoài tiền cọc.
3. Nhân viên ca sau/manager/accounting được thao tác reservation ngoài người tạo
   ở mức nào.
5. Customer portal có thuộc phạm vi release đầu hay chỉ là customer account.

## Thứ tự thực hiện tiếp theo

### Đợt A — khóa invariant và bằng chứng MySQL

1. Chốt năm quyết định nghiệp vụ ở trên.
2. Viết concurrency test MySQL cho booking, retry idempotency, transfer,
   maintenance và payment/refund.
3. Hoàn thành hoặc loại bỏ invoice adjustment endpoint.
4. Sửa snapshot giá, nhiều phòng, late checkout và finance reconciliation.

### Đợt B — hoàn thiện API vận hành

1. Reservation update/confirm/search đầy đủ theo state machine.
2. Guest update, employee/account administration và room/type administration.
3. Receipt/billing/finance/approval/audit query có pagination/filter.
4. Inventory/equipment snapshot và settlement workflow.

### Đợt C — customer và production handoff

1. Nếu customer portal nằm trong release: hoàn thiện ownership cho profile,
   reservation và cancellation.
2. Đồng bộ API contract, error contract, permission matrix và README.
3. Thêm readiness, correlation ID, bootstrap admin, demo data và backup/restore.
4. Chạy lại clean checkout với full Maven suite và MySQL acceptance; ghi kết quả
   vào release checklist.

## Definition of done cho backend

Backend chỉ được đánh dấu hoàn thành khi:

- Tất cả P0 có invariant và test MySQL độc lập, không chỉ test HTTP 200.
- Các endpoint bắt buộc của release có DTO, authorization, transaction, audit và
  query scope tương ứng.
- Không còn endpoint mutation chủ động trả `UNSUPPORTED` trong contract.
- Rule, migration, code, API docs và test dùng cùng một contract.
- Full suite chạy từ checkout sạch với MySQL acceptance và không có test bị bỏ
  qua do thiếu biến môi trường.
