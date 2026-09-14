import { createContext, useContext, useEffect, useMemo, useState } from "react";
import type { FormEvent, ReactNode } from "react";
import {
  BrowserRouter,
  Link,
  Navigate,
  NavLink,
  Outlet,
  Route,
  Routes,
  useNavigate,
  useSearchParams,
  useParams
} from "react-router-dom";
import { getJson, postJson } from "../shared/api/http";
import type {
  CustomerBooking,
  CustomerBookingRequest,
  CustomerRegisterRequest,
  CustomerTokenResponse,
  PublicRoomAvailability,
  PublicRoomDetail,
  PublicRoomStatus,
  PublicRoomSummary,
  PublicServiceSummary
} from "../shared/types/api";

type AuthContextValue = { accessToken: string | null; login: (tokens: CustomerTokenResponse) => void; logout: () => void };
const AuthContext = createContext<AuthContextValue>({ accessToken: null, login: () => undefined, logout: () => undefined });
function useAuth() { return useContext(AuthContext); }

function AuthProvider({ children }: { children: ReactNode }) {
  const [accessToken, setAccessToken] = useState(() => window.localStorage.getItem("hotel-os-access-token"));
  const login = (tokens: CustomerTokenResponse) => {
    window.localStorage.setItem("hotel-os-access-token", tokens.access_token);
    window.localStorage.setItem("hotel-os-refresh-token", tokens.refresh_token);
    setAccessToken(tokens.access_token);
  };
  const logout = () => {
    window.localStorage.removeItem("hotel-os-access-token");
    window.localStorage.removeItem("hotel-os-refresh-token");
    setAccessToken(null);
  };
  return <AuthContext.Provider value={{ accessToken, login, logout }}>{children}</AuthContext.Provider>;
}

const statusLabels: Record<PublicRoomStatus, string> = {
  READY: "Sẵn sàng đón khách",
  RESERVED: "Đã có người đặt",
  OCCUPIED: "Đang có khách",
  CLEANING: "Đang dọn",
  MAINTENANCE: "Đang bảo trì",
  OUT_OF_SERVICE: "Không phục vụ"
};

function statusLabel(status: PublicRoomStatus) {
  return statusLabels[status] ?? "Không phục vụ";
}

function PublicLayout() {
  const { accessToken, logout } = useAuth();
  return (
    <div className="public-app">
      <header className="site-header">
        <Link className="brand" to="/guest/rooms" aria-label="Hotel OS trang chủ">
          <span className="brand-mark">H</span>
          <span><strong>Hotel OS</strong><small>Guest preview</small></span>
        </Link>
        <nav className="public-nav" aria-label="Điều hướng public portal">
          <NavLink to="/guest/rooms">Phòng</NavLink>
          <NavLink to="/guest/services">Dịch vụ</NavLink>
          {accessToken ? <><NavLink to="/guest/bookings">Booking của tôi</NavLink><button className="nav-action" onClick={logout}>Đăng xuất</button></> : <NavLink to="/guest/login">Đăng nhập để đặt phòng</NavLink>}
          <span className="portal-status"><i /> Chỉ xem công khai</span>
        </nav>
      </header>
      <main className="public-main"><Outlet /></main>
      <footer className="site-footer"><span>Hotel OS · Thông tin từ khách sạn</span><span>Không hiển thị thông tin khách lưu trú</span></footer>
    </div>
  );
}

function LoadingState({ label }: { label: string }) {
  return <div className="state-card" role="status"><span className="spinner" />{label}</div>;
}

function ErrorState({ onRetry }: { onRetry: () => void }) {
  return <div className="state-card error-state" role="alert"><div><strong>Không thể tải dữ liệu</strong><p>Kiểm tra kết nối tới Hotel OS rồi thử lại.</p></div><button className="button button-secondary" onClick={onRetry}>Thử lại</button></div>;
}

