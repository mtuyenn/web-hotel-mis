import { useState } from "react";
import { CheckCircle2, X, Zap } from "lucide-react";
import { formatVnd } from "../../shared/utils/money";
import type { PriceRequest, PriceStatus } from "./WarehouseServices";

export function PriceProposalResultModal({
  request,
  onClose,
  onActivate,
}: {
  request: PriceRequest;
  onClose: () => void;
  onActivate?: (req: PriceRequest) => Promise<void>;
}) {
  const [activating, setActivating] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const delta = request.newPrice - request.currentPrice;
  const deltaPct = ((delta / request.currentPrice) * 100).toFixed(1);
  const up = delta > 0;

  const STAT_MAP: Record<PriceStatus, { label: string; bg: string; text: string; desc: string }> = {
    pending: { label: "Chờ duyệt", bg: "#FEF9C3", text: "#92400E", desc: "Đề xuất đã gửi tới Quản lý và đang chờ xem xét phê duyệt." },
    approved: { label: "Đã duyệt", bg: "#DCFCE7", text: "#166534", desc: "Quản lý đã phê duyệt đề xuất. Có thể kích hoạt giá ngay để áp dụng vào hệ thống." },
    rejected: { label: "Từ chối", bg: "#FFE4E6", text: "#BE123C", desc: "Đề xuất đã bị Quản lý từ chối." },
    expired: { label: "Hết hạn", bg: "#F1F5F9", text: "#475569", desc: "Đề xuất đã quá thời hạn hiệu lực." },
    consumed: { label: "Đã áp dụng", bg: "#DBEAFE", text: "#1D4ED8", desc: "Giá mới đã được kích hoạt thành công và đang áp dụng thực tế trên toàn hệ thống." },
  };

  const currentStat = STAT_MAP[request.status];

  const handleActivateClick = async () => {
    if (!onActivate) return;
    setActivating(true);
    setErrorMsg(null);
    setSuccessMsg(null);
    try {
      await onActivate(request);
      setSuccessMsg("Kích hoạt giá thành công! Dịch vụ đã cập nhật mức giá mới.");
    } catch (err) {
      const message = err instanceof Error ? err.message : "Không thể kích hoạt giá.";
      if (message.includes("52206") || message.toLowerCase().includes("requester")) {
        setErrorMsg("Theo quy chuẩn kiểm soát nội bộ (RBAC), nhân viên lập đề xuất không thể tự kích hoạt. Vui lòng nhờ Quản lý (người duyệt) bấm 'Áp dụng giá ngay' tại Màn hình Quản lý.");
      } else {
        setErrorMsg(message);
      }
    } finally {
      setActivating(false);
    }
  };

  return (
    <div style={{
      position: "fixed", inset: 0, zIndex: 120,
      background: "rgba(15,23,42,.55)", backdropFilter: "blur(4px)",
      display: "flex", alignItems: "center", justifyContent: "center", padding: 16,
    }}>
      <div style={{
        background: "#FFF", borderRadius: 16, width: "100%", maxWidth: 520,
        boxShadow: "0 20px 40px rgba(0,0,0,.15)", overflow: "hidden",
      }}>
        {/* Header */}
        <div style={{
          padding: "16px 20px", borderBottom: "1px solid #E2E8F0",
          display: "flex", alignItems: "center", justifyContent: "space-between",
          background: "#F8FAFC",
        }}>
          <div>
            <h3 style={{ fontSize: 16, fontWeight: 700, color: "#0F172A", margin: 0 }}>
              Tiến trình &amp; Kết quả đề xuất
            </h3>
            <p style={{ fontSize: 12, color: "#64748B", margin: "2px 0 0" }}>
              Mã đề xuất: #{request.id} · Dịch vụ: {request.sku}
            </p>
          </div>
          <button
            onClick={onClose}
            style={{ background: "none", border: "none", cursor: "pointer", color: "#64748B", padding: 4 }}
          >
            <X size={18} />
          </button>
        </div>

        {/* Content */}
        <div style={{ padding: "20px 24px" }}>
          {/* Status banner */}
          <div style={{
            padding: "12px 16px", borderRadius: 10, background: currentStat.bg,
            border: `1px solid ${currentStat.text}20`, marginBottom: 18,
            display: "flex", alignItems: "flex-start", gap: 12,
          }}>
            <div style={{
              padding: "4px 10px", borderRadius: 99, background: "#FFF",
              fontSize: 12, fontWeight: 700, color: currentStat.text, flexShrink: 0,
            }}>
              {currentStat.label}
            </div>
            <p style={{ fontSize: 12, color: currentStat.text, margin: 0, lineHeight: 1.5 }}>
              {request.catalogAddition && request.status==="consumed" ? "Quản lý đã duyệt và tạo danh mục mới thành công." : currentStat.desc}
            </p>
          </div>

          {/* Service & Price Details */}
          <div style={{ background: "#F8FAFC", borderRadius: 12, border: "1px solid #E2E8F0", padding: "14px 16px", marginBottom: 16 }}>
            <p style={{ fontSize: 11, fontWeight: 700, color: "#64748B", textTransform: "uppercase", letterSpacing: "0.05em", marginBottom: 8 }}>
              Thông tin danh mục &amp; Mức giá
            </p>
            <p style={{ fontSize: 15, fontWeight: 700, color: "#0F172A", marginBottom: 12 }}>
              {request.item} <span style={{ fontSize: 12, fontWeight: 500, color: "#64748B", fontFamily: "'JetBrains Mono',monospace" }}>({request.sku})</span>
            </p>

            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 10 }}>
              <div style={{ background: "#FFF", padding: "8px 12px", borderRadius: 8, border: "1px solid #E2E8F0" }}>
                <p style={{ fontSize: 10, color: "#64748B", marginBottom: 3 }}>Giá hiện tại</p>
                <p style={{ fontSize: 13, fontWeight: 700, color: "#475569" }}>{formatVnd(request.currentPrice)}</p>
              </div>
              <div style={{ background: "#FFF", padding: "8px 12px", borderRadius: 8, border: "1px solid #E2E8F0" }}>
                <p style={{ fontSize: 10, color: "#64748B", marginBottom: 3 }}>Giá đề xuất</p>
                <p style={{ fontSize: 13, fontWeight: 800, color: "#0F172A" }}>{formatVnd(request.newPrice)}</p>
              </div>
              <div style={{ background: "#FFF", padding: "8px 12px", borderRadius: 8, border: "1px solid #E2E8F0" }}>
                <p style={{ fontSize: 10, color: "#64748B", marginBottom: 3 }}>Thay đổi</p>
                <p style={{ fontSize: 13, fontWeight: 700, color: up ? "#16A34A" : "#DC2626" }}>
                  {request.catalogAddition?"Thêm mới":request.currentPrice===0?formatVnd(delta):`${up?"+":""}${deltaPct}%`}
                </p>
              </div>
            </div>
          </div>

          {/* Audit trail / Details */}
          <div style={{ display: "flex", flexDirection: "column", gap: 8, fontSize: 12, color: "#334155", marginBottom: 18 }}>
            <div style={{ display: "flex", justifyContent: "space-between" }}>
              <span style={{ color: "#64748B" }}>Người đề xuất:</span>
              <strong>{request.requestedBy}</strong>
            </div>
            <div style={{ display: "flex", justifyContent: "space-between" }}>
              <span style={{ color: "#64748B" }}>Thời gian gửi:</span>
              <span>{request.requestedAt}</span>
            </div>
            <div style={{ display: "flex", justifyContent: "space-between" }}>
              <span style={{ color: "#64748B" }}>Lý do đề xuất:</span>
              <span style={{ fontWeight: 500 }}>{request.reason || "—"}</span>
            </div>
            {request.rawApproval?.approver && (
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <span style={{ color: "#64748B" }}>Người phê duyệt:</span>
                <strong style={{ color: "#166534" }}>{request.rawApproval.approver}</strong>
              </div>
            )}
            {request.rawApproval?.decided_at && (
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <span style={{ color: "#64748B" }}>Thời điểm duyệt:</span>
                <span>{new Date(request.rawApproval.decided_at).toLocaleString("vi-VN")}</span>
              </div>
            )}
          </div>

          {/* Notifications */}
          {errorMsg && (
            <div role="alert" style={{
              padding: "10px 14px", borderRadius: 8, background: "#FFF1F2",
              border: "1px solid #FECDD3", color: "#BE123C", fontSize: 12, marginBottom: 16, lineHeight: 1.5,
            }}>
              {errorMsg}
            </div>
          )}
          {successMsg && (
            <div role="status" style={{
              padding: "10px 14px", borderRadius: 8, background: "#F0FDF4",
              border: "1px solid #BBF7D0", color: "#166534", fontSize: 12, marginBottom: 16,
              display: "flex", alignItems: "center", gap: 8,
            }}>
              <CheckCircle2 size={16} />
              <span>{successMsg}</span>
            </div>
          )}

          {/* Actions */}
          <div style={{ display: "flex", justifyContent: "flex-end", gap: 10 }}>
            <button
              onClick={onClose}
              style={{
                height: 36, padding: "0 16px", borderRadius: 8, border: "1px solid #E2E8F0",
                background: "#FFF", color: "#475569", fontSize: 12, fontWeight: 600, cursor: "pointer",
              }}
            >
              Đóng
            </button>
            {!request.catalogAddition && request.status === "approved" && onActivate && (
              <button
                onClick={handleActivateClick}
                disabled={activating || Boolean(successMsg)}
                style={{
                  height: 36, padding: "0 16px", borderRadius: 8, border: "none",
                  background: activating ? "#64748B" : "#16A34A", color: "#FFF",
                  fontSize: 12, fontWeight: 700, cursor: activating ? "wait" : "pointer",
                  display: "flex", alignItems: "center", gap: 6,
                }}
              >
                <Zap size={14} />
                {activating ? "Đang kích hoạt…" : "Kích hoạt giá ngay"}
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

