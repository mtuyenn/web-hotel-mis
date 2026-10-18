import { useEffect, useRef, useState, useMemo } from "react";
import { apiErrorMessage } from "../../shared/api/client";
import StockRequestDialog from "../stock-requests/StockRequestDialog";
import { AlertTriangle, Plus, Send } from "lucide-react";
import type { LinenRow } from "./HousekeepingStation";

export function LinenScreen({ stock = [], onMove }: { stock?: LinenRow[]; onMove?: (itemId: string, movementType: "RECEIVE" | "ISSUE" | "RETURN", quantity: number, reason: string) => Promise<void> }) {
  const [category, setCategory] = useState("all");
  const [movement, setMovement] = useState<{ item: LinenRow; type: "RECEIVE" | "ISSUE" } | null>(null);
  const [movementQuantity, setMovementQuantity] = useState("1");
  const [movementReason, setMovementReason] = useState("");
  const [movementBusy, setMovementBusy] = useState(false);
  const [movementError, setMovementError] = useState<string | null>(null);
  const [movementSuccess, setMovementSuccess] = useState<string | null>(null);
  const movementSuccessTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => () => {
    if (movementSuccessTimeoutRef.current !== null) clearTimeout(movementSuccessTimeoutRef.current);
  }, []);

  // Exclude minibar from categories as requested
  const cats = ["all", "linen", "towels", "amenities"];
  const catLabels: Record<string, string> = {
    all: "Tất cả",
    linen: "Đồ vải",
    towels: "Khăn",
    amenities: "Đồ dùng phòng",
  };

  // Filter out any minibar items from Linen tab so they are only managed in Minibar tab
  const nonMinibarStock = useMemo(() => {
    return stock.filter(s =>
      !s.category.toLowerCase().includes("minibar") &&
      !s.id.toLowerCase().includes("minibar") &&
      !s.item.toLowerCase().includes("minibar")
    );
  }, [stock]);

  const filtered = category === "all" ? nonMinibarStock : nonMinibarStock.filter(s => s.category.toLowerCase() === category.toLowerCase());

  const stockPct = (item: LinenRow) => item.min > 0
    ? Math.min(100, Math.round((item.stock / item.min) * 100))
    : item.stock > 0 ? 100 : 0;

  const isLowStock = (item: LinenRow) => {
    if (item.min <= 0) return false;
    return item.stock < item.min;
  };

  const stockStatus = (item: LinenRow) => {
    if (item.stock === 0) return { color: "#DC2626", label: "Hết", bg: "#FFE4E6" };
    if (isLowStock(item)) return { color: "#E11D48", label: "Dưới ngưỡng an toàn", bg: "#FFE4E6" };
    if (item.stock <= item.min) return { color: "#D97706", label: "Sắp hết", bg: "#FEF3C7" };
    return { color: "#16A34A", label: "Đạt", bg: "#DCFCE7" };
  };

  const criticalLowStock = nonMinibarStock.filter(isLowStock);
  const lowStock = nonMinibarStock.filter(s => s.stock <= s.min && !isLowStock(s));

  const [requestItem,setRequestItem]=useState<LinenRow|null>(null);
  const [requestKind,setRequestKind]=useState<"ISSUE"|"DAMAGE">("ISSUE");
  const [warehouseNotice, setWarehouseNotice] = useState<string | null>(null);

  const handleNotifyWarehouse=(item:LinenRow)=>{setRequestKind("ISSUE");setRequestItem(item);};
  const handleNotifyWarehouseAll=()=>{if(criticalLowStock[0])setRequestItem(criticalLowStock[0]);};

  const openMovement = (item: LinenRow, type: "RECEIVE" | "ISSUE") => {
    setMovement({ item, type });
    setMovementQuantity("1");
    setMovementReason("");
    setMovementError(null);
    setMovementSuccess(null);
  };

  const submitMovement = async () => {
    if (!movement || !onMove) return;
    const quantity = Number(movementQuantity);
    if (!Number.isInteger(quantity) || quantity <= 0) {
      setMovementError("Số lượng phải là số nguyên lớn hơn 0.");
      return;
    }
    if(!movementReason.trim()){setMovementError("Nhập lý do yêu cầu.");return;}
    setMovementBusy(true);
    setMovementError(null);
    setMovementSuccess(null);
    try {
      await onMove(movement.item.id, movement.type==="RECEIVE"?"ISSUE":"RETURN", quantity, movementReason.trim() || (movement.type === "RECEIVE" ? "Yêu cầu cấp hàng" : "Trả hàng về kho"));
      setMovementSuccess(`Yêu cầu ${movement.type === "RECEIVE" ? "cấp" : "trả"} ${quantity} ${movement.item.unit} ${movement.item.item}; đã gửi Kho & Dịch vụ xử lý.`);
      if (movementSuccessTimeoutRef.current !== null) clearTimeout(movementSuccessTimeoutRef.current);
      movementSuccessTimeoutRef.current = setTimeout(() => {
        movementSuccessTimeoutRef.current = null;
        setMovement(null);
        setMovementSuccess(null);
      }, 1500);
    } catch (error) {
      setMovementError(apiErrorMessage(error, "Không thể cập nhật tồn kho. Vui lòng thử lại."));
    } finally {
      setMovementBusy(false);
    }
  };

  return (
    <div style={{ flex: 1, overflowY: "auto", background: "#F8FAFC" }}>{requestItem&&<StockRequestDialog item={requestItem} kind={requestKind} onClose={()=>setRequestItem(null)} onSent={()=>setWarehouseNotice("Đã gửi yêu cầu tới Kho & Dịch vụ.")}/>}
      <div style={{ padding: "20px 24px" }}>
        <div style={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between", marginBottom: 16 }}>
          <div>
            <h2 style={{ fontSize: 18, fontWeight: 700, color: "#0F172A", marginBottom: 2 }}>Kho đồ vải &amp; Vật tư</h2>
            <p style={{ fontSize: 12, color: "#94A3B8" }}>Tồn riêng của Buồng phòng: đồ vải, khăn, đồ dùng phòng. Hàng được cộng sau khi Kho duyệt cấp.</p>
          </div>
          <button
            onClick={() => nonMinibarStock[0] && openMovement(nonMinibarStock[0], "RECEIVE")}
            disabled={!nonMinibarStock.length}
            style={{
              padding: "8px 16px",
              borderRadius: 9,
              background: "#0F172A",
              color: "#FFF",
              fontSize: 12,
              fontWeight: 700,
              cursor: "pointer",
              display: "flex",
              alignItems: "center",
              gap: 6,
              opacity: !nonMinibarStock.length ? 0.5 : 1,
            }}
          >
            <Plus size={14} /> Yêu cầu cấp hàng
          </button>
        </div>

        {/* Notice from warehouse */}
        {warehouseNotice && (
          <div role="status" style={{ padding: "10px 14px", borderRadius: 8, background: "#DCFCE7", border: "1px solid #86EFAC", color: "#166534", fontSize: 12, fontWeight: 600, marginBottom: 14, display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <span>✓ {warehouseNotice}</span>
            <button onClick={() => setWarehouseNotice(null)} style={{ background: "transparent", border: "none", color: "#166534", cursor: "pointer", fontSize: 12 }}>✕</button>
          </div>
        )}

        {/* Low stock alerts */}
        {criticalLowStock.length > 0 && (
          <div style={{ padding: "12px 16px", borderRadius: 10, border: "1px solid #FECDD3", background: "#FFF1F2", marginBottom: 14, display: "flex", alignItems: "center", justifyContent: "space-between", flexWrap: "wrap", gap: 10 }}>
            <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
              <AlertTriangle size={18} style={{ color: "#E11D48", flexShrink: 0 }} />
              <div>
                <strong style={{ color: "#9F1239", fontSize: 12 }}>Cảnh báo vật dụng dưới ngưỡng an toàn:</strong>
                <p style={{ fontSize: 11, color: "#BE123C", margin: "2px 0 0" }}>
                  {criticalLowStock.map(s => `${s.item} (còn ${s.stock} ${s.unit} / tối thiểu ${s.min})`).join(", ")}
                </p>
              </div>
            </div>
            <button
              onClick={handleNotifyWarehouseAll}
              style={{
                padding: "6px 14px",
                borderRadius: 8,
                background: "#E11D48",
                color: "#FFF",
                fontSize: 11,
                fontWeight: 700,
                border: "none",
                cursor: "pointer",
                display: "flex",
                alignItems: "center",
                gap: 6,
                boxShadow: "0 2px 4px rgba(225,29,72,0.2)",
              }}
            >
              <Send size={12} /> Báo kho cấp hàng khẩn cấp
            </button>
          </div>
        )}

        {/* Normal low stock alerts */}
        {lowStock.length > 0 && (
          <div style={{ padding: "10px 14px", borderRadius: 10, border: "1px solid #FED7AA", background: "#FFFBEB", marginBottom: 16, display: "flex", alignItems: "center", gap: 10 }}>
            <AlertTriangle size={16} style={{ color: "#D97706", flexShrink: 0 }} />
            <p style={{ fontSize: 12, color: "#92400E", margin: 0 }}>
              <strong>Tồn kho sắp hết:</strong> {lowStock.map(s => `${s.item} (${s.stock} ${s.unit})`).join(", ")}
            </p>
          </div>
        )}

        {/* Category tabs */}
        <div style={{ display: "flex", gap: 6, marginBottom: 16 }}>
          {cats.map(c => {
            const active = category === c;
            return (
              <button
                key={c}
                onClick={() => setCategory(c)}
                style={{
                  padding: "6px 14px",
                  borderRadius: 99,
                  cursor: "pointer",
                  fontSize: 12,
                  fontWeight: active ? 600 : 400,
                  background: active ? "#0F172A" : "#FFF",
                  color: active ? "#FFF" : "#475569",
                  border: `1px solid ${active ? "#0F172A" : "#E2E8F0"}`,
                }}
              >
                {catLabels[c]}
              </button>
            );
          })}
        </div>

        {movement && (
          <div style={{ background: "#FFF", border: "1px solid #BFDBFE", borderRadius: 12, padding: "14px 16px", marginBottom: 16 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 10 }}>
              <div>
                <p style={{ fontSize: 12, fontWeight: 700, color: "#0F172A", margin: 0 }}>
                  {movement.type === "RECEIVE" ? "Yêu cầu cấp" : "Trả kho"}: {movement.item.item}
                </p>
                <p style={{ fontSize: 10, color: "#64748B", marginTop: 2, marginBottom: 0 }}>
                  Tồn hiện tại: {movement.item.stock} {movement.item.unit}
                </p>
              </div>
              <button onClick={() => setMovement(null)} style={{ fontSize: 12, color: "#64748B", cursor: "pointer", background: "transparent", border: "none" }}>
                Đóng
              </button>
            </div>
            <div style={{ display: "grid", gridTemplateColumns: "180px 1fr auto", gap: 8, alignItems: "end", marginTop: 10 }}>
              <label style={{ fontSize: 10, color: "#64748B" }}>
                Số lượng
                <input
                  type="number"
                  min="1"
                  step="1"
                  value={movementQuantity}
                  onChange={event => setMovementQuantity(event.target.value)}
                  style={{ display: "block", width: "100%", height: 34, marginTop: 3, border: "1px solid #E2E8F0", borderRadius: 7, padding: "0 8px", boxSizing: "border-box" }}
                />
              </label>
              <label style={{ fontSize: 10, color: "#64748B" }}>
                Lý do
                <input
                  value={movementReason}
                  onChange={event => setMovementReason(event.target.value)}
                  placeholder="Ví dụ: Nhập bổ sung đồ vải mới"
                  style={{ display: "block", width: "100%", height: 34, marginTop: 3, border: "1px solid #E2E8F0", borderRadius: 7, padding: "0 8px", boxSizing: "border-box" }}
                />
              </label>
              <button
                onClick={() => void submitMovement()}
                disabled={movementBusy}
                style={{ height: 34, padding: "0 14px", borderRadius: 7, background: "#2563EB", color: "#FFF", fontSize: 11, fontWeight: 700, cursor: "pointer", opacity: movementBusy ? 0.6 : 1 }}
              >
                {movementBusy ? "Đang lưu…" : "Gửi yêu cầu"}
              </button>
            </div>
            {movementError && <p role="alert" style={{ fontSize: 11, color: "#BE123C", marginTop: 8, marginBottom: 0 }}>{movementError}</p>}
            {movementSuccess && <p style={{ fontSize: 11, color: "#166534", marginTop: 8, marginBottom: 0 }}>{movementSuccess}</p>}
          </div>
        )}

        {/* Stock grid */}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill,minmax(270px,1fr))", gap: 12 }}>
          {filtered.length === 0 && (
            <div style={{ gridColumn: "1 / -1", padding: "28px 16px", textAlign: "center", background: "#FFF", border: "1px dashed #CBD5E1", borderRadius: 12, color: "#94A3B8", fontSize: 12 }}>
              Chưa có dữ liệu tồn kho đồ vải/vật tư buồng phòng.
            </div>
          )}
          {filtered.map(item => {
            const pct = stockPct(item);
            const stat = stockStatus(item);
            const belowThreshold = isLowStock(item);
            return (
              <div
                key={item.id}
                style={{
                  background: "#FFF",
                  borderRadius: 12,
                  border: `1px solid ${belowThreshold ? "#FECDD3" : item.stock <= item.min ? "#FED7AA" : "#E2E8F0"}`,
                  padding: "14px 16px",
                  boxShadow: belowThreshold ? "0 2px 8px rgba(225,29,72,0.06)" : "none",
                }}
              >
                <div style={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between", marginBottom: 10 }}>
                  <div>
                    <p style={{ fontSize: 13, fontWeight: 700, color: "#0F172A", marginBottom: 2 }}>{item.item}</p>
                    <p style={{ fontSize: 11, color: "#94A3B8", margin: 0 }}>Tối thiểu: {item.min} {item.unit}</p>
                  </div>
                  <span style={{ fontSize: 10, fontWeight: 700, padding: "2px 8px", borderRadius: 99, background: stat.bg, color: stat.color }}>
                    {stat.label}
                  </span>
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 8 }}>
                  <span style={{ fontFamily: "'JetBrains Mono',monospace", fontSize: 22, fontWeight: 800, color: stat.color, lineHeight: 1 }}>
                    {item.stock}
                  </span>
                  <span style={{ fontSize: 12, color: "#94A3B8" }}>{item.unit} trong kho</span>
                  {belowThreshold && (
                    <span style={{ marginLeft: "auto", fontSize: 10, fontWeight: 700, padding: "2px 6px", borderRadius: 4, background: "#FFE4E6", color: "#E11D48" }}>
                      Dưới ngưỡng
                    </span>
                  )}
                </div>
                <div style={{ height: 6, background: "#E2E8F0", borderRadius: 99, overflow: "hidden", marginBottom: 10 }}>
                  <div style={{ height: "100%", background: stat.color, width: `${pct}%`, borderRadius: 99, transition: "width .3s" }} />
                </div>
                <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>
                  <button
                    onClick={() => openMovement(item, "ISSUE")}
                    style={{ flex: 1, minWidth: 60, padding: "6px", borderRadius: 7, border: "1px solid #E2E8F0", background: "#F8FAFC", color: "#475569", fontSize: 11, cursor: "pointer" }}
                  >
                    Trả kho
                  </button>
                  <button
                    onClick={() => openMovement(item, "RECEIVE")}
                    style={{ flex: 1, minWidth: 60, padding: "6px", borderRadius: 7, border: "1px solid #3B82F6", background: "#EFF6FF", color: "#1D4ED8", fontSize: 11, fontWeight: 600, cursor: "pointer" }}
                  >
                    Yêu cầu cấp
                  </button>
                  <button onClick={()=>{setRequestKind("DAMAGE");setRequestItem(item);}} style={{padding:6,border:"1px solid #E2E8F0",borderRadius:7,fontSize:11}}>Báo hỏng</button>
                  {belowThreshold && (
                    <button
                      onClick={() => handleNotifyWarehouse(item)}
                      style={{
                        width: "100%",
                        padding: "6px 8px",
                        borderRadius: 7,
                        border: "1px solid #FECDD3",
                        background: "#FFF1F2",
                        color: "#BE123C",
                        fontSize: 11,
                        fontWeight: 600,
                        cursor: "pointer",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        gap: 4,
                        marginTop: 2,
                      }}
                    >
                      <Send size={11} />
                      Báo kho cấp hàng
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

