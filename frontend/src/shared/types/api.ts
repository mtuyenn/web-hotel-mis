export type ApiError = {
  timestamp: string;
  status: number;
  code: string;
  message: string;
  details: string[];
};
