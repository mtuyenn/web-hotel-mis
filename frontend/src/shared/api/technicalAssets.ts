import { apiClient } from "./client";
import type { TechnicalAsset, TechnicalAssetInput } from "../types/backoffice";

export const technicalAssetsApi = {
  assets: () => apiClient.request<TechnicalAsset[]>("/api/operations/assets"),
  createAsset: (body: TechnicalAssetInput) => apiClient.request<TechnicalAsset>("/api/operations/assets", { method: "POST", body }),
  updateAssetStatus: (id: string, status: string) => apiClient.request<TechnicalAsset>(`/api/operations/assets/${encodeURIComponent(id)}/status`, { method: "PATCH", body: { status } }),
};
