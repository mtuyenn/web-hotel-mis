# ADR-0001: Bắt đầu bằng modular monolith

## Quyết định

Backend bắt đầu là một Spring Boot modular monolith. Các capability được tách package và dependency boundary, nhưng deploy cùng một ứng dụng.

## Lý do

- Repository hiện tại còn nhỏ và chưa có contract API ổn định.
- Nghiệp vụ đặt phòng, phòng, hóa đơn và tồn kho có transaction liên kết chặt.
- Giảm chi phí vận hành và tránh phân tán dữ liệu quá sớm.

## Hệ quả

- Không import trực tiếp package nội bộ của module khác; giao tiếp qua application port.
- Có thể tách thành service riêng sau khi có nhu cầu vận hành đo được.
- `legacy/` chỉ là vùng chuyển tiếp, không phải boundary production.
