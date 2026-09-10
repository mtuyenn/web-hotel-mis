# Đối chiếu rule và kiểm thử ngày 10/09/2026

## Kết luận

Dự án **chưa thực hiện đầy đủ `rule.md` mới nhất**. Backend có nhiều use case
hoạt động nhưng còn sai lệch nghiệp vụ quan trọng. Frontend mới là scaffold;
chưa thể nghiệm thu nghiệp vụ sử dụng qua trình duyệt.

Rà soát trên working tree đang có thay đổi, không phải một commit sạch. Quyết
định bổ sung của chủ sở hữu ngày 10/09/2026 đã chốt hành vi `no_show`; cách tính
VIP cho lưu trú một phần và phí hủy ngoài cọc vẫn chưa chốt. Kết quả test cũ
749/749 không đại diện cho rule mới.

## Đã sửa trong đợt này

- FRONT_DESK tìm và xem đặt phòng của ca khác; xử lý nhận/trả phòng, hủy,
  gia hạn, thêm dịch vụ và chuyển phòng không phụ thuộc nhân viên tạo.
- Chỉ FRONT_DESK/MANAGER được dùng các command reservation, kiểm tra ở cả
  endpoint và service. ACCOUNTING không thể tạo reservation hoặc checkout.
  ADMIN/DIRECTOR không tự động nhận các quyền vận hành này.
- Lễ tân ca sau truy cập chứng từ invoice/payment/receipt của đặt phòng.
  Không cấp quyền đọc báo cáo tài chính cho lễ tân.
- ReservationService bắt buộc principal đã xác thực, không còn nhánh tin
  chuỗi actor khi SecurityContext trống. Kiểm tra quyền trước idempotency.
- Giữ nguyên nhân viên tạo; audit ghi nhân viên thực hiện hiện tại.
- Cập nhật các test cũ đang khẳng định lễ tân không được truy cập liên ca.
- Mọi refund và hoàn cọc khi hủy miễn phí cần approval do DIRECTOR duyệt;
  FRONT_DESK không còn ngoại lệ.
- Checkout tính riêng tiền phòng, gia hạn và phụ thu theo từng phòng; lưu mốc
  checkout gốc để gia hạn không bị tính trùng. Giá dịch vụ được snapshot tại
  thời điểm sử dụng.
- Giao ca tự tính `expected_amount` từ payment ledger CASH của actor kể từ lần
  giao ca trước. Endpoint `/adjust` đã có adjustment ledger append-only và
  idempotency key.

`CrossShiftReservationWorkflowTest` dùng HTTP → service thật → JPA/H2 để
kiểm tra tìm đặt phòng, check-in, checkout 2.400.000 VND và audit; kiểm tra từ
chối 7 role không có quyền vận hành ngay cả khi principal là người tạo; kiểm
tra từ chối actor không xác thực. Test này không chứng minh concurrency MySQL.

## Các khoảng trống xác nhận từ mã nguồn

| Mục rule | Hiện trạng và việc cần hoàn thiện |
|---|---|
| 1. Múi giờ, phạm vi | ReservationService dùng Asia/Ho_Chi_Minh; nhiều service billing/operations/finance vẫn gọi LocalDateTime.now() theo múi giờ máy. Cần một Clock nghiệp vụ thống nhất. |
| 2. Hợp đồng chuẩn | RoomStatus còn ghi/đọc mã tiếng Việt và nhận cả tên enum làm alias; payment/maintenance còn trạng thái tiếng Việt. JSON enum còn uppercase, timestamp reservation chưa có hậu tố `_at`. Chưa đạt hard cut English lower_snake_case. |
| 3. Thuê phòng | Đã kiểm tra không nhận sớm và gia hạn trước ít nhất 1 giờ. Thuê theo giờ luôn dùng công thức theo giờ, không tự chuyển sang giá ngày sau 18 giờ. |
| 4. Trả muộn | Đã tính phụ thu theo đơn giá ngày của từng phòng, có grace 20 phút và test các mốc 15/20/50/100% cùng trường hợp sau nửa đêm. |
| 5. Khả dụng | Giới hạn 3 phòng mới áp dụng trong một request; chưa cộng các đặt phòng khác của cùng khách/kỳ. Có khóa phòng nhưng IdempotencySupport cho mutation lưu trong RAM, trước transaction commit, mất khi restart và không chia sẻ giữa instance. Chưa có proof concurrency MySQL. |
| 6. Hủy/no-show | Đã chốt: khách được check-in đến trước giờ trả dự kiến; hết giờ trả mà chưa check-in thì `NO_SHOW`, giữ cọc và giải phóng phòng. Đã thêm command `POST /reservations/{id}/no-show`, chỉ chạy sau hạn và có idempotency/audit. Phí hủy bổ sung ngoài cọc vẫn chưa chốt. |
| 7. Tài chính | Ledger, cọc, biên lai và làm tròn tổng đã có. Giá dịch vụ được snapshot tại lúc dùng; checkout tách room/extension/late theo từng phòng. Adjustment ghi ledger append-only. Giao ca tự tính expected từ CASH payment/refund ledger; settlement riêng cho CARD/BANK_TRANSFER vẫn chưa có. |
| 8. VIP | Có bộ đếm độc lập, ngưỡng 10/25/50 và hạ một bậc khi vượt ngưỡng. Tuy nhiên recordCompletedStay tính lại hạng từ tổng lượt nên có thể nâng lại ngay sau hạ hạng; checkout kiểm tra thời lượng từ lịch thay vì actual check-in. Cần test chuỗi nhiều lượt và vi phạm xen kẽ. |
| 9. Thiết bị | Công thức 150%/200% và biên đúng hai năm có test. Giá trị/ngày mua ở incident vẫn nhận từ request; cần đối chiếu thiết bị đã quản lý. |
| 10. Approval | Mọi refund và hoàn cọc khi hủy miễn phí cần approval exact payload/amount do DIRECTOR duyệt và được consume một lần. Chưa có đầy đủ draft/approve/activate giá phòng và dịch vụ. |
| 11. Role/module | Đã sửa quyền reservation và liên ca trong đợt này. HR mới có EMPLOYEE_READ; chưa có module phân ca đầy đủ. Customer thiếu flow xem phòng/dịch vụ/hóa đơn của mình. Không coi ma trận endpoint hiện tại là toàn bộ quyền nghiệp vụ đã đúng. |
| 12. Thao tác phòng/giao ca | Chưa có vòng đời dọn phòng/checklist hoàn chỉnh. Quyền room/maintenance còn rộng ở một số vai trò quản lý; expected_amount giao ca đã lấy từ payment ledger. |
| 13. Actor/audit/transaction | Đã sửa actor tại ReservationService; cần tiếp tục kiểm tra các service khác. Idempotency RAM không gắn với kết quả commit; chưa đủ bảo đảm mọi mutation/audit nguyên tử khi lỗi và retry. |
| 14. Chưa chốt | Còn cách tính lượt VIP cho lưu trú một phần/loại khác và phí hủy bổ sung ngoài cọc. Không tự suy đoán hai điểm này. |

