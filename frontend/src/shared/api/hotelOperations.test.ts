import { beforeEach, describe, expect, it, vi } from "vitest";
import { apiClient } from "./client";
import { canActivatePrice, canApprove, hotelOperationsApi } from "./hotelOperations";

describe("warehouse/accounting API contract", () => {
  const request = vi.spyOn(apiClient, "request");
  beforeEach(() => { request.mockReset(); request.mockResolvedValue([]); });

  it("uses the audited endpoint paths and snake_case query names", async () => {
    request.mockResolvedValue({items:[],page:0,size:20,total_elements:0,total_pages:0,method_counts:{}});
    await hotelOperationsApi.services(); await hotelOperationsApi.lowStock(); await hotelOperationsApi.priceRequests();
    await hotelOperationsApi.inventoryMovements("mini bar"); await hotelOperationsApi.inventoryReport("mini bar", { from: "2026-09-01", to: "2026-09-18" });
    await hotelOperationsApi.priceHistory("mini bar"); await hotelOperationsApi.invoices({ reservation_id: 7 });
    await hotelOperationsApi.payments({ invoice_id: 4, page: 2 }); await hotelOperationsApi.receipts({ issued_by: "e1" });
    await hotelOperationsApi.expenses(); await hotelOperationsApi.partnerDebts(); await hotelOperationsApi.settlements(3); await hotelOperationsApi.ledger({ entry_type: "PAYMENT" }); await hotelOperationsApi.reconciliation({ from: "2026-09-01" });
    expect(request.mock.calls.map(call => call[0])).toEqual([
      "/api/services", "/api/services/low-stock", "/api/services/price-requests", "/api/services/mini%20bar/inventory-movements", "/api/services/mini%20bar/inventory-movements/inventory-report?from=2026-09-01&to=2026-09-18", "/api/services/mini%20bar/price-history", "/api/invoices?reservation_id=7", "/api/finance/payments?invoice_id=4&page=2", "/api/finance/receipts?issued_by=e1", "/api/finance/expenses", "/api/finance/partner-debts", "/api/finance/partner-debts/3/settlements", "/api/finance/ledger?entry_type=PAYMENT", "/api/finance/reconciliation?from=2026-09-01",
    ]);
  });

  it("transports exact snake_case bodies and caller idempotency headers", async () => {
    await hotelOperationsApi.restock("S1", { quantity: 5 }, "stock-1"); await hotelOperationsApi.recordInventoryMovement({ service_id: "S1", type: "ISSUE", quantity: 2, reason: "Bán minibar" }, "movement-1"); await hotelOperationsApi.submitPrice("S1", { price: 120000, reason: "Mùa cao điểm" }, "price-submit-1"); await hotelOperationsApi.activatePrice("S1", { price: 120000, reason: "Mùa cao điểm" }, "price-activate-1"); await hotelOperationsApi.issueReceipt(9, { receipt_number: "R-9", amount: 120000, method: "CASH" }, "receipt-1");
    for (const [path, options] of request.mock.calls) expect(options).toEqual(expect.objectContaining({ method: "POST", idempotencyKey: expect.any(String) }));
    expect(request.mock.calls[0][1]).toEqual(expect.objectContaining({ body: { quantity: 5 } }));
    expect(request.mock.calls[1][0]).toBe("/api/services/S1/inventory-movements");
    expect(request.mock.calls[1][1]).toEqual(expect.objectContaining({ body: { service_id: "S1", type: "ISSUE", quantity: 2, reason: "Bán minibar" } }));
    expect(request.mock.calls[2][1]).toEqual(expect.objectContaining({ body: { price: 120000, reason: "Mùa cao điểm" } }));
    expect(request.mock.calls[3][0]).toBe("/api/services/S1/price/activate");
    expect(request.mock.calls[3][1]).toEqual(expect.objectContaining({ body: { price: 120000, reason: "Mùa cao điểm" } }));
    expect(request.mock.calls[4][1]).toEqual(expect.objectContaining({ body: { receipt_number: "R-9", amount: 120000, method: "CASH" } }));
  });

  it("uses idempotent approval decisions and reads the contract-backed cash handover route", async () => {
    await hotelOperationsApi.approve(12, "approval-approve-1");
    await hotelOperationsApi.reject(13, "approval-reject-1");
    await hotelOperationsApi.cashHandovers();
    expect(request.mock.calls[0]).toEqual(["/api/governance/approvals/12/approve", expect.objectContaining({ method: "POST", idempotencyKey: "approval-approve-1" })]);
    expect(request.mock.calls[1]).toEqual(["/api/governance/approvals/13/reject", expect.objectContaining({ method: "POST", idempotencyKey: "approval-reject-1" })]);
    expect(request.mock.calls[2][0]).toBe("/api/finance/cash-handovers");
  });

  it("uses the current idempotent finance mutation controllers", async () => {
    await hotelOperationsApi.recordCashHandover({ shift_code: "S-1", from_actor: "e1", to_actor: "e2", actual_amount: 500000, note: "Bàn giao đủ" }, "cash-1");
    await hotelOperationsApi.recordExpense({ category: "SUPPLIES", description: "Mua nước", amount: 120000, method: "BANK_TRANSFER" }, "expense-1");
    await hotelOperationsApi.recordPartnerDebt({ partner_name: "Nhà cung cấp A", reference_code: "PO-1", amount: 900000 }, "debt-1");
    await hotelOperationsApi.settlePartnerDebt(7, { amount: 300000, method: "BANK_TRANSFER", note: "Thanh toán đợt 1" }, "settle-1");
    expect(request.mock.calls).toEqual([
      ["/api/finance/cash-handovers", expect.objectContaining({ method: "POST", body: { shift_code: "S-1", from_actor: "e1", to_actor: "e2", actual_amount: 500000, note: "Bàn giao đủ" }, idempotencyKey: "cash-1" })],
      ["/api/finance/expenses", expect.objectContaining({ method: "POST", body: { category: "SUPPLIES", description: "Mua nước", amount: 120000, method: "BANK_TRANSFER" }, idempotencyKey: "expense-1" })],
      ["/api/finance/partner-debts", expect.objectContaining({ method: "POST", body: { partner_name: "Nhà cung cấp A", reference_code: "PO-1", amount: 900000 }, idempotencyKey: "debt-1" })],
      ["/api/finance/partner-debts/7/settle", expect.objectContaining({ method: "POST", body: { amount: 300000, method: "BANK_TRANSFER", note: "Thanh toán đợt 1" }, idempotencyKey: "settle-1" })],
    ]);
  });

  it("rejects missing or malformed idempotency keys before a mutation request", async () => {
    expect(() => hotelOperationsApi.recordInventoryMovement({ service_id: "S1", type: "ADJUST", quantity: -1, reason: "Kiểm kê" }, "")).toThrow("Idempotency-Key");
    expect(() => hotelOperationsApi.issueReceipt(9, { receipt_number: "R-9", amount: 120000, method: "CASH" }, "bad key")).toThrow("Idempotency-Key");
    expect(request).not.toHaveBeenCalled();
  });

  it("uses the Idempotency-Key header and keeps payment idempotency out of JSON", async () => {
    await hotelOperationsApi.recordPayment(9, { amount: 120000, method: "CASH", type: "PAYMENT", reference: null }, "payment-9-1");
    await hotelOperationsApi.adjustInvoice(9, { delta: -1000, reason: "Điều chỉnh minibar" }, "adjust-9-1");
    expect(request).toHaveBeenCalledWith("/api/invoices/9/payments", expect.objectContaining({ method: "POST", body: { amount: 120000, method: "CASH", type: "PAYMENT", reference: null }, idempotencyKey: "payment-9-1" }));
    expect(request).toHaveBeenCalledWith("/api/invoices/9/adjust", expect.objectContaining({ method: "POST", body: { delta: -1000, reason: "Điều chỉnh minibar" }, idempotencyKey: "adjust-9-1" }));
  });

  it("keeps requester and canonical status guards in the client contract", () => {
    expect(canApprove({ status: "PENDING", requester: "e1" }, ["APPROVAL_APPROVE"], "e1")).toBe(false);
    expect(canApprove({ status: "APPROVED", requester: "e2" }, ["APPROVAL_APPROVE"], "e1")).toBe(false);
    expect(canApprove({ status: "PENDING", requester: "e2" }, ["APPROVAL_APPROVE"], "e1")).toBe(true);
    expect(canApprove({ status: "PENDING", requester: "e2" }, [], "e1")).toBe(false);
    expect(canApprove({ status: "PENDING", requester: "e2" }, ["APPROVAL_APPROVE"], undefined)).toBe(false);
    expect(canActivatePrice({ status: "APPROVED", action: "SERVICE_PRICE_CHANGE", target_id: "S1", requester: "e1" }, "S1", ["SERVICE_PRICE_ACTIVATE"], "e2")).toBe(true);
    expect(canActivatePrice({ status: "PENDING", action: "SERVICE_PRICE_CHANGE", target_id: "S1", requester: "e1" }, "S1", ["SERVICE_PRICE_ACTIVATE"], "e2")).toBe(false);
    expect(canActivatePrice({ status: "APPROVED", action: "SERVICE_PRICE_CHANGE", target_id: "S1", requester: "e2" }, "S1", ["SERVICE_PRICE_ACTIVATE"], "e2")).toBe(false);
  });

  it("normalizes page responses for invoices and payments safely", async () => {
    request.mockResolvedValueOnce({
      items: [{ id: 1 }],
      page: 1,
      total_elements: 21,
      total_pages: 2,
      size: 20,
    } as any);
    const invoicePage = await hotelOperationsApi.invoices();
    expect(invoicePage).toEqual(expect.objectContaining({
      items: [{ id: 1 }],
      totalElements: 21,
      totalPages: 2,
      size: 20,
    }));

    request.mockResolvedValueOnce({ items:[{id:2,reservation_id:150}],page:14,size:10,total_elements:141,total_pages:15,method_counts:{CASH:141} } as any);
    const paymentPage = await hotelOperationsApi.payments({page:14,size:10,search:"PAY-2",method:"CASH"});
    expect(paymentPage).toEqual({
      items: [{ id: 2,reservation_id:150 }],
      page: 14,
      size: 10,
      totalElements: 141,
      totalPages: 15,
      methodCounts:{CASH:141},
    });
    expect(request).toHaveBeenLastCalledWith("/api/finance/payments?page=14&size=10&search=PAY-2&method=CASH");
  });

  it("rejects missing or inconsistent page metadata rather than hiding remaining records", async()=>{
    request.mockResolvedValueOnce([{id:1}] as any);
    await expect(hotelOperationsApi.invoices()).rejects.toThrow("Phản hồi phân trang không hợp lệ");
    request.mockResolvedValueOnce({items:[{id:1}],page:0,size:10,total_elements:150,total_pages:1} as any);
    await expect(hotelOperationsApi.payments()).rejects.toThrow("Phản hồi phân trang không hợp lệ");
    request.mockResolvedValueOnce({items:[{id:1}],page:0,size:10,total_elements:150,total_pages:15,method_counts:{CASH:150}} as any);
    await expect(hotelOperationsApi.payments()).rejects.toThrow("Phản hồi phân trang không hợp lệ");
  });
});