function EmptyState({ children }: { children: ReactNode }) {
  return <div className="state-card empty-state"><span className="empty-icon">⌂</span><span>{children}</span></div>;
}

function StatusBadge({ status, available }: { status: PublicRoomStatus; available?: boolean }) {
  const text = available === false ? "Không khả dụng trong khoảng này" : statusLabel(status);
  return <span className={`status-badge status-${status.toLowerCase()}`}>{text}</span>;
}

function RoomVisual({ roomName, imageUrls = [] }: { roomName: string; imageUrls?: string[] }) {
  if (imageUrls.length > 0) return <img className="room-visual-image" src={imageUrls[0]} alt={`Ảnh ${roomName}`} />;
  return <div className="room-visual" aria-label={`Khu vực ảnh của ${roomName}`}><div className="window-shape" /><div className="bed-shape" /><div className="lamp-shape" /><span>Ảnh phòng sẽ được cập nhật</span></div>;
}

function RoomCard({ room, availability }: { room: PublicRoomSummary | PublicRoomAvailability; availability?: PublicRoomAvailability }) {
  const currentStatus = availability?.current_status ?? ("status" in room ? room.status : "OUT_OF_SERVICE");
  const bookingTarget = availability?.available ? `/guest/bookings/new?room=${encodeURIComponent(room.room_id)}` : undefined;
  return <article className="room-card"><RoomVisual roomName={room.room_name} /><div className="room-card-body"><div className="room-card-heading"><div><span className="room-kicker">TẦNG {room.floor ?? "—"}</span><h3>{room.room_name}</h3></div><StatusBadge status={currentStatus} available={availability?.available} /></div><p className="room-type">{room.room_type_name}</p><div className="room-card-footer"><span><strong>{new Intl.NumberFormat("vi-VN").format(room.daily_price)} đ</strong> / ngày</span><span className="room-actions"><Link className="text-link" to={`/guest/rooms/${room.room_id}`}>Chi tiết <span aria-hidden="true">↗</span></Link>{bookingTarget && <Link className="button button-small" to={bookingTarget}>Đặt phòng</Link>}</span></div></div></article>;
}

function GuestRoomsPage() {
  const [rooms, setRooms] = useState<PublicRoomSummary[]>([]);
  const [availability, setAvailability] = useState<PublicRoomAvailability[] | undefined>();
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const loadRooms = async () => { setLoading(true); setError(false); setAvailability(undefined); try { setRooms(await getJson<PublicRoomSummary[]>("/api/public/rooms")); } catch { setError(true); } finally { setLoading(false); } };
  useEffect(() => { void loadRooms(); }, []);

  const searchAvailability = async (event: FormEvent) => {
    event.preventDefault();
    if (!from || !to) { await loadRooms(); return; }
    setLoading(true); setError(false);
    try { const normalize = (value: string) => `${value}:00`; setAvailability(await getJson<PublicRoomAvailability[]>(`/api/public/rooms/availability?from=${encodeURIComponent(normalize(from))}&to=${encodeURIComponent(normalize(to))}`)); } catch { setError(true); } finally { setLoading(false); }
  };
  const displayRooms = useMemo(() => availability ?? rooms, [availability, rooms]);

  return <><section className="hero-section"><div className="hero-copy"><span className="eyebrow">KHÁM PHÁ KHÔNG GIAN LƯU TRÚ</span><h1>Một căn phòng đúng<br /><em>cho nhịp nghỉ của bạn.</em></h1><p>Xem phòng, tiện nghi và trạng thái hiện tại của khách sạn. Khi chọn thời gian, hệ thống sẽ kiểm tra khả dụng theo đúng khoảng bạn cần.</p></div><div className="hero-aside"><span className="hero-number">01</span><span>ROOM<br />DIRECTORY</span></div></section><section className="search-panel" aria-label="Tìm phòng theo thời gian"><div className="panel-heading"><div><span className="section-label">TÌM THEO THỜI GIAN</span><h2>Kiểm tra phòng còn trống</h2></div><span className="privacy-note">● Không hiển thị thông tin người đặt</span></div><form className="availability-form" onSubmit={searchAvailability}><label>Nhận phòng<input type="datetime-local" value={from} onChange={(event) => setFrom(event.target.value)} /></label><label>Trả phòng<input type="datetime-local" value={to} onChange={(event) => setTo(event.target.value)} /></label><button className="button button-primary" type="submit">Kiểm tra khả dụng <span aria-hidden="true">→</span></button>{availability && <button className="button button-quiet" type="button" onClick={loadRooms}>Xóa bộ lọc</button>}</form></section><section className="catalog-section"><div className="section-heading"><div><span className="section-label">ROOM DIRECTORY</span><h2>Phòng trong khách sạn</h2></div><span className="catalog-count">{displayRooms.length} phòng</span></div>{loading ? <LoadingState label="Đang tải danh sách phòng..." /> : error ? <ErrorState onRetry={loadRooms} /> : displayRooms.length === 0 ? <EmptyState>Hiện chưa có phòng phù hợp.</EmptyState> : <div className="room-grid">{displayRooms.map((room) => <RoomCard key={room.room_id} room={room} availability={availability?.find((item) => item.room_id === room.room_id)} />)}</div>}</section></>;
}

