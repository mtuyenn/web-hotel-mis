# web-hotel-mis

Hệ thống quản lý một khách sạn: API Spring Boot, giao diện React/TypeScript,
SQL Server và trợ lý khách hàng FastAPI/RAG.

## Chạy local

Từ thư mục gốc, khởi động các dịch vụ phụ thuộc:

```powershell
docker compose up -d --build sqlserver sqlserver-init qdrant agent
```

Compose init chỉ tạo QLKS khi chưa có database. Backend chạy Flyway và
Hibernate validate; khởi động ứng dụng không reset dữ liệu demo. Giữ các
volume SQL Server/Qdrant khi dừng dịch vụ.

Chạy backend và frontend trong hai terminal:

```powershell
Set-Location C:\web-hotel-mis\backend
mvn spring-boot:run
```

```powershell
Set-Location C:\web-hotel-mis\frontend
npm ci
npm run dev -- --host 127.0.0.1 --port 5173
```

Backend dùng cổng 8080, frontend 5173, agent 8090. Vite proxy chuyển
`/api` và `/media` tới backend, `/agent-api` tới agent.
Xem [hướng dẫn agent](agent/README.md) để cài môi trường, cấu hình Gemini
và lập chỉ mục chính sách khách hàng. Sau khi sửa `customer-policy.md`,
chạy lại `index-customer-policy` theo hướng dẫn đó.

Để làm mới dữ liệu vận hành trước buổi báo cáo, dùng
[hướng dẫn dữ liệu demo](database/demo/README.md). Bộ dữ liệu báo cáo
21/10 hiện có được mô tả trong [kịch bản trình diễn](database/demo/report_demo_2026-10-21.md).

## Tài liệu theo nơi chịu trách nhiệm

Đây là mục lục chung của kho mã; các README bộ phận chứa cách dùng tại chỗ.

| Nội dung | Tài liệu |
|---|---|
| Bất biến của repository, sáu migration, cấu hình demo do chủ repo quản lý | [AGENTS.md](AGENTS.md) |
| Quy tắc nghiệp vụ và phân quyền | [rule.md](rule.md) |
| Điều khoản, quyền lợi và giá công bố cho khách; nguồn RAG | [customer-policy.md](customer-policy.md) |
| Kiến trúc và ranh giới các module | [Kiến trúc](docs/architecture.md) |
| Endpoint, payload và quyền API | [Hợp đồng API](docs/api-contract.md) |
| Adapter, object SQL và test bảo vệ | [Truy cập database](docs/database-access-matrix.md) |
| Thuật ngữ tiếng Việt | [Từ điển nghiệp vụ](docs/tu-dien-nghiep-vu-tieng-viet.md) |
| Ánh xạ Java/API sang giá trị database | [Từ điển giá trị](docs/doi-chieu-gia-tri-database.csv) |
| Backend, test SQL Server và VNPay sandbox | [Backend](backend/README.md) |
| Giao diện và lệnh frontend | [Frontend](frontend/README.md) |
| Thiết lập kiểm thử HTTP trên database dùng một lần | [Live E2E](frontend/e2e/README.md) |
| Gemini, Qdrant và công cụ trợ lý | [Agent](agent/README.md) |
| Nạp demo và dữ liệu báo cáo | [Demo](database/demo/README.md) |
| Tổ chức object SQL và trigger | [Migration](backend/src/main/resources/db/migration/README.md) |

## Kiểm tra

[CI](.github/workflows/ci.yml) chạy `mvn -B test` với SQL Server dùng một
lần, `npm test`, `npm run build` và `python -m pytest` trong bộ phận tương ứng.
SQL Server tests cần các biến `MIGRATION_TEST_DB_*` theo hướng dẫn backend;
không trỏ vào QLKS demo đang dùng để báo cáo. Live HTTP E2E có thiết lập
riêng; trường hợp bị skip không được tính là đã kiểm chứng.
