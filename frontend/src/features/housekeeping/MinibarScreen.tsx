import { AlertTriangle, Archive, CheckCircle2, Clock, Plus, Search, Send, Sparkles, Wine, X } from "lucide-react";
import { useEffect, useMemo, useRef, useState } from "react";
import { apiErrorMessage } from "../../shared/api/client";
import { stockRequestsApi } from "../../shared/api/stockRequests";
import type { EmployeeProfileDto } from "../../shared/types/api";
import StockRequestDialog from "../stock-requests/StockRequestDialog";
import type { HKRoom, LinenRow } from "./types";

interface ReturnRequest {
  id: string;
  itemId: string;
  itemName: string;
  quantity: number;
  unit: string;
  reason: string;
  requestedBy: string;
  requestedAt: string;
  status: "CHO_KHO_DUYET" | "DA_DUYET" | "TU_CHOI";
  approvedAt?: string;
}

export function MinibarScreen({
  stock,
  onMove,
  currentUser,
}: {
  stock: LinenRow[];
  rooms?: HKRoom[];
  onMove: (itemId: string, movementType: "RECEIVE" | "ISSUE" | "RETURN", quantity: number, reason: string) => Promise<void>;
  currentUser: EmployeeProfileDto | null;
}) {
  const [returnModalOpen, setReturnModalOpen] = useState(false);
  const [returnItemId, setReturnItemId] = useState("");
  const [returnQuantity, setReturnQuantity] = useState("1");
  const [returnReasonType, setReturnReasonType] = useState("Thừa ca trực, hoàn trả kho");
  const [returnCustomReason, setReturnCustomReason] = useState("");
  const [returnBusy, setReturnBusy] = useState(false);
  const returnSubmission=useRef<{body:string;key:string}|null>(null);
  const [returnError, setReturnError] = useState<string | null>(null);
  const [approveBusyId, setApproveBusyId] = useState<string | null>(null);

  const [activeTab, setActiveTab] = useState<"items" | "returns">("items");
  const [search, setSearch] = useState("");

  const [warehouseNotice, setWarehouseNotice] = useState<string | null>(null);

  const [returnRequests,setReturnRequests]=useState<ReturnRequest[]>([]);
  const [requestItem,setRequestItem]=useState<LinenRow|null>(null);
  const [requestKind,setRequestKind]=useState<"ISSUE"|"DAMAGE">("ISSUE");
  const reloadReturns=async()=>{
    const rows=await stockRequestsApi.list();
    setReturnRequests(rows.filter(row=>row.kind==="RETURN").map(row=>({
      id:String(row.id),itemId:row.service_id,itemName:row.service_name,quantity:row.quantity,unit:row.unit,
      reason:row.reason,requestedBy:row.requester,requestedAt:new Date(row.requested_at).toLocaleString("vi-VN"),
      status:row.status==="PENDING"?"CHO_KHO_DUYET":row.status==="APPROVED"?"DA_DUYET":"TU_CHOI",
      approvedAt:row.decided_at?new Date(row.decided_at).toLocaleString("vi-VN"):undefined,
    })));
  };
  useEffect(()=>{void reloadReturns().catch(error=>setWarehouseNotice(apiErrorMessage(error,"Không tải được phiếu trả hàng.")));},[]);

  const minibarItems = useMemo(() => {
    const list = stock.filter(s =>
      s.category.toLowerCase().includes("minibar") ||
      s.id.toLowerCase().includes("minibar") ||
      s.id.toLowerCase().includes("water") ||
      s.item.toLowerCase().includes("minibar") ||
      s.item.toLowerCase().includes("nước suối") ||
      s.item.toLowerCase().includes("coca") ||
      s.item.toLowerCase().includes("bia") ||
      s.item.toLowerCase().includes("snack")
    );
    return list;
  }, [stock]);

  const isLowStock = (item: LinenRow) => {
    if (item.min <= 0) return false;
    return item.stock < item.min;
  };

  const criticalMinibarLow = useMemo(() => {
    return minibarItems.filter(isLowStock);
  }, [minibarItems]);

  const filteredItems = useMemo(() => {
    if (!search.trim()) return minibarItems;
    const q = search.toLowerCase();
    return minibarItems.filter(i => i.item.toLowerCase().includes(q) || i.id.toLowerCase().includes(q));
  }, [minibarItems, search]);

  const totalStock = minibarItems.reduce((sum, i) => sum + i.stock, 0);
  const pendingReturnsCount = returnRequests.filter(r => r.status === "CHO_KHO_DUYET").length;

  const stockPct = (item: LinenRow) =>
    item.min > 0 ? Math.min(100, Math.round((item.stock / item.min) * 100)) : item.stock > 0 ? 100 : 0;

  const stockStatus = (item: LinenRow) => {
    if (item.stock === 0) return { color: "#DC2626", label: "Hết hàng", bg: "#FFE4E6" };
    if (isLowStock(item)) return { color: "#E11D48", label: "Dưới ngưỡng an toàn", bg: "#FFE4E6" };
    if (item.stock <= item.min) return { color: "#D97706", label: "Sắp hết", bg: "#FEF3C7" };
    return { color: "#16A34A", label: "Đủ tồn", bg: "#DCFCE7" };
  };

  const handleNotifyWarehouse=(item:LinenRow)=>{setRequestKind("ISSUE");setRequestItem(item);};
  const handleNotifyWarehouseAll=()=>{if(criticalMinibarLow[0])setRequestItem(criticalMinibarLow[0]);};

  const openReturnModal = (item?: LinenRow) => {
    setReturnItemId(item?.id ?? minibarItems[0]?.id ?? "");
    setReturnQuantity("1");
    setReturnReasonType("Thừa ca trực, hoàn trả kho");
    setReturnCustomReason("");
    setReturnError(null);
    setReturnModalOpen(true);
  };

  const handleSubmitReturn = async () => {
    const item = stock.find(s => s.id === returnItemId) || minibarItems.find(s => s.id === returnItemId);
    if (!item) {
      setReturnError("Vui lòng chọn mặt hàng cần trả.");
      return;
    }
    const num = Number(returnQuantity);
    if (!Number.isInteger(num) || num <= 0) {
      setReturnError("Số lượng hoàn trả phải là số nguyên lớn hơn 0.");
      return;
    }

    const finalReason = returnReasonType === "Khác"
      ? (returnCustomReason.trim() || "Hoàn trả hàng về kho")
      : returnReasonType;

    setReturnBusy(true);
    setReturnError(null);
    try {
      const body={kind:"RETURN" as const,service_id:item.id,quantity:num,room_id:null,reason:finalReason};
      const serialized=JSON.stringify(body);if(returnSubmission.current?.body!==serialized)returnSubmission.current={body:serialized,key:crypto.randomUUID()};
      const created=await stockRequestsApi.create(body,returnSubmission.current.key);
      await reloadReturns();returnSubmission.current=null;
      setWarehouseNotice(`Đã tạo phiếu trả hàng [${created.id}] (${num} ${item.unit} "${item.item}"). Phiếu đang ở trạng thái Chờ kho duyệt!`);
      setReturnModalOpen(false);
      setActiveTab("returns");
    } catch(error) {
      setReturnError(apiErrorMessage(error,"Không thể gửi yêu cầu trả kho."));
    } finally {
      setReturnBusy(false);
    }
  };

  return (
    <div style={{ flex: 1, overflowY: "auto", background: "#F8FAFC" }}>{requestItem&&<StockRequestDialog item={requestItem} kind={requestKind} onClose={()=>setRequestItem(null)} onSent={()=>setWarehouseNotice("Đã gửi yêu cầu tới Kho & Dịch vụ.")}/>}
      <div style={{ padding: "20px 24px" }}>
        {/* Header */}
        <div style={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between", marginBottom: 16, flexWrap: "wrap", gap: 12 }}>
          <div>
            <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
              <span style={{ display: "inline-flex", padding: 6, borderRadius: 8, background: "#EFF6FF", color: "#2563EB" }}>
                <Wine size={18} />
              </span>
              <h2 style={{ fontSize: 18, fontWeight: 800, color: "#0F172A", margin: 0 }}>Minibar &amp; Quản lý kho</h2>
            </div>
            <p style={{ fontSize: 12, color: "#64748B", margin: 0 }}>
              Quản lý tồn kho minibar buồng phòng, thông báo bộ phận kho khi dưới ngưỡng an toàn và lập phiếu trả hàng về kho
            </p>
          </div>

          {/* Action buttons */}
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            {criticalMinibarLow.length > 0 && (
              <button
                onClick={handleNotifyWarehouseAll}
                style={{
                  padding: "8px 14px",
                  borderRadius: 8,
                  border: "none",
                  background: "#E11D48",
                  color: "#FFF",
                  fontSize: 12,
                  fontWeight: 700,
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  gap: 6,
                  boxShadow: "0 2px 4px rgba(225,29,72,0.2)",
                }}
              >
                <Send size={14} /> Báo kho cấp hàng ({criticalMinibarLow.length})
              </button>
            )}

            <button
              onClick={() => openReturnModal()}
              disabled={minibarItems.length === 0}
              style={{
                padding: "8px 16px",
                borderRadius: 8,
                border: "none",
                background: "#0F172A",
                color: "#FFF",
                fontSize: 12,
                fontWeight: 700,
                cursor: "pointer",
                display: "flex",
                alignItems: "center",
                gap: 6,
                boxShadow: "0 2px 6px rgba(15,23,42,0.18)",
              }}
            >
              <Archive size={14} style={{ color: "#38BDF8" }} /> Trả hàng về kho
            </button>
          </div>
        </div>

        {/* Notice from warehouse */}
        {warehouseNotice && (
          <div role="status" style={{ padding: "10px 14px", borderRadius: 8, background: "#DCFCE7", border: "1px solid #86EFAC", color: "#166534", fontSize: 12, fontWeight: 600, marginBottom: 16, display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <span>✓ {warehouseNotice}</span>
            <button onClick={() => setWarehouseNotice(null)} style={{ background: "transparent", border: "none", color: "#166534", cursor: "pointer", fontSize: 12 }}>✕</button>
          </div>
        )}

        {/* Workflow alert banner */}
        <div
          style={{
            padding: "12px 16px",
            borderRadius: 10,
            background: "#EFF6FF",
            border: "1px solid #BFDBFE",
            marginBottom: 16,
            display: "flex",
            alignItems: "flex-start",
            gap: 12,
          }}
        >
          <Sparkles size={18} style={{ color: "#2563EB", flexShrink: 0, marginTop: 2 }} />
          <div style={{ fontSize: 12, color: "#1E40AF", lineHeight: 1.5 }}>
            <strong>Kho minibar riêng của Buồng phòng:</strong> Kho duyệt cấp hàng thì trừ kho tổng và cộng kho Buồng phòng. Trả hàng còn dùng được sẽ trừ kho Buồng phòng và cộng lại kho tổng khi được duyệt. Báo hỏng được xác nhận sẽ trừ kho Buồng phòng, không cộng vào kho tổng.
          </div>
        </div>

        {/* Low stock alerts */}
        {criticalMinibarLow.length > 0 && (
          <div
            style={{
              padding: "12px 16px",
              borderRadius: 10,
              background: "#FFF1F2",
              border: "1px solid #FECDD3",
              marginBottom: 16,
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              flexWrap: "wrap",
              gap: 10,
            }}
          >
            <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
              <AlertTriangle size={18} style={{ color: "#E11D48", flexShrink: 0 }} />
              <div>
                <strong style={{ color: "#9F1239", fontSize: 12 }}>Vật dụng Minibar dưới ngưỡng an toàn tổng số lượng định mức:</strong>
                <p style={{ fontSize: 11, color: "#BE123C", margin: "2px 0 0" }}>
                  {criticalMinibarLow.map(i => `${i.item} (còn ${i.stock} ${i.unit} / định mức ${i.min})`).join(", ")}
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
              }}
            >
              <Send size={12} /> Báo kho cấp hàng khẩn cấp
            </button>
          </div>
        )}

        {/* KPI Cards */}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: 12, marginBottom: 18 }}>
          <div style={{ background: "#FFF", border: "1px solid #E2E8F0", borderRadius: 10, padding: "14px 16px" }}>
            <p style={{ fontSize: 11, color: "#64748B", margin: 0, fontWeight: 600 }}>Mặt hàng Minibar</p>
            <p style={{ fontSize: 22, fontWeight: 800, color: "#0F172A", margin: "4px 0 0", fontFamily: "'JetBrains Mono',monospace" }}>
              {minibarItems.length}
            </p>
            <span style={{ fontSize: 11, color: "#64748B" }}>Chủng loại đồ uống / snack</span>
          </div>

          <div style={{ background: "#FFF", border: "1px solid #E2E8F0", borderRadius: 10, padding: "14px 16px" }}>
            <p style={{ fontSize: 11, color: "#64748B", margin: 0, fontWeight: 600 }}>Tổng tồn kho Minibar</p>
            <p style={{ fontSize: 22, fontWeight: 800, color: "#2563EB", margin: "4px 0 0", fontFamily: "'JetBrains Mono',monospace" }}>
              {totalStock}
            </p>
            <span style={{ fontSize: 11, color: "#64748B" }}>Sản phẩm sẵn sàng cấp</span>
          </div>

          <div style={{ background: "#FFF", border: "1px solid #E2E8F0", borderRadius: 10, padding: "14px 16px" }}>
            <p style={{ fontSize: 11, color: "#64748B", margin: 0, fontWeight: 600 }}>Cảnh báo dưới ngưỡng an toàn</p>
            <p style={{ fontSize: 22, fontWeight: 800, color: criticalMinibarLow.length > 0 ? "#E11D48" : "#16A34A", margin: "4px 0 0", fontFamily: "'JetBrains Mono',monospace" }}>
              {criticalMinibarLow.length}
            </p>
            <span style={{ fontSize: 11, color: criticalMinibarLow.length > 0 ? "#E11D48" : "#16A34A" }}>
              {criticalMinibarLow.length > 0 ? "Mặt hàng cần kho cấp ngay" : "Tất cả trên ngưỡng an toàn"}
            </span>
          </div>

          <div style={{ background: "#FFF", border: "1px solid #E2E8F0", borderRadius: 10, padding: "14px 16px" }}>
            <p style={{ fontSize: 11, color: "#64748B", margin: 0, fontWeight: 600 }}>Phiếu trả hàng về kho</p>
            <p style={{ fontSize: 22, fontWeight: 800, color: pendingReturnsCount > 0 ? "#D97706" : "#0F172A", margin: "4px 0 0", fontFamily: "'JetBrains Mono',monospace" }}>
              {pendingReturnsCount} / {returnRequests.length}
            </p>
            <span style={{ fontSize: 11, color: pendingReturnsCount > 0 ? "#D97706" : "#64748B" }}>
              {pendingReturnsCount > 0 ? "Phiếu đang chờ kho duyệt" : "Đã duyệt hoàn tất"}
            </span>
          </div>
        </div>

        {/* Subtabs & search */}
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 14, flexWrap: "wrap", gap: 10 }}>
          <div style={{ display: "flex", gap: 8 }}>
            <button
              onClick={() => setActiveTab("items")}
              style={{
                padding: "6px 14px",
                borderRadius: 8,
                fontSize: 12,
                fontWeight: activeTab === "items" ? 700 : 500,
                background: activeTab === "items" ? "#0F172A" : "#FFF",
                color: activeTab === "items" ? "#FFF" : "#475569",
                border: `1px solid ${activeTab === "items" ? "#0F172A" : "#CBD5E1"}`,
                cursor: "pointer",
              }}
            >
              Danh mục &amp; Tồn kho ({minibarItems.length})
            </button>
            <button
              onClick={() => setActiveTab("returns")}
              style={{
                padding: "6px 14px",
                borderRadius: 8,
                fontSize: 12,
                fontWeight: activeTab === "returns" ? 700 : 500,
                background: activeTab === "returns" ? "#0F172A" : "#FFF",
                color: activeTab === "returns" ? "#FFF" : "#475569",
                border: `1px solid ${activeTab === "returns" ? "#0F172A" : "#CBD5E1"}`,
                cursor: "pointer",
                display: "inline-flex",
                alignItems: "center",
                gap: 6,
              }}
            >
              Phiếu trả hàng về kho ({returnRequests.length})
              {pendingReturnsCount > 0 && (
                <span style={{ fontSize: 10, fontWeight: 700, padding: "1px 6px", borderRadius: 99, background: "#F59E0B", color: "#FFF" }}>
                  {pendingReturnsCount} chờ duyệt
                </span>
              )}
            </button>
          </div>

          {activeTab === "items" && (
            <div style={{ position: "relative", minWidth: 220 }}>
              <Search size={13} style={{ position: "absolute", left: 10, top: "50%", transform: "translateY(-50%)", color: "#94A3B8" }} />
              <input
                value={search}
                onChange={e => setSearch(e.target.value)}
                placeholder="Tìm mặt hàng minibar..."
                style={{
                  width: "100%",
                  height: 32,
                  paddingLeft: 30,
                  paddingRight: 10,
                  borderRadius: 7,
                  border: "1px solid #CBD5E1",
                  background: "#FFF",
                  fontSize: 11,
                  boxSizing: "border-box",
                }}
              />
            </div>
          )}
        </div>

        {/* Subtab 1: Minibar Stock Items Grid */}
        {activeTab === "items" && (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))", gap: 12 }}>
            {filteredItems.map(item => {
              const stat = stockStatus(item);
              const pct = stockPct(item);
              const belowThreshold = isLowStock(item);
              return (
                <div
                  key={item.id}
                  style={{
                    background: "#FFF",
                    borderRadius: 12,
                    border: `1px solid ${belowThreshold ? "#FECDD3" : item.stock <= item.min ? "#FED7AA" : "#E2E8F0"}`,
                    padding: "16px",
                    boxShadow: belowThreshold ? "0 2px 8px rgba(225,29,72,0.06)" : "0 1px 3px rgba(0,0,0,0.03)",
                  }}
                >
                  <div style={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between", marginBottom: 10 }}>
                    <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                      <div style={{ width: 34, height: 34, borderRadius: 8, background: "#EFF6FF", display: "flex", alignItems: "center", justifyContent: "center" }}>
                        <Wine size={16} style={{ color: "#2563EB" }} />
                      </div>
                      <div>
                        <p style={{ fontSize: 13, fontWeight: 700, color: "#0F172A", margin: 0 }}>{item.item}</p>
                        <p style={{ fontSize: 10, color: "#64748B", margin: "2px 0 0" }}>Mã: {item.id}</p>
                      </div>
                    </div>
                    <span style={{ fontSize: 10, fontWeight: 700, padding: "2px 8px", borderRadius: 99, background: stat.bg, color: stat.color }}>
                      {stat.label}
                    </span>
                  </div>

                  <div style={{ display: "flex", alignItems: "baseline", gap: 6, margin: "10px 0 6px" }}>
                    <span style={{ fontFamily: "'JetBrains Mono',monospace", fontSize: 24, fontWeight: 800, color: stat.color, lineHeight: 1 }}>
                      {item.stock}
                    </span>
                    <span style={{ fontSize: 11, color: "#64748B" }}>{item.unit} trong kho</span>
                    <span style={{ fontSize: 10, color: "#94A3B8", marginLeft: "auto" }}>
                      Định mức: {item.min} {item.unit}
                    </span>
                  </div>

                  <div style={{ height: 6, background: "#F1F5F9", borderRadius: 99, overflow: "hidden", marginBottom: 12 }}>
                    <div style={{ height: "100%", background: stat.color, width: `${pct}%`, transition: "width .3s" }} />
                  </div>

                  <div style={{ display: "flex", gap: 8 }}>
                    <button
                      onClick={() => openReturnModal(item)}
                      style={{
                        flex: 1,
                        padding: "7px 10px",
                        borderRadius: 7,
                        border: "1px solid #CBD5E1",
                        background: "#FFF",
                        color: "#0F172A",
                        fontSize: 11,
                        fontWeight: 600,
                        cursor: "pointer",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        gap: 4,
                      }}
                    >
                      <Archive size={12} style={{ color: "#0F172A" }} /> Trả về kho
                    </button>

                    <button onClick={()=>{setRequestKind("DAMAGE");setRequestItem(item);}} style={{padding:7,border:"1px solid #E2E8F0",borderRadius:7,fontSize:11}}>Báo hỏng</button>
                    {belowThreshold ? (
                      <button
                        onClick={() => handleNotifyWarehouse(item)}
                        style={{
                          flex: 1,
                          padding: "7px 10px",
                          borderRadius: 7,
                          border: "1px solid #FECDD3",
                          background: "#FFF1F2",
                          color: "#E11D48",
                          fontSize: 11,
                          fontWeight: 700,
                          cursor: "pointer",
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          gap: 4,
                        }}
                      >
                        <Send size={12} /> Báo kho cấp hàng
                      </button>
                    ) : (
                      <button
                        onClick={() => handleNotifyWarehouse(item)}
                        style={{
                          flex: 1,
                          padding: "7px 10px",
                          borderRadius: 7,
                          border: "1px solid #E2E8F0",
                          background: "#F8FAFC",
                          color: "#64748B",
                          fontSize: 11,
                          fontWeight: 500,
                          cursor: "pointer",
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          gap: 4,
                        }}
                      >
                        <Send size={12} /> Báo kho
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* Subtab 2: Return Requests & Warehouse Approval */}
        {activeTab === "returns" && (
          <div style={{ background: "#FFF", borderRadius: 12, border: "1px solid #E2E8F0", overflow: "hidden" }}>
            <div style={{ padding: "14px 18px", borderBottom: "1px solid #E2E8F0", background: "#F8FAFC", display: "flex", justifyContent: "space-between", alignItems: "center", flexWrap: "wrap", gap: 10 }}>
              <div>
                <h3 style={{ fontSize: 13, fontWeight: 700, color: "#0F172A", margin: 0 }}>
                  Danh sách yêu cầu trả hàng về kho
                </h3>
                <p style={{ fontSize: 11, color: "#64748B", margin: "2px 0 0" }}>
                  Theo dõi hàng buồng phòng hoàn trả và quy trình duyệt nhận hàng từ bộ phận kho
                </p>
              </div>
              <button
                onClick={() => openReturnModal()}
                style={{
                  padding: "6px 14px",
                  borderRadius: 7,
                  background: "#0F172A",
                  color: "#FFF",
                  fontSize: 11,
                  fontWeight: 700,
                  border: "none",
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  gap: 5,
                }}
              >
                <Plus size={12} /> Tạo phiếu trả hàng
              </button>
            </div>

            <div style={{ overflowX: "auto" }}>
              <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 12, textAlign: "left" }}>
                <thead>
                  <tr style={{ background: "#F8FAFC", borderBottom: "1px solid #E2E8F0", color: "#64748B", fontSize: 11 }}>
                    <th style={{ padding: "10px 16px" }}>Mã phiếu</th>
                    <th style={{ padding: "10px 16px" }}>Mặt hàng hoàn trả</th>
                    <th style={{ padding: "10px 16px" }}>Số lượng</th>
                    <th style={{ padding: "10px 16px" }}>Lý do trả hàng</th>
                    <th style={{ padding: "10px 16px" }}>Người tạo &amp; Thời gian</th>
                    <th style={{ padding: "10px 16px" }}>Trạng thái</th>
                    <th style={{ padding: "10px 16px", textAlign: "right" }}>Thao tác duyệt kho</th>
                  </tr>
                </thead>
                <tbody>
                  {returnRequests.length === 0 && (
                    <tr>
                      <td colSpan={7} style={{ padding: "28px 16px", textAlign: "center", color: "#94A3B8" }}>
                        Chưa có yêu cầu trả hàng về kho nào.
                      </td>
                    </tr>
                  )}
                  {returnRequests.map(req => {
                    const isPending = req.status === "CHO_KHO_DUYET";
                    return (
                      <tr key={req.id} style={{ borderBottom: "1px solid #F1F5F9" }}>
                        <td style={{ padding: "12px 16px", fontFamily: "'JetBrains Mono',monospace", fontWeight: 700, color: "#0F172A" }}>
                          {req.id}
                        </td>
                        <td style={{ padding: "12px 16px", fontWeight: 600, color: "#0F172A" }}>
                          {req.itemName}
                        </td>
                        <td style={{ padding: "12px 16px", color: "#0F172A" }}>
                          <span style={{ fontFamily: "'JetBrains Mono',monospace", fontWeight: 700, color: "#2563EB" }}>
                            {req.quantity}
                          </span>{" "}
                          {req.unit}
                        </td>
                        <td style={{ padding: "12px 16px", color: "#475569" }}>
                          {req.reason}
                        </td>
                        <td style={{ padding: "12px 16px", color: "#64748B", fontSize: 11 }}>
                          <div>{req.requestedBy}</div>
                          <div style={{ color: "#94A3B8" }}>{req.requestedAt}</div>
                        </td>
                        <td style={{ padding: "12px 16px" }}>
                          {isPending ? (
                            <span style={{ fontSize: 10, fontWeight: 700, padding: "3px 8px", borderRadius: 99, background: "#FEF3C7", color: "#92400E", display: "inline-flex", alignItems: "center", gap: 4 }}>
                              <Clock size={11} /> Chờ kho duyệt
                            </span>
                          ) : (
                            <span style={{ fontSize: 10, fontWeight: 700, padding: "3px 8px", borderRadius: 99, background: "#DCFCE7", color: "#166534", display: "inline-flex", alignItems: "center", gap: 4 }}>
                              <CheckCircle2 size={11} /> {req.status==="TU_CHOI"?"Kho từ chối":"Kho đã duyệt"}
                            </span>
                          )}
                        </td>
                        <td style={{ padding: "12px 16px", textAlign: "right" }}>
                          {isPending ? (
                            <span>Chờ Kho & Dịch vụ xử lý</span>
                          ) : (
                            <span style={{ fontSize: 11, color: "#16A34A", fontWeight: 600 }}>
                              {req.status==="TU_CHOI"?"Tồn kho không thay đổi":`✓ Đã hoàn kho ${req.approvedAt??""}`}
                            </span>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Modal: Lập phiếu Trả hàng về kho */}
        {returnModalOpen && (
          <div
            role="presentation"
            onMouseDown={e => { if (e.target === e.currentTarget && !returnBusy) setReturnModalOpen(false); }}
            style={{
              position: "fixed",
              inset: 0,
              zIndex: 120,
              background: "rgba(15,23,42,0.48)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              padding: 16,
            }}
          >
            <div
              role="dialog"
              aria-modal="true"
              style={{
                width: "100%",
                maxWidth: 480,
                background: "#FFF",
                borderRadius: 14,
                border: "1px solid #E2E8F0",
                boxShadow: "0 20px 60px rgba(15,23,42,0.24)",
                overflow: "hidden",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "16px 20px", borderBottom: "1px solid #E2E8F0" }}>
                <div>
                  <h3 style={{ margin: 0, fontSize: 16, fontWeight: 800, color: "#0F172A" }}>
                    Trả hàng về kho
                  </h3>
                  <p style={{ margin: "2px 0 0", fontSize: 11, color: "#64748B" }}>
                    Tạo yêu cầu hoàn trả hàng về kho tổng (chờ bộ phận kho kiểm tra và duyệt)
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() => setReturnModalOpen(false)}
                  disabled={returnBusy}
                  style={{ width: 28, height: 28, borderRadius: 6, border: "1px solid #E2E8F0", background: "#F8FAFC", cursor: "pointer", display: "flex", alignItems: "center", justifyContent: "center" }}
                >
                  <X size={14} />
                </button>
              </div>

              <div style={{ padding: "18px 20px", display: "grid", gap: 14 }}>
                <label style={{ display: "grid", gap: 5, fontSize: 11, fontWeight: 700, color: "#334155" }}>
                  Mặt hàng hoàn trả
                  <select
                    value={returnItemId}
                    onChange={e => setReturnItemId(e.target.value)}
                    disabled={returnBusy}
                    style={{ height: 38, padding: "0 10px", border: "1px solid #CBD5E1", borderRadius: 8, background: "#FFF", fontSize: 12 }}
                  >
                    {minibarItems.map(item => (
                      <option key={item.id} value={item.id}>
                        {item.item} ({item.id}) · Hiện có {item.stock} {item.unit}
                      </option>
                    ))}
                  </select>
                </label>

                <label style={{ display: "grid", gap: 5, fontSize: 11, fontWeight: 700, color: "#334155" }}>
                  Số lượng hoàn trả
                  <input
                    type="number"
                    min="1"
                    step="1"
                    value={returnQuantity}
                    onChange={e => setReturnQuantity(e.target.value)}
                    disabled={returnBusy}
                    style={{ height: 38, padding: "0 10px", border: "1px solid #CBD5E1", borderRadius: 8, fontSize: 13 }}
                  />
                </label>

                <label style={{ display: "grid", gap: 5, fontSize: 11, fontWeight: 700, color: "#334155" }}>
                  Lý do hoàn trả
                  <select
                    value={returnReasonType}
                    onChange={e => setReturnReasonType(e.target.value)}
                    disabled={returnBusy}
                    style={{ height: 38, padding: "0 10px", border: "1px solid #CBD5E1", borderRadius: 8, background: "#FFF", fontSize: 12 }}
                  >
                    <option value="Thừa ca trực, hoàn trả kho">Thừa ca trực, hoàn trả kho</option>
                    <option value="Hàng cận date / hết hạn sử dụng">Hàng cận date / hết hạn sử dụng</option>
                    <option value="Bao bì rách / móp méo vận chuyển">Bao bì rách / móp méo vận chuyển</option>
                    <option value="Lỗi tem nhãn / kiểm tra không đạt">Lỗi tem nhãn / kiểm tra không đạt</option>
                    <option value="Khác">Lý do khác...</option>
                  </select>
                </label>

                {returnReasonType === "Khác" && (
                  <label style={{ display: "grid", gap: 5, fontSize: 11, fontWeight: 700, color: "#334155" }}>
                    Chi tiết lý do
                    <input
                      value={returnCustomReason}
                      onChange={e => setReturnCustomReason(e.target.value)}
                      disabled={returnBusy}
                      placeholder="Ghi rõ tình trạng mặt hàng..."
                      style={{ height: 38, padding: "0 10px", border: "1px solid #CBD5E1", borderRadius: 8, fontSize: 12 }}
                    />
                  </label>
                )}

                {returnError && (
                  <div role="alert" style={{ padding: "8px 12px", borderRadius: 6, background: "#FEE2E2", color: "#DC2626", fontSize: 11, fontWeight: 600 }}>
                    ⚠️ {returnError}
                  </div>
                )}
              </div>

              <div style={{ padding: "14px 20px", borderTop: "1px solid #E2E8F0", background: "#F8FAFC", display: "flex", justifyContent: "flex-end", gap: 10 }}>
                <button
                  type="button"
                  onClick={() => setReturnModalOpen(false)}
                  disabled={returnBusy}
                  style={{ padding: "8px 16px", borderRadius: 8, border: "1px solid #CBD5E1", background: "#FFF", color: "#475569", fontSize: 12, fontWeight: 600, cursor: "pointer" }}
                >
                  Hủy
                </button>
                <button
                  type="button"
                  onClick={() => void handleSubmitReturn()}
                  disabled={returnBusy}
                  style={{
                    padding: "8px 18px",
                    borderRadius: 8,
                    border: "none",
                    background: "#0F172A",
                    color: "#FFF",
                    fontSize: 12,
                    fontWeight: 700,
                    cursor: returnBusy ? "wait" : "pointer",
                    display: "flex",
                    alignItems: "center",
                    gap: 6,
                  }}
                >
                  <Archive size={14} /> {returnBusy ? "Đang gửi…" : "Gửi yêu cầu trả hàng"}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

/* ══════════════════════════════════════════════════════════
   SHARED KPI BAR
══════════════════════════════════════════════════════════ */
