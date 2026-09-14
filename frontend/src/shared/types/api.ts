// Hợp đồng lỗi chung tương ứng với ApiError phía backend và payload JSON snake_case.
export type ApiError = {
  /** Thời điểm backend tạo lỗi, thường là chuỗi ISO-8601. */
  timestamp: string;
  /** Mã HTTP để quyết định cách hiển thị hoặc retry. */
  status: number;
  /** Mã ổn định cho logic xử lý lỗi theo loại. */
  code: string;
  /** Thông điệp tổng quát dành cho người dùng hoặc log. */
  message: string;
  /** Danh sách lỗi chi tiết, nhất là lỗi validation theo trường. */
  details: string[];
};

export type PublicRoomStatus = "READY" | "RESERVED" | "OCCUPIED" | "CLEANING" | "MAINTENANCE" | "OUT_OF_SERVICE";

export type PublicRoomSummary = {
  room_id: string;
  room_name: string;
  room_type_id: string;
  room_type_name: string;
  daily_price: number;
  floor: number | null;
  status: PublicRoomStatus;
};

export type PublicRoomDetail = PublicRoomSummary & {
  room_type_description: string | null;
  description: string | null;
  image_urls: string[];
  amenities: string[];
};

export type PublicRoomAvailability = {
  room_id: string;
  room_name: string;
  room_type_id: string;
  room_type_name: string;
  daily_price: number;
  floor: number | null;
  current_status: PublicRoomStatus;
  available: boolean;
};

export type PublicServiceSummary = {
  service_id: string;
  name: string;
  price: number;
  unit: string;
};

export type CustomerTokenResponse = {
  access_token: string;
  refresh_token: string;
  token_type: string;
  expires_in: number;
  refresh_expires_in: number;
};

export type CustomerRegisterRequest = {
  phone: string;
  password: string;
  full_name: string;
  identity_number: string;
};

export type CustomerBookingRequest = {
  rental_type: "PACKAGE" | "HOURLY";
  rooms: Array<{ room_id: string; expected_check_in: string; expected_check_out: string }>;
  idempotency_key: string;
};

export type CustomerBooking = {
  id: number;
  status: string;
  rental_type: "PACKAGE" | "HOURLY";
  deposit_amount: number;
  booked_at: string;
  rooms: Array<{ room_id: string; expected_check_in: string; expected_check_out: string }>;
  deposit_payment: {
    payment_code: string;
    amount: number;
    status: "NOT_REQUIRED" | "PENDING" | "PAID" | "EXPIRED";
    expires_at: string | null;
    instruction: string;
  };
};