function GuestRoomDetailPage() {
  const { id } = useParams();
  const [room, setRoom] = useState<PublicRoomDetail | null>(null);
  const [loading, setLoading] = useState(true); const [error, setError] = useState(false);
  const load = async () => { if (!id) return; setLoading(true); setError(false); try { setRoom(await getJson<PublicRoomDetail>(`/api/public/rooms/${encodeURIComponent(id)}`)); } catch { setError(true); } finally { setLoading(false); } };
  useEffect(() => { void load(); }, [id]);
  if (loading) return <LoadingState label="Đang tải chi tiết phòng..." />;
  if (error || !room) return <ErrorState onRetry={load} />;
  return <section className="detail-section"><Link className="back-link" to="/guest/rooms">← Quay lại danh sách phòng</Link><div className="detail-grid"><RoomVisual roomName={room.room_name} imageUrls={room.image_urls} /><div className="detail-copy"><span className="eyebrow">ROOM {room.room_id}</span><div className="detail-title-row"><div><h1>{room.room_name}</h1><p className="room-type">{room.room_type_name} · Tầng {room.floor ?? "—"}</p></div><StatusBadge status={room.status} /></div><p className="detail-description">{room.description || room.room_type_description || "Không gian lưu trú được thiết kế cho một kỳ nghỉ thoải mái."}</p><div className="detail-price"><span>Giá phòng</span><strong>{new Intl.NumberFormat("vi-VN").format(room.daily_price)} đ <small>/ ngày</small></strong></div><div className="detail-info"><div><span>Tiện nghi</span><strong>{room.amenities.length ? room.amenities.join(" · ") : "Đang cập nhật"}</strong></div><div><span>Thông tin riêng tư</span><strong>Không công khai người đang lưu trú</strong></div></div></div></div></section>;
}

