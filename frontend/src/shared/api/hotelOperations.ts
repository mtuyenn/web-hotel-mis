/**
 * API surface used by hotel operations screens.
 * Endpoint ownership lives in the domain modules below; this object is only a
 * convenient composition for screens that span more than one operational area.
 */
import { approvalsApi, canActivatePrice, canApprove, hasPermission, newApprovalIdempotencyKey, toRows } from "./approvals";
import { financeApi, newFinanceIdempotencyKey } from "./finance";
import { newWarehouseIdempotencyKey, warehouseServicesApi } from "./warehouseServices";

export const hotelOperationsApi = {
  ...warehouseServicesApi,
  ...approvalsApi,
  ...financeApi,
};

export { canActivatePrice, canApprove, hasPermission, toRows };
export const newOperationsIdempotencyKey = newWarehouseIdempotencyKey;
export const newPaymentIdempotencyKey = newFinanceIdempotencyKey;
export const newApprovalKey = newApprovalIdempotencyKey;
