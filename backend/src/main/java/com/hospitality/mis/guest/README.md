# Guest module

Hồ sơ khách, membership, lịch sử lưu trú và chính sách khóa quyền đặt trước.

`Guest` là concrete JPA owner duy nhất của bảng `guests` theo schema V1.
`GuestStore` là application port và `JpaGuestStore` là adapter persistence duy
nhất.

Guest API giữ authorization hook cho các thao tác ghi và audit actor cho các
thay đổi dữ liệu nhạy cảm.

Membership mặc định là `STANDARD`; `MembershipPolicy` hỗ trợ ngưỡng chi tiêu
VIP 10.000.000 và 10 lượt lưu trú. `BookingPolicy` mặc định khóa quyền đặt
trước từ lần hủy sát giờ thứ tư. Các use case reservation hiện vẫn sở hữu
transaction hủy/booking nên policy canonical chưa thay đổi behavior reservation.
