import { apiClient } from "./client";

export const employeeProvisioningApi = {
  autoProvisionEmployee: (body: { full_name: string; role: string; phone: string; email: string; address?: string }) => apiClient.request<{ employee_id: string; full_name: string; role: string; phone: string; email: string; temporary_password: string; must_change_password: boolean }>("/api/auth/employees/auto-provision", { method: "POST", body }),
  changeOwnPassword: (password: string) => apiClient.request<void>("/api/auth/me/password", { method: "POST", body: { password } }),
};
