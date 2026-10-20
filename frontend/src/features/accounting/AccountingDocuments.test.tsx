import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import InvoiceOperations from "./InvoiceOperations";
import CashHandoverPanel from "./CashHandoverPanel";
import { financeApi as api } from "../../shared/api/finance";
import type { Invoice, Receipt } from "../../shared/types/hotelOperations";

const invoice: Invoice = { id: 3, reservation_id: 7, issued_at: "2026-10-17T10:00:00", room_total: 20000000, service_total: 0, late_surcharge: 0, compensation: 0, extension_total: 0, adjustment_total: 0, discount: 0, deposit: 5000000, payable: 15000000, payment_method: "CASH", status: "CHUA_THANH_TOAN", deposit_refunded: false };
const receipt: Receipt = { id: 4, invoice_id: 3, receipt_number: "PT-004", amount: 5000000, method: "CASH", issued_at: "2026-10-17T10:00:00", issued_by: "FRONTDESK" };
const mountInvoices = () => render(<InvoiceOperations onChanged={vi.fn()} onVatCreated={vi.fn()} refresh={0} search="" vatInvoices={[]} />);
beforeEach(() => {
  vi.spyOn(api, "invoices").mockResolvedValue({ items: [invoice], page: 0, size: 10, totalElements: 1, totalPages: 1 });
  vi.spyOn(api, "refundRequests").mockResolvedValue([]);
  vi.spyOn(api, "invoicePayments").mockResolvedValue([{ id: 1, invoice_id: 3, amount: 5000000, method: "CASH", type: "PAYMENT", status: "COMPLETED", reference: null, occurred_at: "2026-10-17T10:00:00", actor_id: "FRONTDESK" }]);
  vi.spyOn(api, "invoiceReceipts").mockResolvedValue([receipt]);
});
afterEach(() => vi.restoreAllMocks());