function GuestServicesPage() {
  const [services, setServices] = useState<PublicServiceSummary[]>([]); const [loading, setLoading] = useState(true); const [error, setError] = useState(false);
  const load = async () => { setLoading(true); setError(false); try { setServices(await getJson<PublicServiceSummary[]>("/api/public/services")); } catch { setError(true); } finally { setLoading(false); } };
  useEffect(() => { void load(); }, []);
  return <section className="catalog-section services-section"><div className="hero-copy compact"><span className="eyebrow">DỊCH VỤ KHÁCH SẠN</span><h1>Những điều nhỏ<br /><em>làm kỳ nghỉ trọn vẹn.</em></h1><p>Danh mục các dịch vụ đang hoạt động tại khách sạn. Giá niêm yết theo đơn vị sử dụng.</p></div><div className="section-heading"><div><span className="section-label">SERVICE CATALOG</span><h2>Dịch vụ đang phục vụ</h2></div><span className="catalog-count">{services.length} dịch vụ</span></div>{loading ? <LoadingState label="Đang tải danh mục dịch vụ..." /> : error ? <ErrorState onRetry={load} /> : services.length === 0 ? <EmptyState>Hiện chưa có dịch vụ đang phục vụ.</EmptyState> : <div className="service-list">{services.map((service, index) => <article className="service-row" key={service.service_id}><span className="service-index">{String(index + 1).padStart(2, "0")}</span><div><h3>{service.name}</h3><span>{service.unit}</span></div><strong>{new Intl.NumberFormat("vi-VN").format(service.price)} đ</strong></article>)}</div>}</section>;
}

function GuestLoginPage() {
  const { accessToken, login } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  if (accessToken) return <Navigate to={searchParams.get("next") || "/guest/rooms"} replace />;
  const submit = async (event: FormEvent) => {
    event.preventDefault(); setBusy(true); setError("");
    try {
      const tokens = await postJson<{ phone: string; password: string }, CustomerTokenResponse>("/api/auth/customers/login", { phone, password });
      login(tokens); navigate(searchParams.get("next") || "/guest/rooms");
    } catch (cause) { setError(cause instanceof Error ? cause.message : "Đăng nhập không thành công."); }
    finally { setBusy(false); }
  };
  return <section className="auth-section"><div className="auth-card"><span className="eyebrow">CUSTOMER ACCESS</span><h1>Đăng nhập để đặt phòng.</h1><p>Khách chưa đăng nhập vẫn có thể xem phòng và dịch vụ. Chỉ tài khoản đã xác thực mới được tạo booking.</p><form className="auth-form" onSubmit={submit}><label>Số điện thoại<input required value={phone} onChange={(event) => setPhone(event.target.value)} autoComplete="tel" /></label><label>Mật khẩu<input required type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="current-password" /></label>{error && <div className="form-error" role="alert">{error}</div>}<button className="button button-primary" disabled={busy}>{busy ? "Đang đăng nhập..." : "Đăng nhập"}</button></form><p className="auth-switch">Chưa có tài khoản? <Link className="text-link" to={`/guest/register?next=${encodeURIComponent(searchParams.get("next") || "/guest/rooms")}`}>Đăng ký khách hàng</Link></p></div></section>;
}

function GuestRegisterPage() {
  const { accessToken } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [form, setForm] = useState({ phone: "", password: "", full_name: "", identity_number: "" });
  const [busy, setBusy] = useState(false); const [error, setError] = useState("");
  if (accessToken) return <Navigate to="/guest/rooms" replace />;
  const submit = async (event: FormEvent) => { event.preventDefault(); setBusy(true); setError(""); try { const payload: CustomerRegisterRequest = form; await postJson<CustomerRegisterRequest, unknown>("/api/auth/customers/register", payload); navigate(`/guest/login?next=${encodeURIComponent(searchParams.get("next") || "/guest/rooms")}`); } catch (cause) { setError(cause instanceof Error ? cause.message : "Không thể tạo tài khoản."); } finally { setBusy(false); } };
  return <section className="auth-section"><div className="auth-card"><span className="eyebrow">CUSTOMER ACCESS</span><h1>Tạo tài khoản khách.</h1><p>Tài khoản giúp bạn đặt phòng và theo dõi mã thanh toán của booking thuộc chính mình.</p><form className="auth-form" onSubmit={submit}><label>Họ tên<input required value={form.full_name} onChange={(event) => setForm({ ...form, full_name: event.target.value })} /></label><label>Số điện thoại<input required value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} autoComplete="tel" /></label><label>Số giấy tờ<input required value={form.identity_number} onChange={(event) => setForm({ ...form, identity_number: event.target.value })} /></label><label>Mật khẩu<input required minLength={8} type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} autoComplete="new-password" /></label>{error && <div className="form-error" role="alert">{error}</div>}<button className="button button-primary" disabled={busy}>{busy ? "Đang tạo tài khoản..." : "Đăng ký"}</button></form><p className="auth-switch">Đã có tài khoản? <Link className="text-link" to={`/guest/login?next=${encodeURIComponent(searchParams.get("next") || "/guest/rooms")}`}>Đăng nhập</Link></p></div></section>;
}

