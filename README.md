# web-hotel-mis

Project web mới cho Hotel MIS. Repository desktop Java Swing được giữ độc lập tại:

- Desktop legacy: https://github.com/mtuyenn/hotel-mis
- Web project: https://github.com/mtuyenn/web-hotel-mis

## Cấu trúc

```text
backend/              Spring Boot API và application use cases
frontend/             Web UI React/TypeScript (sẽ triển khai ở milestone tiếp theo)
database/legacy/      Schema/sample data được lấy làm baseline tham chiếu
docs/                 Kiến trúc và kế hoạch chuyển đổi
```

Không copy mã Swing, thư mục `target`, IDE metadata, credential DB hoặc file `.env` từ desktop sang repository này.

## Nguyên tắc chuyển đổi

1. Backend API là boundary duy nhất cho web và chatbot.
2. LLM/agent không truy cập MySQL trực tiếp và không tự sinh SQL production.
3. Dữ liệu nghiệp vụ lấy qua API/tool; tài liệu quy trình mới đi qua RAG.
4. Desktop tiếp tục vận hành trong repository cũ cho đến khi từng use case được chuyển và kiểm chứng.

Xem [docs/architecture.md](docs/architecture.md) và [docs/migration-plan.md](docs/migration-plan.md).
