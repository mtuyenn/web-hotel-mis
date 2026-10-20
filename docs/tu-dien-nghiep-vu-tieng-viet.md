# Từ điển nghiệp vụ

Tài liệu này giải thích thuật ngữ. Quy tắc nội bộ thuộc
[rule.md](../rule.md), điều khoản/giá thuộc [customer-policy.md](../customer-policy.md),
endpoint/payload thuộc [API contract](api-contract.md).
Ánh xạ đầy đủ mã Java/API sang giá trị vật lý nằm trong
[từ điển giá trị database](doi-chieu-gia-tri-database.csv), được kiểm tra bằng
converter thật và metadata SQL Server.

## Khái niệm trong sản phẩm

| Thuật ngữ | Nghĩa |
|---|---|
| Guest / Khách lưu trú | Hồ sơ người đặt hoặc người ở; bảng `KhachLuuTru` |
| Customer account | Tài khoản khách dùng portal; bảng `TaiKhoanKhachHang` |
| Employee | Nhân viên; bảng `NhanVien` |
| Room type / Hạng phòng | Nhóm cấu hình giá, sức chứa và tiện nghi; bảng `LoaiPhong` |
| Room | Phòng cụ thể; bảng `Phong` |
| Reservation / Booking | Phiếu đặt phòng; bảng `PhieuDatPhong` |
| Room stay | Khoảng thuê của từng phòng trong booking; bảng `ChiTietDatPhong` |
| Booking source | Nguồn đặt trực tiếp, điện thoại, walk-in hoặc OTA |
| Rental type | Hình thức thuê theo gói hoặc theo giờ |
| Deposit | Tiền cọc; yêu cầu thanh toán khác với khoản thực thu |
| Invoice / Folio | Hóa đơn tổng hợp phòng, dịch vụ và điều chỉnh; bảng `HoaDon` |
| Payment transaction | Dòng thu tiền hoặc hoàn tiền; bảng `GiaoDichThanhToan` |
| Receipt | Biên lai của tiền thực thu; bảng `BienLai` |
| VAT invoice | Chứng từ thuế riêng, hỗ trợ thông tin doanh nghiệp và XML |
| Hotel service booking | Lượt đặt dịch vụ gắn với booking/phòng; bảng `DatDichVuKhachSan` |
| Minibar | Hàng tiêu dùng trong phòng, được kiểm kê và ghi nhận sử dụng |
| Supplier debt | Công nợ mua hàng/vật tư với nhà cung cấp |
| OTA | Kênh bán phòng ngoài như Booking.com hoặc Agoda |
| Reconciliation | Đối chiếu doanh thu, hoa hồng hoặc giao dịch giữa các sổ/kênh |
| Cash handover | Bàn giao tiền giữa lễ tân và người nhận được chỉ định |
| Housekeeping | Buồng phòng: nhiệm vụ vệ sinh, checklist, kiểm minibar và thiết bị |
| Technical work order | Phiếu công việc sửa chữa, nghiệm thu và bàn giao kỹ thuật |
| Technical asset | Tài sản phòng hoặc tòa nhà có vị trí và lịch bảo trì |
| Department stock | Tồn riêng của buồng phòng; khác số tồn kho tổng |
| Shift | Ca làm việc; khác bản ghi chấm công thực tế |
| Leave request | Đơn nghỉ phép, nghỉ ốm hoặc đổi ca |
| Onboarding request | Đề nghị tiếp nhận nhân viên trước khi tạo tài khoản |
| Membership tier | Hạng thành viên; khác vai trò đăng nhập và trạng thái booking |

## Khái niệm kỹ thuật

| Thuật ngữ | Nghĩa |
|---|---|
| Frontend | Giao diện khách hàng và các màn hình nhân viên |
| Backend | API xử lý quyền, nghiệp vụ và giao dịch dữ liệu |
| DTO | Kiểu dữ liệu tại biên request/response |
| Controller | Tiếp nhận HTTP và chuyển cho service |
| Service | Điều phối ca sử dụng, kiểm tra quyền và nghiệp vụ |
| Database adapter / DAO | Lớp JDBC gọi view, function hoặc stored procedure |
| Entity | Mapping JPA để Hibernate kiểm tra schema |
| Database / Schema | QLKS là tên database local; các bảng nghiệp vụ thuộc schema `dbo` |
| Flyway migration | Tệp quản lý object SQL; cách tổ chức thuộc AGENTS.md |
| Transaction | Các thay đổi thành công hoặc rollback cùng nhau |
| Validation | Kiểm tra dữ liệu và điều kiện hợp lệ |
| Actor | Principal đã xác thực hoặc tác nhân hệ thống thực hiện thao tác |
| Ownership | Phạm vi sở hữu, ví dụ khách chỉ xem booking của mình |
| JWT / Access token | Token xác thực để backend nhận biết principal |
| Role | Vai trò nhân viên; CUSTOMER là loại tài khoản khách riêng |
| Permission / Capability | Quyền chi tiết của endpoint hoặc ca sử dụng |
| Audit | Sự kiện ghi người thực hiện, đối tượng, thời điểm và lý do |
| Approval | Quyết định phê duyệt gắn với hành động, dữ liệu và người duyệt |
| Idempotency | Cùng yêu cầu được gửi lại trả cùng kết quả, tránh ghi trùng |
| Notification outbox | Hàng đợi thông báo được lưu cùng giao dịch nghiệp vụ |
| Pagination | Phân trang kết quả đọc |

## Cách đọc trạng thái và trường dữ liệu

Mã có nghĩa theo miền: `PENDING` của tiền cọc khác `PENDING` của phê duyệt
hoặc thông báo. Trạng thái phòng hiện tại khác trạng thái chi tiết đặt phòng;
khả dụng tương lai còn phụ thuộc lịch lưu trú. Các nhóm “khách đến”,
“khách trả”, “đang ở”, “chưa thu cọc” trên dashboard là kết quả truy vấn,
không phải trạng thái mới để lưu vào booking.

`expected_*` là thời điểm dự kiến, `actual_*` là thời điểm thực tế;
`amount` là số tiền, `quantity` là số lượng; `gross_revenue` là doanh thu
gộp, `net_revenue` là doanh thu ròng, `commission` là hoa hồng và
`variance` là chênh lệch. Tên trường chính xác theo từng endpoint ở API contract.

Khi sửa nhãn tiếng Việt, giữ mã Java/API và giá trị database theo hợp đồng
đang dùng. Khi thay đổi nghiệp vụ, sửa chủ sở hữu tương ứng ở đầu tài liệu,
code và test liên quan; không duy trì một bản chính sách thứ hai trong từ điển.