function CustomerBookingPage() {
  const { accessToken } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [roomId, setRoomId] = useState(searchParams.get("room") || "");
  const [from, setFrom] = useState(searchParams.get("from") || "");
  const [to, setTo] = useState(searchParams.get("to") || "");
  const [rentalType, setRentalType] = useState<"PACKAGE" | "HOURLY">("PACKAGE");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  if (!accessToken) return <Navigate to={`/guest/login?next=${encodeURIComponent(`/guest/bookings/new?${searchParams.toString()}`)}`} replace />;
  const submit = async (event: FormEvent) => {
    event.preventDefault(); setBusy(true); setError("");
    try {
      const payload: CustomerBookingRequest = { rental_type: rentalType, rooms: [{ room_id: roomId, expected_check_in: `${from}:00`, expected_check_out: `${to}:00` }], idempotency_key: `web-${crypto.randomUUID()}` };
      const booking = await postJson<CustomerBookingRequest, CustomerBooking>("/api/customer/reservations", payload, accessToken);
      navigate(`/guest/bookings/${booking.id}/payment`);
    } catch (cause) { setError(cause instanceof Error ? cause.message : "Không thể tạo booking."); }
    finally { setBusy(false); }
  };
  return <section className="auth-section"><div className="auth-card booking-card"><span className="eyebrow">RESERVE A ROOM</span><h1>Xác nhận kỳ lưu trú.</h1><p>Hệ thống sẽ kiểm tra phòng, giữ lịch và phát hành mã thanh toán tiền cọc sau khi tạo booking.</p><form className="auth-form" onSubmit={submit}><label>Mã phòng<input required value={roomId} onChange={(event) => setRoomId(event.target.value)} /></label><label>Nhận phòng<input required type="datetime-local" value={from} onChange={(event) => setFrom(event.target.value)} /></label><label>Trả phòng<input required type="datetime-local" value={to} onChange={(event) => setTo(event.target.value)} /></label><label>Kiểu thuê<select value={rentalType} onChange={(event) => setRentalType(event.target.value as "PACKAGE" | "HOURLY")}><option value="PACKAGE">Theo gói ngày</option><option value="HOURLY">Theo giờ</option></select></label>{error && <div className="form-error" role="alert">{error}</div>}<button className="button button-primary" disabled={busy}>{busy ? "Đang tạo booking..." : "Tạo booking và nhận mã cọc"}</button></form></div></section>;
}

