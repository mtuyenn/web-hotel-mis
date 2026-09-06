import os
import sys
from decimal import Decimal
import pymysql
from dotenv import load_dotenv
from google import genai
from google.genai import types

# Tải biến môi trường từ file .env ngay trong thư mục agent (ưu tiên ghi đè biến hệ thống)
env_path = os.path.join(os.path.dirname(__file__), ".env")
load_dotenv(env_path, override=True)

# =====================================================================
# BÀI THỰC HÀNH 2: AI AGENT KẾT NỐI TRỰC TIẾP DATABASE KHÁCH SẠN (MySQL QLKS)
# =====================================================================
# Tính năng nổi bật:
# 1. Agent tự truy vấn Database MySQL để lấy giá phòng, trạng thái buồng phòng, thông tin khách.
# 2. Người dùng hỏi thiếu thông tin (ví dụ chỉ nói số phòng) -> Agent tự tìm giá phòng trong DB.
# 3. Vòng lặp tương tác (Interactive Loop while True) -> gõ 'thoát' để dừng.
# =====================================================================

DB_CONFIG = {
    "host": "localhost",
    "user": "root",
    "password": "mtuyen123",
    "database": "QLKS",
    "charset": "utf8mb4",
    "cursorclass": pymysql.cursors.DictCursor
}

def get_db_connection():
    return pymysql.connect(**DB_CONFIG)


# -------------------------------------------------------------
# CÁC "TOOLS" (CÔNG CỤ) AGENT DÙNG ĐỂ ĐỌC DATABASE & XỬ LÝ NGHIỆP VỤ
# -------------------------------------------------------------

def tra_cuu_thong_tin_phong(ma_phong: str) -> dict:
    """
    Tra cứu thông tin chi tiết của một phòng trong database khách sạn (giá phòng, loại phòng, tình trạng hiện tại).
    
    Args:
        ma_phong: Mã phòng cần tra cứu (ví dụ: 'P101', 'P102', 'P201', 'P301').
    """
    ma_phong = ma_phong.strip().upper()
    try:
        conn = get_db_connection()
        with conn.cursor() as cur:
            sql = """
                SELECT p.MAPHONG, lp.TENLOAI, lp.GIA, p.TINHTRANG, p.MOTA 
                FROM phong p
                JOIN loaiphong lp ON p.MALOAIPHONG = lp.MALOAIPHONG
                WHERE p.MAPHONG = %s
            """
            cur.execute(sql, (ma_phong,))
            row = cur.fetchone()
        conn.close()
        
        if row:
            # Chuyển Decimal sang float để JSON serialize được
            row["GIA"] = float(row["GIA"])
            return {"status": "success", "data": row}
        else:
            return {"status": "not_found", "message": f"Không tìm thấy phòng có mã '{ma_phong}' trong database."}
    except Exception as e:
        return {"status": "error", "message": str(e)}


def tim_phong_theo_trang_thai(tinh_trang: str = "SAN_SANG") -> dict:
    """
    Tìm danh sách các phòng trong database theo tình trạng buồng phòng.
    Các trạng thái hợp lệ trong hệ thống:
    - 'SAN_SANG': Phòng trống sẵn sàng đón khách
    - 'DANG_O': Phòng đang có khách lưu trú
    - 'DANG_DON_DEP': Phòng đang dọn dẹp vệ sinh
    - 'BAO_TRI': Phòng đang sửa chữa/bảo trì
    
    Args:
        tinh_trang: Tình trạng phòng cần tìm (mặc định là 'SAN_SANG').
    """
    tinh_trang = tinh_trang.strip().upper()
    try:
        conn = get_db_connection()
        with conn.cursor() as cur:
            sql = """
                SELECT p.MAPHONG, lp.TENLOAI, lp.GIA, p.TINHTRANG, p.MOTA 
                FROM phong p
                JOIN loaiphong lp ON p.MALOAIPHONG = lp.MALOAIPHONG
                WHERE p.TINHTRANG = %s
            """
            cur.execute(sql, (tinh_trang,))
            rows = cur.fetchall()
        conn.close()
        
        for r in rows:
            r["GIA"] = float(r["GIA"])
            
        return {
            "status": "success",
            "tinh_trang_tra_cuu": tinh_trang,
            "so_luong": len(rows),
            "danh_sach_phong": rows
        }
    except Exception as e:
        return {"status": "error", "message": str(e)}


def tinh_phu_thu_checkout_tre(ma_phong: str, gio_checkout_thuc_te: str) -> dict:
    """
    Tính tiền phụ thu khi khách trả phòng trễ hạn. Tự động đọc giá phòng từ Database.
    Quy tắc nghiệp vụ:
    - Miễn phí nếu check-out trễ <= 20 phút (trước 12:20).
    - Trễ từ 12:01 đến 14:00: Phụ thu 15% giá phòng.
    - Trễ từ 14:01 đến 16:00: Phụ thu 20% giá phòng.
    - Trễ từ 16:01 đến 18:00: Phụ thu 50% giá phòng.
    - Trễ từ 18:01 đến 00:00: Phụ thu 100% giá phòng.

    Args:
        ma_phong: Mã phòng (ví dụ 'P102', 'P204'). Hàm sẽ tự lấy giá phòng trong DB.
        gio_checkout_thuc_te: Giờ trả phòng thực tế định dạng HH:MM (ví dụ '14:30', '15:15').
    """
    thong_tin = tra_cuu_thong_tin_phong(ma_phong)
    if thong_tin.get("status") != "success":
        return thong_tin

    gia_phong = thong_tin["data"]["GIA"]
    ten_loai = thong_tin["data"]["TENLOAI"]

    try:
        parts = gio_checkout_thuc_te.strip().split(":")
        hour = int(parts[0])
        minute = int(parts[1])
    except Exception:
        return {"error": "Định dạng giờ không hợp lệ. Vui lòng cung cấp giờ theo dạng HH:MM."}

    total_minutes = hour * 60 + minute
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
        "ma_phong": ma_phong,
        "loai_phong": ten_loai,
        "gia_phong_ngay_vnd": gia_phong,
        "gio_checkout_thuc_te": gio_checkout_thuc_te,
        "ty_le_phu_thu": f"{int(rate * 100)}%",
        "tien_phu_thu_vnd": phu_thu,
        "tong_tien_sau_phu_thu_vnd": gia_phong + phu_thu,
        "chinh_sach_ap_dung": ly_do
    }


