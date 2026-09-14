// Giữ các định nghĩa tuyến đường tại một nơi để các lớp bảo vệ xác thực và ranh giới quyền hạn
// có thể được áp dụng nhất quán khi bổ sung tính năng.
export const appRoutes = [
  "/guest/rooms",
  "/guest/rooms/:id",
  "/guest/services",
  "/guest/login",
  "/guest/register",
  "/guest/bookings",
  "/guest/bookings/new",
  "/guest/bookings/:id/payment",
  "/staff",
  "/staff/front-desk",
  "/staff/housekeeping",
  "/staff/kitchen",
  "/staff/accounting",
  "/staff/hr",
  "/staff/technical",
  "/staff/governance",
  "/staff/admin"
] as const;
