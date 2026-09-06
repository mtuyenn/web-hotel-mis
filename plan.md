# Plan phát triển Web Hotel MIS

## 1. Mục tiêu

Xây dựng phiên bản web và chatbot agent/RAG từ hệ thống Hotel MIS hiện tại, nhưng không làm gián đoạn ứng dụng desktop Java Swing.

Từ thời điểm này:

- `C:\hotel-mis` là bản desktop legacy, chỉ giữ để chạy/đối chiếu.
- `C:\web-hotel-mis` là source of truth cho phát triển mới.
- Repository web: `https://github.com/mtuyenn/web-hotel-mis.git`.
- Snapshot desktop được lưu tại `legacy-desktop/` trong project web để migration và đối chiếu.

## 2. Hiện trạng

Repository cũ là ứng dụng Java Swing/JPA/MySQL:

- Giao diện nằm trong `src/main/java/com/hotelmanagement/view`.
- Nghiệp vụ nằm trong `service`, `service/impl`, `model` và DAO.
- Database hiện có các nhóm phòng, khách hàng, đặt phòng, dịch vụ, hóa đơn, bảo trì và chuyển phòng.
- Chưa có REST API, frontend web, audit log đầy đủ, workflow phê duyệt, housekeeping, minibar/kho, phân ca, giao ca và công nợ.

Các thành phần có thể tái sử dụng sau refactor:

- Entity, DTO, enum, mapper và một phần domain service.
- Schema legacy trong `database/legacy` làm baseline tham chiếu.
- Các prototype agent chỉ dùng để tham khảo, không cho agent production truy cập DB trực tiếp.

## 3. Kiến trúc đích

```text
React/TypeScript Web Frontend
            |
            v
Spring Boot REST API
  |         |          |
 Auth/RBAC  Use Cases  Audit/Approval
            |
            v
       MySQL Database

Chatbot UI -> Agent Orchestrator -> Backend API Tools
                              |
                              +-> RAG: SOP, chính sách, biểu mẫu, hướng dẫn
```

### Backend

Bắt đầu bằng modular monolith Spring Boot:

- `identity`: đăng nhập, vai trò, quyền.
- `room`: phòng, loại phòng, khả dụng, bảo trì.
- `guest`: khách hàng, thành viên, giới hạn đặt phòng.
- `reservation`: đặt phòng, check-in, check-out, chuyển phòng, hủy.
- `billing`: giá phòng, dịch vụ, cọc, hóa đơn, thanh toán.
- `operations`: vệ sinh, minibar, kho, thiết bị, bảo trì.
- `finance`: thu/chi, giao ca, quỹ tiền mặt, công nợ.
- `governance`: audit log, phê duyệt, báo cáo.

Dùng Flyway cho migration, OpenAPI cho contract và transaction ở cấp application use case. Không dùng `hibernate.hbm2ddl.auto=update` trong production.

### Frontend

React/TypeScript, chỉ gọi backend API. Các màn hình ưu tiên:

1. Đăng nhập và điều hướng theo quyền.
2. Bảng phòng và khả dụng.
3. Tra cứu khách hàng/đặt phòng.
4. Check-in, check-out và xem trước hóa đơn.
5. Quản lý dịch vụ/minibar.
6. Chatbot có citation và xác nhận thao tác.

### Agent/RAG

- RAG chỉ dùng cho tài liệu quy trình, chính sách, biểu mẫu và hướng dẫn.
- Dữ liệu phòng trống, khách hàng, hóa đơn, tồn kho và doanh thu phải lấy qua API tools.
- LLM không được truy cập MySQL trực tiếp hoặc tự sinh SQL production.
- Tool ghi dữ liệu phải có schema, RBAC, idempotency, xác nhận người dùng và audit.
- Xóa hóa đơn, hoàn cọc và đổi giá bắt buộc qua approval workflow.

## 4. Lộ trình triển khai

### Phase 0 — Baseline và chốt nghiệp vụ