## Kiểm thử và giới hạn

- Baseline mới: `mvn -B test "-Dtest=!MySql*"`: **811 tests, 0 failures,
  0 errors, 0 skipped**. Log: `backend/rule-baseline-test.log`.
- Test workflow liên ca mới: **9/9 pass**, log `backend/rule-cross-shift-test.log`.
- Suite không-MySQL sau sửa: **820 tests, 0 failures, 0 errors, 0 skipped**,
  `BUILD SUCCESS`, log `backend/rule-final-test.log` (2 phút 21 giây).
- Suite không-MySQL sau nhóm sửa P0 này: **840 tests, 0 failures, 0 errors,
  0 skipped**, `BUILD SUCCESS` (3 phút 21 giây). Nhóm kiểm thử P0 bổ sung sau
  đó đạt **18/18**.
- MySQL 8.4 trên database thử nghiệm riêng: **7 tests, 0 failures, 0 errors,
  0 skipped**. Bao gồm Flyway/JPA validation, security smoke, hai request
  thanh toán đồng thời cùng idempotency key (một ledger row) và hai request
  đặt cùng phòng đồng thời (một booking thành công, một `OVERBOOKING`).
- Kiểm thử hồi quy sau khi xử lý deadlock/snapshot InnoDB: **35 tests, 0
  failures, 0 errors, 0 skipped** cho billing, approval, cross-shift và
  cancellation trên H2.
- `git diff --check`: **pass** sau khi dọn khoảng trắng cuối dòng tại các
  file Git báo lỗi; không đổi hành vi ở các file chỉ được dọn khoảng trắng.
- Frontend `npm run build`: **pass**. `npm test`: **fail vì chưa có test file**;
  không sửa cấu hình để biến việc thiếu test thành thành công.
- MySQL được chạy trong container dùng riêng cho acceptance và đã dọn sau test;
  không đụng dữ liệu khách sạn hiện có. Flyway hiện cảnh báo phiên bản thư viện
  chính thức mới xác nhận đến MySQL 8.1, nhưng migration, schema validation và
  các test trên MySQL 8.4 đều đạt.
- H2 workflow chạy trong transaction test có rollback; không chứng minh
  durable idempotency, commit failure hay nhiều tiến trình.

## Thứ tự tiếp tục

1. Khóa các đường hoàn tiền bằng approval DIRECTOR, cả refund cọc khi hủy;
   kiểm thử requester/approver, amount/payload, consume-once và rollback.
2. Snapshot giá, loại bỏ tính trùng gia hạn, phụ thu đúng giá ngày; thống nhất
   hourly/deposit/checkout và bổ sung bảng test số tiền nhiều phòng.
3. Giao ca lấy expected từ ledger có ranh giới ca, đối soát cash/card/bank.
4. Durable idempotency và giới hạn phòng trên toàn kỳ khách; chạy MySQL với
   transaction độc lập, retry/rollback và conflict đặt phòng/bảo trì/chuyển phòng.
5. Hard cut API/schema/enum/test cùng lúc; hoàn thiện draft/approval giá,
   HR/phân ca, vệ sinh phòng, customer ownership rồi frontend nghiệp vụ.

Không đánh dấu dự án hoàn thành hoặc sẵn sàng production khi các mục trên
chưa có bằng chứng nghiệm thu.
