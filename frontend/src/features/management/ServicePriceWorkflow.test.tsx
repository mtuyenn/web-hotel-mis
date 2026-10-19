import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { approvalsApi } from "../../shared/api/approvals";
import { authApi } from "../../shared/api/auth";
import { ApiError } from "../../shared/api/client";
import { financeApi } from "../../shared/api/finance";
import { frontDeskApi } from "../../shared/api/frontDesk";
import { housekeepingTechnicalApi } from "../../shared/api/housekeepingTechnical";
import { hrApi } from "../../shared/api/hr";
import { hrGovernanceApi } from "../../shared/api/hrGovernance";
import { personnelApi } from "../../shared/api/personnel";
import { warehouseServicesApi } from "../../shared/api/warehouseServices";
import ManagerDashboard from "./ManagerDashboard";

describe("Service price approval workflow", () => {
  const onBack=vi.fn();
  beforeEach(() => {
    vi.restoreAllMocks();
    vi.spyOn(personnelApi, "requests").mockResolvedValue([]);

    // Mock manager profile
    vi.spyOn(authApi, "employeeProfile").mockResolvedValue({
      employee_id: "EMP_MGR",
      full_name: "Trần Quản Lý Vận Hành",
      role: "MANAGER",
      permissions: ["APPROVAL_APPROVE", "TECHNICAL_WORK_ORDER_READ", "TECHNICAL_WORK_ORDER_ACCEPT"],
    });

    // Mock rooms
    vi.spyOn(housekeepingTechnicalApi, "rooms").mockResolvedValue([
      { id: "R101", name: "101", room_type_id: "RT1", room_type_name: "Standard", daily_price: 1000000, floor: 1, status: "available" },
      { id: "R202", name: "202", room_type_id: "RT1", room_type_name: "Standard", daily_price: 1000000, floor: 2, status: "maintenance" },
    ]);

    // Mock employees
    vi.spyOn(hrGovernanceApi, "employees").mockResolvedValue([]);

    // Mock tasks
    vi.spyOn(housekeepingTechnicalApi, "tasks").mockResolvedValue([]);

    // Mock incidents
    vi.spyOn(housekeepingTechnicalApi, "incidents").mockResolvedValue([]);

    // Mock invoices
    vi.spyOn(frontDeskApi, "invoices").mockResolvedValue({
      items: [],
      page: 0,
      size: 100,
      total_elements: 0,
      total_pages: 0,
    });

    // Mock expenses
    vi.spyOn(financeApi, "expenses").mockResolvedValue([]);
    vi.spyOn(approvalsApi, "approvals").mockResolvedValue([]);
    vi.spyOn(warehouseServicesApi, "services").mockResolvedValue([]);
    vi.spyOn(hrApi, "leaves").mockResolvedValue([]);
    vi.spyOn(financeApi, "ledger").mockResolvedValue({items: [], page: 0, size: 100, totalElements: 0, totalPages: 0});
    vi.spyOn(financeApi, "reconciliation").mockResolvedValue({recognized_revenue: 0, net_total: 0, total_payments: 0, total_refunds: 0} as any);

    // Mock work orders
    vi.spyOn(housekeepingTechnicalApi, "workOrders").mockResolvedValue([]);
    vi.spyOn(housekeepingTechnicalApi, "equipment").mockResolvedValue([]);
  });
  it("keeps approval separate from activation and retains the approved proposal after an activation failure", async () => {
    const proposal = {id:81,requester:"KITCHEN",action:"SERVICE_PRICE_CHANGE",target_id:"COURT",payload:"180000",amount:180000,reason:"Chi phí tăng",risk:"LOW",status:"PENDING",approver:null,requested_at:"2035-01-01T08:00:00",expires_at:"2035-02-01T08:00:00",decided_at:null,consumed_at:null,idempotency_key:null} as const;
    let approved=false;
    vi.spyOn(approvalsApi,"approve").mockImplementation(async () => { approved=true; return {...proposal,status:"APPROVED",approver:"EMP_MGR"}; });
    vi.mocked(approvalsApi.approvals).mockImplementation(async (status) => status === "PENDING"
      ? approved ? [] : [proposal]
      : approved ? [{...proposal,status:"APPROVED",approver:"EMP_MGR"}] : []);
    vi.mocked(warehouseServicesApi.services).mockResolvedValue([{id:"COURT",name:"Cầu lông",price:150000,unit:"giờ",stock:10,safety_threshold:0,low_stock:false}]);
    const activate=vi.spyOn(warehouseServicesApi,"activatePrice").mockRejectedValue(new ApiError(503,{code:"BUSY",message:"Máy chủ bận"}));
    render(<ManagerDashboard role="manager" onBack={onBack} />);
    await screen.findByText("Cầu lông (COURT)");
    fireEvent.click(screen.getByRole("button",{name:/Trung tâm Phê duyệt/i}));
    fireEvent.click(await screen.findByRole("button",{name:/^Phê duyệt$/i}));
    await screen.findByText(/Giá hiện tại được giữ/);
    expect(activate).not.toHaveBeenCalled();
    fireEvent.click(await screen.findByRole("button",{name:"Áp dụng giá ngay"}));
    await waitFor(() => expect(screen.getAllByRole("alert").some(item => item.textContent?.includes("Máy chủ bận"))).toBe(true));
    expect(screen.getByText("Đã duyệt · Chưa áp dụng")).toBeTruthy();
    expect(screen.getByRole("button",{name:"Áp dụng giá ngay"})).toBeTruthy();
    expect(activate).toHaveBeenCalledWith("COURT",{price:180000,reason:"Chi phí tăng"},"act-mgr-81");
  });

});