describe("Accounting uses existing financial records", () => {
  it("shows a recorded deposit refund instead of paid for a cancelled booking", async () => {
    const takePayment = vi.spyOn(api, "recordPayment");
    vi.mocked(api.invoices).mockResolvedValue({ items: [{ ...invoice, room_total: 0, deposit: 0, payable: 0, status: "DA_THANH_TOAN", deposit_refunded: true }], page: 0, size: 10, totalElements: 1, totalPages: 1 });
    vi.mocked(api.invoicePayments).mockResolvedValue([]);
    vi.mocked(api.invoiceReceipts).mockResolvedValue([]);
    mountInvoices();
    expect(await screen.findByText("Đã hoàn cọc")).toBeTruthy();
    expect(screen.queryByText("Đã thanh toán")).toBeNull();
    expect(screen.getByRole("columnheader", { name: "Cọc còn giữ" })).toBeTruthy();
    fireEvent.click(screen.getByRole("button", { name: "Xem / ghi nhận" }));
    expect((await screen.findByRole("status")).textContent).toBe("Đã hoàn cọc");
    expect(takePayment).not.toHaveBeenCalled();
  });

  it("does not call a zero-balance invoice refunded without a recorded deposit refund", async () => {
    vi.mocked(api.invoices).mockResolvedValue({ items: [{ ...invoice, room_total: 0, deposit: 0, payable: 0, status: "DA_THANH_TOAN" }], page: 0, size: 10, totalElements: 1, totalPages: 1 });
    mountInvoices();
    expect(await screen.findByText("Đã thanh toán")).toBeTruthy();
    expect(screen.queryByText("Đã hoàn cọc")).toBeNull();
  });

  it("opens the saved receipt unchanged for reprinting without issuing a receipt or taking payment", async () => {
    const takePayment = vi.spyOn(api, "recordPayment");
    const issueReceipt = vi.spyOn(api, "issueReceipt");
    mountInvoices();
    fireEvent.click(await screen.findByRole("button", { name: "Xem / ghi nhận" }));
    fireEvent.click(await screen.findByRole("button", { name: "In phiếu thu PT-004" }));
    const preview = screen.getByRole("dialog", { name: "PHIẾU THU · PT-004" });
    expect(within(preview).getByText("FRONTDESK")).toBeTruthy();
    expect(within(preview).getByText("INV-3")).toBeTruthy();
    expect(within(preview).getByText(/5\.000\.000/)).toBeTruthy();
    expect(within(preview).queryByRole("textbox")).toBeNull();
    expect(within(preview).getByRole("button", { name: "In / Lưu PDF" })).toBeTruthy();
    fireEvent.click(within(preview).getByRole("button", { name: "Đóng" }));
    fireEvent.click(screen.getByRole("button", { name: "In phiếu thu PT-004" }));
    expect(within(screen.getByRole("dialog")).getByText("FRONTDESK")).toBeTruthy();
    expect(takePayment).not.toHaveBeenCalled();
    expect(issueReceipt).not.toHaveBeenCalled();
  });

  it("shows the outstanding balance for reconciliation without offering guest collection", async () => {
    const takePayment = vi.spyOn(api, "recordPayment");
    mountInvoices();
    fireEvent.click(await screen.findByRole("button", { name: "Xem / ghi nhận" }));
    await screen.findByRole("button", { name: "In phiếu thu PT-004" });
    expect(screen.getByRole("heading", { name: "Hóa đơn, đối soát & phiếu thu" })).toBeTruthy();
    expect(screen.queryByRole("button", { name: "Ghi nhận thu tiền" })).toBeNull();
    expect(screen.getByRole("button", { name: "Xin Giám đốc duyệt hoàn tiền" })).toBeTruthy();
    expect(screen.getAllByText(/15\.000\.000/).length).toBeGreaterThan(0);
    expect(takePayment).not.toHaveBeenCalled();
  });

  it("still executes an approved refund with its original approval-bound payment key", async () => {
    const takePayment = vi.spyOn(api, "recordPayment").mockResolvedValue({ id: 2, invoice_id: 3, amount: 1000000, method: "CASH", type: "REFUND", status: "COMPLETED", reference: "Guest refund", occurred_at: "2026-10-18T10:00:00", actor_id: "ACCOUNTING" });
    vi.mocked(api.refundRequests).mockResolvedValue([{ id: 9, requester: "ACCOUNTING", action: "PAYMENT_REFUND", target_id: "3", payload: JSON.stringify({ method: "CASH", type: "REFUND", reference: "Guest refund", idempotency_key: "approved-refund" }), amount: 1000000, reason: "Guest refund", risk: "HIGH", status: "APPROVED", approver: "DIRECTOR", decided_at: "2026-10-18T09:00:00", expires_at: null, consumed_at: null, requested_at: "2026-10-17T12:00:00", idempotency_key: "refund-request" }]);
    mountInvoices();
    fireEvent.click(await screen.findByRole("button", { name: "Thực hiện hoàn tiền đã duyệt" }));
    await waitFor(() => expect(takePayment).toHaveBeenCalledWith(3, { amount: 1000000, method: "CASH", type: "REFUND", reference: "Guest refund" }, "approved-refund"));
    expect(takePayment).toHaveBeenCalledOnce();
  });

  it("shows a failed payment-history request and blocks money actions until it can be reloaded", async () => {
    vi.mocked(api.invoicePayments).mockRejectedValue(new Error("Unavailable"));
    mountInvoices();
    fireEvent.click(await screen.findByRole("button", { name: "Xem / ghi nhận" }));
    await screen.findByRole("button", { name: "Tải lại giao dịch hóa đơn" });
    expect((screen.getByRole("button", { name: "Xin Giám đốc duyệt hoàn tiền" }) as HTMLButtonElement).disabled).toBe(true);
    expect(screen.queryByRole("button", { name: "Lập phiếu thu" })).toBeNull();
    expect(screen.getByText("Chưa tải được")).toBeTruthy();
  });

  it("prints a submitted handover with its recorded discrepancy without confirming or recounting it", () => {
    const confirm = vi.spyOn(api, "confirmCashHandover");
    render(<CashHandoverPanel date="2026-10-17" actor="ACCOUNTING" onSaved={vi.fn()} handovers={[{ id: 8, shift_code: "MORNING", from_actor: "FRONTDESK", to_actor: "ACCOUNTING", expected_amount: 100000, actual_amount: 90000, variance: -10000, handed_over_at: "2026-10-17T13:00:00", note: '<script>alert("x")</script>', denominations: [] }]} />);
    fireEvent.click(screen.getByRole("button", { name: "In biên bản BG-8" }));
    const preview = screen.getByRole("dialog");
    expect(within(preview).getByText("Chờ xác nhận")).toBeTruthy();
    expect(within(preview).getByText(/90\.000/)).toBeTruthy();
    expect(within(preview).getByText('<script>alert("x")</script>')).toBeTruthy();
    expect(preview.querySelector("script")).toBeNull();
    expect(within(preview).queryByRole("spinbutton")).toBeNull();
    expect(confirm).not.toHaveBeenCalled();
  });
});
