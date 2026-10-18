import { apiClient } from "./client";
import type { Approval, InventoryMovement, InventoryMovementRequest, InventoryReport, PriceChangeRequest, PriceHistory, RestaurantBooking, Service, StockRequest } from "../types/hotelOperations";

const key = () => `WH-${(globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random().toString(36).slice(2)}`).replace(/[^a-zA-Z0-9]/g, "").slice(0, 32)}`;
const query = (params: object) => {
  const value = Object.entries(params).filter(([, item]) => item !== undefined && item !== "").map(([name, item]) => [name, String(item)] as [string, string]);
  const text = new URLSearchParams(value).toString();
  return text ? `?${text}` : "";
};
const requiredKey = (value: string) => {
  const normalized = value?.trim();
  if (!normalized || !/^[A-Za-z0-9][A-Za-z0-9._:-]{0,34}$/.test(normalized)) throw new Error("Mutation requires a valid Idempotency-Key");
  return normalized;
};
const mutation = <T>(path: string, body: unknown, idempotencyKey: string) => apiClient.request<T>(path, { method: "POST", body, idempotencyKey: requiredKey(idempotencyKey) });

export const warehouseServicesApi = {
  services: () => apiClient.request<Service[]>("/api/services"),
  coordinatedBookings: (params: { date?: string; status?: string } = {}) => apiClient.request<RestaurantBooking[]>(`/api/operations/restaurant/service-bookings${query(params)}`),
  markServiceBookingUsed: (id: number) => apiClient.request<RestaurantBooking>(`/api/operations/restaurant/service-bookings/${id}/use`, { method: "POST" }),
  priceRequests: () => apiClient.request<Approval[]>("/api/services/price-requests"),
  lowStock: () => apiClient.request<Service[]>("/api/services/low-stock"),
  inventoryMovements: (serviceId: string) => apiClient.request<InventoryMovement[]>(`/api/services/${encodeURIComponent(serviceId)}/inventory-movements`),
  inventoryReport: (serviceId: string, params: { from?: string; to?: string } = {}) => apiClient.request<InventoryReport>(`/api/services/${encodeURIComponent(serviceId)}/inventory-movements/inventory-report${query(params)}`),
  priceHistory: (serviceId: string) => apiClient.request<PriceHistory[]>(`/api/services/${encodeURIComponent(serviceId)}/price-history`),
  restock: (serviceId: string, body: StockRequest, idempotencyKey: string) => mutation<Service>(`/api/services/${encodeURIComponent(serviceId)}/stock`, body, idempotencyKey),
  recordInventoryMovement: (body: InventoryMovementRequest, idempotencyKey: string) => mutation<InventoryMovement>(`/api/services/${encodeURIComponent(body.service_id)}/inventory-movements`, body, idempotencyKey),
  submitPrice: (serviceId: string, body: PriceChangeRequest, idempotencyKey: string) => mutation<Approval>(`/api/services/${encodeURIComponent(serviceId)}/price/submit`, body, idempotencyKey),
  activatePrice: (serviceId: string, body: PriceChangeRequest, idempotencyKey: string) => mutation<Service>(`/api/services/${encodeURIComponent(serviceId)}/price/activate`, body, idempotencyKey),
};

export const newWarehouseIdempotencyKey = key;
