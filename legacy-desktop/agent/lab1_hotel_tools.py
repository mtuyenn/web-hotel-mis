import os
import sys
from dotenv import load_dotenv

# Tải biến môi trường từ file .env (nếu có)
load_dotenv()

# =====================================================================
# BÀI THỰC HÀNH 1: BẢN CHẤT CỦA AI AGENT (FUNCTION CALLING / TOOL CALLING)
# =====================================================================
# Mục tiêu:
# 1. Hiểu cách Agent tự động chọn đúng hàm Python để giải quyết nghiệp vụ.
# 2. Xem Agent trích xuất tham số từ câu nói tự nhiên của người dùng.
# 3. Áp dụng đúng các công thức nghiệp vụ trong tài liệu Khách sạn.
# =====================================================================

# -------------------------------------------------------------
# ĐỊNH NGHĨA CÁC "TOOLS" (CÔNG CỤ) CHO AGENT
# Mỗi tool là một hàm Python có type annotation và docstring rõ ràng.
# LLM sẽ đọc docstring này để "hiểu" khi nào cần gọi hàm.
# -------------------------------------------------------------

def tinh_phu_thu_checkout_tre(gio_checkout_thuc_te: str, gia_phong: float) -> dict:
    """
    Tính tiền phụ thu khi khách check-out trễ hạn theo quy định khách sạn.
    Quy tắc (Trang 1 & Trang 6 tài liệu):
    - Được miễn phí nếu trễ <= 20 phút (trước 12:20).
    - Check-out trễ từ 12:01 đến 14:00: tính thêm 15% giá phòng.
    - Check-out trễ từ 14:01 đến 16:00: tính thêm 20% giá phòng.
    - Check-out trễ từ 16:01 đến 18:00: tính thêm 50% giá phòng.
    - Check-out trễ từ 18:01 đến 00:00: tính thêm 100% giá phòng.

    Args:
        gio_checkout_thuc_te: Giờ trả phòng thực tế định dạng HH:MM (ví dụ "14:30", "15:15", "12:15").
        gia_phong: Đơn giá phòng một ngày đêm (VNĐ).
    """
    try:
        parts = gio_checkout_thuc_te.strip().split(":")
        hour = int(parts[0])
        minute = int(parts[1])
    except Exception:
        return {"error": "Định dạng giờ không hợp lệ, vui lòng dùng HH:MM"}

    total_minutes = hour * 60 + minute
    # Mốc chuẩn check-out là 12:00 = 720 phút
    standard_checkout = 12 * 60

    if total_minutes <= standard_checkout + 20:
        rate = 0.0
        ly_do = "Được miễn phí trễ dưới 20 phút (trước 12:20)"
    elif total_minutes <= 14 * 60:
        rate = 0.15
        ly_do = "Check-out trễ từ 12:01 đến 14:00 (Phụ thu 15%)"
    elif total_minutes <= 16 * 60:
        rate = 0.20
        ly_do = "Check-out trễ từ 14:01 đến 16:00 (Phụ thu 20%)"
    elif total_minutes <= 18 * 60:
        rate = 0.50
        ly_do = "Check-out trễ từ 16:01 đến 18:00 (Phụ thu 50%)"
    else:
        rate = 1.00
        ly_do = "Check-out trễ từ 18:01 đến 00:00 (Phụ thu 100%)"

    phu_thu = gia_phong * rate
    return {
        "gio_checkout": gio_checkout_thuc_te,
        "ty_le_phu_thu": f"{int(rate * 100)}%",
        "tien_phu_thu_vnd": phu_thu,
        "quy_dinh_ap_dung": ly_do
    }


