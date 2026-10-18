import { ApiError, apiClient } from "./client";
import type { CashHandover, CashHandoverRequest, DebtSettlement, DebtSettlementRequest, Expense, ExpenseRequest, Invoice, InvoiceAdjustmentRequest, LedgerEntry, LedgerQuery, LedgerPayment, Page, PartnerDebt, PartnerDebtRequest, Payment, PaymentPage, PaymentCreateRequest, PaymentQuery, Receipt, ReceiptQuery, Reconciliation } from "../types/hotelOperations";
import type { OtaReconciliation, VatInvoice } from "../types/backoffice";

const key = () => `FIN-${(globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random().toString(36).slice(2)}`).replace(/[^a-zA-Z0-9]/g, "").slice(0, 32)}`;
const query = (params: object) => { const value = Object.entries(params).filter(([, item]) => item !== undefined && item !== "").map(([name, item]) => [name, String(item)] as [string, string]); const text = new URLSearchParams(value).toString(); return text ? `?${text}` : ""; };
const requiredKey = (value: string) => { const normalized = value?.trim(); if (!normalized || !/^[A-Za-z0-9][A-Za-z0-9._:-]{0,34}$/.test(normalized)) throw new Error("Mutation requires a valid Idempotency-Key"); return normalized; };
const mutation = <T>(path: string, body: unknown, idempotencyKey: string) => apiClient.request<T>(path, { method: "POST", body, idempotencyKey: requiredKey(idempotencyKey) });
interface ApiPage<T> { items: T[]; page: number; size: number; total_elements: number; total_pages: number; }
const normalizePage = <T>(res: ApiPage<T>): Page<T> => {
  if (!res || !Array.isArray(res.items) || !Number.isSafeInteger(res.page) || res.page < 0 || !Number.isSafeInteger(res.size) || res.size <= 0 || !Number.isSafeInteger(res.total_elements) || res.total_elements < 0 || res.total_pages !== Math.ceil(res.total_elements / res.size) || res.items.length !== Math.max(0, Math.min(res.size, res.total_elements - res.page * res.size))) throw new ApiError(502, { code: "INVALID_PAGE_RESPONSE", message: "Phản hồi phân trang không hợp lệ. Vui lòng tải lại." });
  return { items: res.items, page: res.page, size: res.size, totalElements: res.total_elements, totalPages: res.total_pages };
};

export const financeApi = {
  invoices: (params: { status?: string; reservation_id?: number; from?: string; to?: string; page?: number; size?: number } = {}) => apiClient.request<ApiPage<Invoice>>(`/api/invoices${query(params)}`).then(normalizePage<Invoice>),
  payments: (params: PaymentQuery = {}): Promise<PaymentPage> => apiClient.request<ApiPage<LedgerPayment> & { method_counts: PaymentPage["methodCounts"] }>(`/api/finance/payments${query(params)}`).then(res => { const page = normalizePage(res); if (!res.method_counts || typeof res.method_counts !== "object" || Array.isArray(res.method_counts) || Object.values(res.method_counts).some(count => !Number.isSafeInteger(count) || (count ?? -1) < 0)) throw new ApiError(502, { code: "INVALID_PAGE_RESPONSE", message: "Phản hồi thống kê phương thức thanh toán không hợp lệ." }); return { ...page, methodCounts: res.method_counts }; }),
  invoicePayments: (invoiceId: number) => apiClient.request<Payment[] | Page<Payment>>(`/api/invoices/${invoiceId}/payments`),
  receipts: (params: ReceiptQuery = {}) => apiClient.request<Page<Receipt>>(`/api/finance/receipts${query(params)}`),
  invoiceReceipts: (invoiceId: number) => apiClient.request<Receipt[] | Page<Receipt>>(`/api/invoices/${invoiceId}/receipts`),
  recordPayment: (invoiceId: number, body: PaymentCreateRequest, idempotencyKey: string) => mutation<Payment>(`/api/invoices/${invoiceId}/payments`, body, idempotencyKey),
  adjustInvoice: (invoiceId: number, body: InvoiceAdjustmentRequest, idempotencyKey: string) => mutation<Invoice>(`/api/invoices/${invoiceId}/adjust`, body, idempotencyKey),
  expenses: () => apiClient.request<Expense[]>("/api/finance/expenses"),
  partnerDebts: () => apiClient.request<PartnerDebt[]>("/api/finance/partner-debts"),
  settlements: (id: number) => apiClient.request<DebtSettlement[]>(`/api/finance/partner-debts/${id}/settlements`),
  confirmCashHandover: (id: number) => apiClient.request<void>(`/api/finance/cash-handovers/${id}/confirm`, { method: "POST" }),
  cashHandovers: () => apiClient.request<CashHandover[]>("/api/finance/cash-handovers"),
  cashBalance: () => apiClient.request<import("../types/hotelOperations").CashBalance>("/api/finance/cash-handovers/balance"),
  cashRecipients: () => apiClient.request<import("../types/hotelOperations").CashRecipient[]>("/api/finance/cash-handovers/recipients"),
  refundRequests: () => apiClient.request<import("../types/hotelOperations").Approval[]>("/api/finance/refund-requests"),
  requestRefund: (invoiceId: number, body: PaymentCreateRequest, paymentKey: string, requestKey: string) => apiClient.request<import("../types/hotelOperations").Approval>("/api/governance/approvals", { method: "POST", body: { action: "PAYMENT_REFUND", target_id: String(invoiceId), amount: body.amount, reason: body.reference, payload: JSON.stringify({ invoice_id: invoiceId, idempotency_key: requiredKey(paymentKey), method: body.method, type: "REFUND", reference: body.reference ?? "" }), idempotency_key: requiredKey(requestKey) } }),
  recordCashHandover: (body: CashHandoverRequest, idempotencyKey: string) => mutation<CashHandover>("/api/finance/cash-handovers", body, idempotencyKey),
  ota: () => apiClient.request<OtaReconciliation[]>("/api/finance/ota-reconciliation"),
  vatInvoices: () => apiClient.request<VatInvoice[]>("/api/finance/vat-invoices"),
  recordExpense: (body: ExpenseRequest, idempotencyKey: string) => mutation<Expense>("/api/finance/expenses", body, idempotencyKey),
  recordPartnerDebt: (body: PartnerDebtRequest, idempotencyKey: string) => mutation<PartnerDebt>("/api/finance/partner-debts", body, idempotencyKey),
  settlePartnerDebt: (id: number, body: DebtSettlementRequest, idempotencyKey: string) => mutation<PartnerDebt>(`/api/finance/partner-debts/${id}/settle`, body, idempotencyKey),
  ledger: (params: LedgerQuery = {}) => apiClient.request<ApiPage<LedgerEntry>>(`/api/finance/ledger${query(params)}`).then(normalizePage<LedgerEntry>),
  reconciliation: (params: { from?: string; to?: string } = {}) => apiClient.request<Reconciliation>(`/api/finance/reconciliation${query(params)}`),
  issueReceipt: (invoiceId: number, body: { receipt_number: string; amount: number; method: Receipt["method"] }, idempotencyKey: string) => mutation<Receipt>(`/api/invoices/${invoiceId}/receipts`, body, idempotencyKey),
};
export const newFinanceIdempotencyKey = key;