function CustomerBookingsPage() {
  const { accessToken } = useAuth();
  const [bookings, setBookings] = useState<CustomerBooking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const load = async () => { if (!accessToken) return; setLoading(true); setError(false); try { setBookings(await getJson<CustomerBooking[]>("/api/customer/reservations", accessToken)); } catch { setError(true); } finally { setLoading(false); } };
  useEffect(() => { void load(); }, [accessToken]);
  if (!accessToken) return <Navigate to="/guest/login?next=%2Fguest%2Fbookings" replace />;
  return <section className="catalog-section booking-list-section"><div className="hero-copy compact"><span className="eyebrow">MY BOOKINGS</span><h1>Lịch đặt phòng<br /><em>của bạn.</em></h1><p>Chỉ booking thuộc tài khoản hiện tại được hiển thị.</p></div>{loading ? <LoadingState label="Đang tải booking..." /> : error ? <ErrorState onRetry={load} /> : bookings.length === 0 ? <EmptyState>Bạn chưa có booking nào.</EmptyState> : <div className="booking-list">{bookings.map((booking) => <article className="booking-row" key={booking.id}><div><span className="room-kicker">BOOKING #{booking.id}</span><h3>{booking.rooms.map((room) => room.room_id).join(", ")}</h3><p>{booking.rooms[0]?.expected_check_in.replace("T", " ")} → {booking.rooms[0]?.expected_check_out.replace("T", " ")}</p></div><div><StatusBadge status={booking.deposit_payment.status === "PAID" ? "READY" : "RESERVED"} /><Link className="text-link" to={`/guest/bookings/${booking.id}/payment`}>Xem mã cọc →</Link></div></article>)}</div>}</section>;
}

function BookingPaymentPage() {
  const { accessToken } = useAuth();
  const { id } = useParams();
  const [booking, setBooking] = useState<CustomerBooking | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const load = async () => { if (!accessToken || !id) return; setLoading(true); setError(false); try { setBooking(await getJson<CustomerBooking>(`/api/customer/reservations/${encodeURIComponent(id)}`, accessToken)); } catch { setError(true); } finally { setLoading(false); } };
  useEffect(() => { void load(); }, [accessToken, id]);
  if (!accessToken) return <Navigate to={`/guest/login?next=${encodeURIComponent(`/guest/bookings/${id}/payment`)}`} replace />;
  if (loading) return <LoadingState label="Đang tải hướng dẫn thanh toán..." />;
  if (error || !booking) return <ErrorState onRetry={load} />;
  const payment = booking.deposit_payment;
  return <section className="payment-section"><Link className="back-link" to="/guest/bookings">← Booking của tôi</Link><div className="payment-card"><span className="eyebrow">DEPOSIT INSTRUCTION</span><h1>Mã thanh toán tiền cọc</h1><p>Booking #{booking.id} đang ở trạng thái <strong>{booking.status}</strong>. Mã này chỉ là hướng dẫn thanh toán; trạng thái chỉ đổi sau khi hệ thống xác minh giao dịch.</p><div className="payment-code">{payment.payment_code}</div><div className="payment-meta"><span>Số tiền cọc<strong>{new Intl.NumberFormat("vi-VN").format(payment.amount)} đ</strong></span><span>Trạng thái<strong>{payment.status}</strong></span></div><div className="payment-instruction">{payment.instruction}</div></div></section>;
}

function StaffPlaceholder() {
  return <section className="state-card staff-placeholder"><span className="eyebrow">STAFF WORKSPACE</span><h1>Giao diện vận hành sẽ được nối ở phase tiếp theo.</h1><p>Public portal đã tách khỏi vùng nhân viên. Dashboard theo permission sẽ dùng AppShell riêng.</p><Link className="button button-secondary" to="/guest/rooms">Về public portal</Link></section>;
}

export function App() {
  return <BrowserRouter><AuthProvider><Routes><Route element={<PublicLayout />}><Route path="/guest/rooms" element={<GuestRoomsPage />} /><Route path="/guest/rooms/:id" element={<GuestRoomDetailPage />} /><Route path="/guest/services" element={<GuestServicesPage />} /><Route path="/guest/login" element={<GuestLoginPage />} /><Route path="/guest/register" element={<GuestRegisterPage />} /><Route path="/guest/bookings" element={<CustomerBookingsPage />} /><Route path="/guest/bookings/new" element={<CustomerBookingPage />} /><Route path="/guest/bookings/:id/payment" element={<BookingPaymentPage />} /><Route path="/staff/*" element={<StaffPlaceholder />} /><Route path="/" element={<Navigate to="/guest/rooms" replace />} /><Route path="*" element={<Navigate to="/guest/rooms" replace />} /></Route></Routes></AuthProvider></BrowserRouter>;
}
