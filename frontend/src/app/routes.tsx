// Keep route definitions in one place so auth guards and permission boundaries
// can be applied consistently as features are added.
export const appRoutes = [
  "/rooms",
  "/guests",
  "/reservations",
  "/billing",
  "/chat"
] as const;