- Viết characterization tests cho đặt phòng, check-in/out và tính tiền.
- Xác nhận các quy tắc còn mâu thuẫn trong tài liệu.
- Baseline schema và chuẩn bị backup/restore.
- Không thay đổi behavior của desktop.

### Phase 1 — Backend foundation

- Tách application service khỏi Swing controller.
- Dùng dependency injection thay cho `new DAO`.
- Đưa transaction về cấp use case.
- Di chuyển credential sang environment/secret manager.
- Hash mật khẩu.
- Tạo audit abstraction, RBAC và error contract.

### Phase 2 — Web read-only

Triển khai API và giao diện đọc cho phòng, khả dụng, khách hàng, đặt phòng, dịch vụ, hóa đơn và báo cáo hiện có. Desktop vẫn là client ghi dữ liệu chính.

### Phase 3 — Chuyển đổi từng luồng ghi

Thứ tự đề xuất:

1. Khách hàng.
2. Phòng và trạng thái phòng.
3. Đặt phòng/chống overbooking.
4. Check-in/check-out.
5. Dịch vụ/minibar.
6. Hóa đơn/thanh toán.

Mỗi luồng phải có transaction nguyên tử, idempotency, bảo vệ cạnh tranh, contract test, audit và feature flag rollback.

### Phase 4 — Bổ sung nghiệp vụ còn thiếu

- Housekeeping và checklist vệ sinh.
- Thiết bị và tiền bồi thường.
- Minibar/kho và xuất-nhập-tồn.
- Nhân sự, phân ca và giao ca.
- Phiếu thu/chi và quỹ tiền mặt.
- Công nợ OTA/nhà cung cấp.
- Approval workflow và audit log chi tiết.
- Báo cáo tài chính/vận hành.

### Phase 5 — Chatbot agent/RAG

1. Hỏi đáp SOP/chính sách có trích nguồn.
2. Tra cứu live data qua API read-only tools.
3. Đề xuất thao tác.
4. Người dùng xác nhận.
5. Backend kiểm tra quyền và thực thi.
6. Ghi audit toàn bộ tool call và kết quả.

### Phase 6 — Cutover

- Web/API trở thành client chính.
- Desktop chuyển dần sang gọi API.
- Không còn client production dùng credential DB trực tiếp.
- Có backup/restore, monitoring và rollback được kiểm thử.
- Chỉ loại bỏ code desktop sau khi đạt đủ acceptance criteria.

## 5. Acceptance criteria

- API có contract test cho các use case chính.
- Web và desktop cho kết quả tính tiền tương đương trên regression dataset.
- Không xảy ra overbooking khi có request đồng thời.
- Mọi thao tác nhạy cảm có RBAC, approval và audit.
- Agent không truy cập DB trực tiếp.
- Câu trả lời RAG có nguồn và phiên bản tài liệu.
- Secrets không xuất hiện trong Git.
- Có backup/restore và rollback theo từng phase.

## 6. Quy tắc cần xác nhận trước khi triển khai nghiệp vụ

- Thời gian thuê giờ tối thiểu: đề xuất 3 giờ theo tài liệu, trong code cũ đang có nơi dùng 2 giờ.
- Checkout trễ: tài liệu vừa ghi miễn ≤20 phút vừa có mốc 12:01; đề xuất cấu hình thành policy, mặc định miễn đến 12:20.
- Hủy sát giờ: xác định ngưỡng `>3` hay `>=3`; đề xuất cấu hình thay vì hard-code.
- Ngưỡng nâng hạng VIP và tỷ lệ chiết khấu.
- Ma trận vai trò/phê duyệt chính thức.
- Phạm vi OTA, thanh toán online và multi-hotel.

## 7. Milestone kế tiếp

Triển khai Phase 0 và Phase 1 trong `C:\web-hotel-mis`: tạo domain glossary, migration baseline, application boundary, cấu hình môi trường và test cho các quy tắc đặt phòng/tính tiền trước khi xây frontend và chatbot.
