# ADR-0001: Bắt đầu bằng modular monolith

## Quyết định

Backend bắt đầu là một Spring Boot modular monolith. Các capability được tách
package và dependency boundary, nhưng deploy cùng một ứng dụng. Backend API là
DB writer duy nhất và là boundary cho mọi thao tác nghiệp vụ.

## Lý do

- Repository hiện tại còn nhỏ và chưa có contract API ổn định.
- Nghiệp vụ đặt phòng, phòng, hóa đơn và tồn kho có transaction liên kết chặt.
- Giảm chi phí vận hành và tránh phân tán dữ liệu quá sớm.
- Một writer duy nhất giúp transaction, authorization và audit có một nguồn
  điều phối rõ ràng.

## Hệ quả

- Không import trực tiếp package nội bộ của module khác; giao tiếp qua
  application port.
- Flyway là authority duy nhất cho schema; Hibernate chỉ validate mapping.
- Có thể tách thành service riêng sau khi có nhu cầu vận hành đo được, nhưng
  không thay đổi API/database ownership contract.