def tra_cuu_khach_hang(tu_khoa: str) -> dict:
    """
    Tra cứu thông tin khách hàng trong database theo họ tên, số điện thoại hoặc số CCCD.
    
    Args:
        tu_khoa: Tên khách hàng (hoặc một phần tên), số điện thoại hoặc CCCD.
    """
    try:
        conn = get_db_connection()
        with conn.cursor() as cur:
            sql = """
                SELECT MAKH, TENKH, SODIENTHOAI, CCCD, EMAIL, DIACHI
                FROM khachhang
                WHERE TENKH LIKE %s OR SODIENTHOAI LIKE %s OR CCCD LIKE %s
            """
            kw = f"%{tu_khoa.strip()}%"
            cur.execute(sql, (kw, kw, kw))
            rows = cur.fetchall()
        conn.close()
        
        return {
            "status": "success",
            "tu_khoa": tu_khoa,
            "so_ket_qua": len(rows),
            "ket_qua": rows
        }
    except Exception as e:
        return {"status": "error", "message": str(e)}


def tra_cuu_danh_muc_dich_vu() -> dict:
    """
    Tra cứu toàn bộ danh mục dịch vụ và đơn giá của khách sạn trong database (nước suối, giặt ủi, ăn sáng, v.v.).
    """
    try:
        conn = get_db_connection()
        with conn.cursor() as cur:
            sql = "SELECT MADV, TENDICHVU, GIADV FROM dichvu ORDER BY GIADV ASC"
            cur.execute(sql)
            rows = cur.fetchall()
        conn.close()
        
        for r in rows:
            r["GIADV"] = float(r["GIADV"])
            
        return {"status": "success", "danh_muc": rows}
    except Exception as e:
        return {"status": "error", "message": str(e)}


# Tập hợp các tools đưa cho Agent
DB_AGENT_TOOLS = [
    tra_cuu_thong_tin_phong,
    tim_phong_theo_trang_thai,
    tinh_phu_thu_checkout_tre,
    tra_cuu_khach_hang,
    tra_cuu_danh_muc_dich_vu
]


def interactive_chat():
    api_key = os.getenv("GEMINI_API_KEY")
    client = genai.Client(api_key=api_key)

    print("=" * 70)
    print("🏨 HỆ THỐNG AI AGENT QUẢN LÝ KHÁCH SẠN (KẾT NỐI DATABASE MYSQL: QLKS)")
    print("=" * 70)
    print("• Agent đã được cấp quyền đọc dữ liệu từ MySQL (phòng, loại phòng, khách hàng, dịch vụ).")
    print("• Nếu bạn hỏi thiếu thông tin, Agent sẽ tự hỏi lại hoặc tự tra Database để bù đắp.")
    print("• Gõ 'thoát' (hoặc 'exit') để dừng chương trình.\n")

    # Tạo phiên chat với Gemini 3.7 Flash kèm danh sách Tools
    chat = client.chats.create(
        model='gemini-3.7-flash',
        config=types.GenerateContentConfig(
            tools=DB_AGENT_TOOLS,
            temperature=0.2,
            system_instruction=(
                "Bạn là Trợ lý Lễ tân & Quản trị AI thông minh của Khách sạn. "
                "Bạn có quyền truy cập các công cụ để đọc trực tiếp dữ liệu từ Database MySQL của khách sạn. "
                "Khi người dùng hỏi về phòng, khách hàng, dịch vụ hoặc tính tiền phạt, hãy ưu tiên dùng các Tools "
                "để kiểm tra dữ liệu thực tế. "
                "Nếu người dùng hỏi cộc lốc hoặc thiếu thông tin cần thiết mà Database không có, hãy lịch sự hỏi lại để làm rõ."
            )
        )
    )

    while True:
        try:
            user_input = input("\n👤 Bạn: ").strip()
            
            if not user_input:
                continue
                
            if user_input.lower() in ["thoat", "thoát", "exit", "quit", "q"]:
                print("\n👋 Tạm biệt bạn! Chúc bạn học tốt lộ trình AI Engineer!")
                return

            print("🤖 Agent đang suy nghĩ & tra cứu Database...")
            response = chat.send_message(user_input)
            print(f"\n🏨 Agent:\n{response.text}\n")
            print("-" * 70)

        except KeyboardInterrupt:
            print("\n👋 Đã nhận lệnh ngắt. Thoát chương trình.")
            return
        except Exception as e:
            err_msg = str(e)
            if "429" in err_msg or "RESOURCE_EXHAUSTED" in err_msg:
                print("\n⚠️ [LỖI 429: HẾT HẠN MỨC GỌI API (RATE LIMIT / QUOTA)]")
            else:
                print(f"\n❌ Đã xảy ra lỗi: {e}")   


if __name__ == "__main__":
    interactive_chat()
