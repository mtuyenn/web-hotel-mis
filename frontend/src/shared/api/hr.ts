import { apiClient } from "./client";
import type { AttendanceInput, AttendanceRecord, LeaveRequest, LeaveRequestInput } from "../types/backoffice";

export const hrApi = {
  attendance: (date?: string) => apiClient.request<AttendanceRecord[]>(`/api/hr/attendance${date ? `?date=${encodeURIComponent(date)}` : ""}`),
  importAttendance: (records: AttendanceInput[]) => apiClient.request<AttendanceRecord[]>("/api/hr/attendance/import", { method: "POST", body: { records } }),
  leaves: (status?: string) => apiClient.request<LeaveRequest[]>(`/api/hr/leave-requests${status ? `?status=${encodeURIComponent(status)}` : ""}`),
  createLeave: (body: LeaveRequestInput) => apiClient.request<LeaveRequest>("/api/hr/leave-requests", { method: "POST", body }),
  approveLeave: (id: number) => apiClient.request<LeaveRequest>(`/api/hr/leave-requests/${id}/approve`, { method: "POST" }),
  rejectLeave: (id: number) => apiClient.request<LeaveRequest>(`/api/hr/leave-requests/${id}/reject`, { method: "POST" }),
};
