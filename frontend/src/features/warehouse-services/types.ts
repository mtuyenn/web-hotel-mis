import type { CatalogProposal } from "../../shared/api/catalogProposals";
import type { Approval } from "../../shared/types/hotelOperations";

export type PriceStatus = "pending" | "approved" | "rejected" | "expired" | "consumed";

export interface PriceRequest {
  id: string; item: string; sku: string;
  currentPrice: number; newPrice: number;
  requestedBy: string; requestedAt: string;
  status: PriceStatus; reason?: string;
  rawApproval?: Approval;
  catalogAddition?: boolean;
  catalogKind?: CatalogProposal["kind"];
}
