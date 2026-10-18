import { apiClient } from "./client";
import type { Approval, ApprovalStatus, Page } from "../types/hotelOperations";

const key = () => `APR-${(globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random().toString(36).slice(2)}`).replace(/[^a-zA-Z0-9]/g, "").slice(0, 32)}`;
const query = (params: object) => { const value = Object.entries(params).filter(([, item]) => item !== undefined && item !== "").map(([name, item]) => [name, String(item)] as [string, string]); const text = new URLSearchParams(value).toString(); return text ? `?${text}` : ""; };
const mutation = <T>(path: string, idempotencyKey: string) => apiClient.request<T>(path, { method: "POST", idempotencyKey: idempotencyKey.trim() });
export const approvalsApi = {
  approvals: (status: ApprovalStatus = "PENDING") => apiClient.request<Approval[] | Page<Approval>>(`/api/governance/approvals${query({ status })}`),
  approve: (id: number, idempotencyKey: string) => mutation<Approval>(`/api/governance/approvals/${id}/approve`, idempotencyKey),
  reject: (id: number, idempotencyKey: string) => mutation<Approval>(`/api/governance/approvals/${id}/reject`, idempotencyKey),
};
export const hasPermission = (permissions: string[] | undefined, permission: string) => Boolean(permissions?.includes(permission));
export const canApprove = (approval: Pick<Approval, "status" | "requester">, permissions: string[] | undefined, actorId: string | undefined) => approval.status === "PENDING" && Boolean(actorId) && approval.requester !== actorId && hasPermission(permissions, "APPROVAL_APPROVE");
export const canActivatePrice = (approval: Pick<Approval, "status" | "requester" | "action" | "target_id">, serviceId: string, permissions: string[] | undefined, actorId: string | undefined) => approval.status === "APPROVED" && approval.action === "SERVICE_PRICE_CHANGE" && approval.target_id === serviceId && Boolean(actorId) && approval.requester !== actorId && hasPermission(permissions, "SERVICE_PRICE_ACTIVATE");
export const toRows = <T>(value: T[] | Page<T> | undefined): T[] => Array.isArray(value) ? value : value?.items ?? [];
export const newApprovalIdempotencyKey = key;