def tinh_tien_den_bu_thiet_bi(ten_thiet_bi: str, so_nam_su_dung: float, nguyen_gia: float) -> dict:
    """
    Tính tiền đền bù khi khách làm mất hoặc hư hỏng trang thiết bị trong phòng.
    Quy tắc (Trang 1 & Trang 6 tài liệu):
    - Đền bù bằng 150% giá trị thiết bị đối với đồ dùng mua chưa quá 2 năm (<= 2 năm).
    - Đền bù bằng 200% đối với đồ dùng mua trên 2 năm (> 2 năm).

    Args:
        ten_thiet_bi: Tên thiết bị bị hỏng/mất (ví dụ "Tivi", "Máy sấy tóc", "Ấm đun nước").
        so_nam_su_dung: Số năm thiết bị đã đưa vào sử dụng (ví dụ 1.5, 3.0).
        nguyen_gia: Giá trị gốc lúc mua của thiết bị (VNĐ).
    """
    if so_nam_su_dung <= 2.0:
        ty_le = 1.50
        ghi_chu = "Thiết bị sử dụng <= 2 năm: Bồi thường 150% nguyên giá"
    else:
        ty_le = 2.00
        ghi_chu = "Thiết bị sử dụng > 2 năm: Bồi thường 200% nguyên giá"

    tien_den_bu = nguyen_gia * ty_le
    return {
        "thiet_bi": ten_thiet_bi,
        "so_nam_su_dung": so_nam_su_dung,
        "ty_le_boi_thuong": f"{int(ty_le * 100)}%",
        "tien_den_bu_vnd": tien_den_bu,
        "chinh_sach": ghi_chu
    }


def kiem_tra_khoa_dat_truoc(ho_ten: str, so_lan_huy_sat_gio: int) -> dict:
    """
    Kiểm tra tình trạng quyền đặt trước của khách hàng.
    Quy định (Trang 1 & Trang 4 tài liệu):
    - Tự động khóa quyền đặt phòng trước nếu khách có số lần hủy phòng sát giờ quá 3 lần (> 3 lần).

    Args:
        ho_ten: Họ và tên khách hàng.
        so_lan_huy_sat_gio: Số lần khách đã hủy phòng sát giờ trong quá khứ.
    """
    bi_khoa = so_lan_huy_sat_gio > 3
    return {
        "khach_hang": ho_ten,
        "so_lan_huy_sat_gio": so_lan_huy_sat_gio,
        "trang_thai_quyen_dat": "ĐÃ BỊ KHÓA ĐẶT TRƯỚC" if bi_khoa else "BÌNH THƯỜNG",
        "ghi_chu": "Khách đã hủy sát giờ quá 3 lần nên bị khóa theo quy định" if bi_khoa else "Khách vẫn có quyền đặt phòng trước bình thường"
    }


# Danh sách các tools đưa cho Agent
MY_HOTEL_TOOLS = [
    tinh_phu_thu_checkout_tre,
    tinh_tien_den_bu_thiet_bi,
    kiem_tra_khoa_dat_truoc
]


def run_agent_lab():
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key or api_key == "your_gemini_api_key_here":
        print("=" * 65)
        print("CHƯA CÓ GEMINI_API_KEY!")
        print("Để chạy Agent thực tế, bạn chỉ cần:")
        print("1. Lấy API key miễn phí tại: https://aistudio.google.com/")
        print("2. Mở file agent/.env và dán: GEMINI_API_KEY=AIzaSy...")
        print("=" * 65)
        print("\n[MÔ PHỎNG LUỒNG SUY NGHĨ CỦA AGENT]:")
        print("Khi bạn hỏi: 'Khách phòng VIP giá 1.200.000 VNĐ trả phòng lúc 15:10, tính tiền giúp tôi?'")
        print("-> Agent tự phân tích và gọi: tinh_phu_thu_checkout_tre('15:10', 1200000.0)")
        res = tinh_phu_thu_checkout_tre("15:10", 1200000.0)
        print(f"-> Kết quả từ Tool: {res}")
        print("-> Phản hồi: Khách trả phòng lúc 15:10 (thuộc khung 14:01-16:00), phụ thu 20% tương đương 240.000 VNĐ.\n")
        return

    from google import genai
    from google.genai import types

    print("Đang khởi tạo Agent với Google Gemini Client...")
    client = genai.Client(api_key=api_key)

    cau_hoi = [
        "Phòng 204 vừa làm vỡ cái ấm đun nước, tính tiền đền bù giúp tôi?"
    ]

    for idx, q in enumerate(cau_hoi, 1):
        print("\n" + "=" * 70)
        print(f"TEST CASE {idx}:")
        print(f"Người dùng hỏi: \"{q}\"")
        print("-" * 70)

        chat = client.chats.create(
            model='gemini-3.6-flash',
            config=types.GenerateContentConfig(
                tools=MY_HOTEL_TOOLS,
                temperature=0.1
            )
        )
        response = chat.send_message(q)
        print("AGENT TRẢ LỜI:")
        print(response.text)


if __name__ == "__main__":
    run_agent_lab()
