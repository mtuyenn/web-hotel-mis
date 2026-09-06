# 🏨 KHU VỰC THỰC HÀNH AI AGENT - DỰ ÁN HOTEL MIS

> Snapshot này chỉ phục vụ đối chiếu migration. Prototype truy cập DB trực tiếp và file credential đã được loại khỏi repository web. Không dùng snapshot này cho production.

Chào mừng bạn đến với module **AI Agent** dành cho sinh viên CNTT định hướng **AI Engineer / GenAI Developer**!

---

## 📂 Cấu trúc thư mục

```
c:\hotel-mis\agent\
├── .env.example          # File mẫu cấu hình API Key
├── lab1_hotel_tools.py   # BÀI THỰC HÀNH 1: Hiểu bản chất Function Calling & Tool Calling
├── lab2_db_agent.py      # BÀI THỰC HÀNH 2: Đọc trực tiếp Database MySQL (QLKS) + Interactive Chat (while True)
└── README.md             # Hướng dẫn chi tiết từng bài
```

---

## 🚀 Hướng dẫn chạy Bài thực hành 1 (`lab1_hotel_tools.py`)

### 1. Lấy API Key Google Gemini (Hoàn toàn Miễn phí):
1. Truy cập: [Google AI Studio](https://aistudio.google.com/)
2. Bấm nút **"Get API key"** hoặc **"Create API key"**.
3. Copy chuỗi API key đó.

### 2. Cấu hình Key vào file `.env`:
Tạo một file `.env` ngay trong thư mục `agent/` (bên cạnh `.env.example`):
```ini
GEMINI_API_KEY=AIzaSy... (dán key của bạn vào đây)
```

### 3. Chạy thử nghiệm:
Mở terminal tại thư mục `c:\hotel-mis\agent` và chạy:
```bash
python lab1_hotel_tools.py
```

---

## 🧠 Những gì diễn ra trong Bài 1:

Trong file `lab1_hotel_tools.py`, chúng ta đã mã hóa **3 quy tắc nghiệp vụ từ file PDF 18 trang** thành 3 Python Tools:

1. `tinh_phu_thu_checkout_tre(gio_checkout_thuc_te, gia_phong)`:
   - Tự động áp dụng khung giờ phạt:
     - Trước 12:20: Miễn phí.
     - 12:01 – 14:00: Phụ thu 15%.
     - 14:01 – 16:00: Phụ thu 20%.
     - 16:01 – 18:00: Phụ thu 50%.
     - 18:01 – 00:00: Phụ thu 100%.

2. `tinh_tien_den_bu_thiet_bi(ten_thiet_bi, so_nam_su_dung, nguyen_gia)`:
   - Tính bồi thường tài sản hỏng:
     - Dưới hoặc bằng 2 năm: Phạt 150% giá gốc.
     - Trên 2 năm: Phạt 200% giá gốc.

3. `kiem_tra_khoa_dat_truoc(ho_ten, so_lan_huy_sat_gio)`:
   - Nếu khách hàng hủy phòng sát giờ quá 3 lần: Tự động khóa quyền đặt trước.

---

## 🎯 Điều bạn sẽ học được từ bài này:
- Bạn không cần viết `if/else` để phân tích câu nói của người dùng!
- Mô hình ngôn ngữ (LLM) sẽ **tự đọc hiểu yêu cầu**, **tự chọn đúng hàm cần gọi**, và **tự truyền đúng tham số** (ví dụ trích xuất giờ `"15:30"` hay tính số năm `"3 năm"`).
- Đây là **nền tảng cốt lõi số 1** của mọi AI Agent hiện đại trên thế giới.
