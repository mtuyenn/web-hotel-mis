import { apiClient } from "./client";
import type { OtaReconciliation, VatInvoice, VatInvoiceRequest } from "../types/backoffice";

export const financeBackofficeApi = {
  ota: () => apiClient.request<OtaReconciliation[]>("/api/finance/ota-reconciliation"),
  updateOtaStatus: (reservationId: number, status: string) => apiClient.request<OtaReconciliation>(`/api/finance/ota-reconciliation/${reservationId}`, { method: "PATCH", body: { status } }),
  vatInvoices: () => apiClient.request<VatInvoice[]>("/api/finance/vat-invoices"),
  createVat: (body: VatInvoiceRequest) => apiClient.request<VatInvoice>("/api/finance/vat-invoices", { method: "POST", body }),
  vatXml: async (id: number) => {
    const token = apiClient.store.get()?.access_token;
    const response = await fetch(`${apiClient.baseUrl}/api/finance/vat-invoices/${id}/xml`, { headers: { Accept: "application/xml", ...(token ? { Authorization: `Bearer ${token}` } : {}) } });
    if (!response.ok) throw new Error(`Không thể xuất XML VAT (${response.status})`);
    return response.text();
  },
};
