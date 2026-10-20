# Dữ liệu demo local

Các script được chạy thủ công. Ứng dụng không tự xóa hoặc nạp dữ liệu lúc
khởi động. Schema do đúng sáu migration Flyway quản lý theo
[AGENTS.md](../../AGENTS.md); các tệp SQL trong thư mục này chỉ chứa dữ liệu.

## Làm mới toàn bộ dữ liệu vận hành

Chạy từ thư mục gốc khi chủ repo yêu cầu thay dữ liệu QLKS demo local:

```powershell
.\database\demo\refresh_report_demo.ps1 -ResetExistingDemo
# Hoặc chỉ định ngày báo cáo, từ hôm nay đến bảy ngày tới:
.\database\demo\refresh_report_demo.ps1 -ResetExistingDemo -ReportDate '2026-10-21'
```

Ngày mặc định là ngày mai theo giờ Việt Nam. Script chỉ nhắm container
`web-hotel-mis-sqlserver-1433` và database `QLKS`. Nó kiểm tra Flyway V1–V6
và tài khoản demo, sao lưu/xác minh backup, chép backup ra máy trước khi thay
dữ liệu. Backup và log nằm ngoài repo tại
`%USERPROFILE%\.codex\backups\web-hotel-mis`.

Reset, nạp dữ liệu vận hành và kiểm tra nằm trong một transaction. Khóa bảng
được giữ đến COMMIT/ROLLBACK để chặn thay đổi đồng thời; không đổi database
sang SINGLE_USER. Lỗi thì rollback. Script không đổi schema/Flyway history
và giữ hash mật khẩu của tài khoản demo còn tồn tại sau reset.

Sau khi nạp, đăng xuất/đăng nhập lại. Các thao tác demo cũ bị thay thế.
Xem [kịch bản báo cáo 21/10/2026](report_demo_2026-10-21.md) để chọn booking,
phòng và tài khoản cho từng bộ phận. Ngày giờ nghiệp vụ tính theo Việt Nam;
yêu cầu cọc/dịch vụ chờ thanh toán vẫn hết hạn sau 10 phút. Nếu cần những
yêu cầu mới sát giờ trình diễn, chạy lại với ngày báo cáo là chính ngày đó.
Dữ liệu ngân hàng `OPS-*` là chứng từ mô phỏng, không gọi ngân hàng.

## Khởi tạo database demo mới

Chỉ tạo database khi đã được chủ repo cho phép. Cấu trúc và cách chạy backend
nằm trong [hướng dẫn migration](../../backend/src/main/resources/db/migration/README.md)
và [backend](../../backend/README.md). Sau khi Flyway đã áp dụng đủ V1–V6
trên database trống, chạy:

```powershell
.\database\demo\ensure_demo_data.ps1
```

Lệnh này chỉ nạp fixture nền nếu các bảng nghiệp vụ chưa có dữ liệu; nó không
thay một demo đã dùng. Tài khoản nhân viên nền đã có trong fixture; việc cấp
tài khoản nghiệp vụ thực tế phải theo luồng tiếp nhận/phân quyền ở
[rule.md](../../rule.md).

## Bổ sung vào demo nền

Nếu muốn giữ hồ sơ hiện có và chưa nạp bộ vận hành:

```powershell
.\database\demo\populate_operating_demo.ps1 -PopulateExistingDemo
```

Script backup trước khi ghi, thêm dữ liệu trong transaction và chỉ nạp một
lần theo marker `OPERATING-DEMO-V1-COMPLETE`. Nó không làm mới ngày khi chạy
lại; dùng lệnh refresh ở trên khi dữ liệu đã cũ.

## Các tệp SQL được giữ

| Tệp | Nơi dùng và trách nhiệm |
|---|---|
| `reset_demo.sql` | Fixture nền; refresh, bootstrap và thiết lập E2E dùng một lần |
| `populate_operating_demo.sql` | Khách/nhân viên, lịch sử và tình huống vận hành; refresh và lệnh bổ sung dùng chung |
| `verify_operating_demo.sql` | Đối chiếu hóa đơn, payment/receipt/ledger, lịch phòng, VIP, kho, bàn giao và phân ca sau nạp |
| `verify_report_demo.sql` | Kiểm tra khách đến/trả ngày báo cáo, nhiệm vụ/checklist, ca và kho buồng phòng |
| `seed_live_e2e.sql` | Fixture riêng cho [live HTTP E2E](../../frontend/e2e/README.md), chỉ chạy trên database `e2e_*` |

Hai verifier kiểm tra fixture ngay sau nạp và trước khi người dùng thử thao tác.
Chúng không thay thế các test nghiệp vụ của backend.
