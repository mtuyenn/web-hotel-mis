const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

// Base URL lấy từ môi trường build; fallback phục vụ frontend chạy cục bộ cùng backend mặc định.
export async function getJson<T>(path: string, accessToken?: string): Promise<T> {
  // Hàm chỉ đọc JSON, đồng thời biến mọi HTTP status ngoài 2xx thành lỗi để caller xử lý.
  const response = await fetch(`${apiBaseUrl}${path}`, {
    headers: { Accept: "application/json", ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}) }
  });

  if (!response.ok) {
    let message = `API request failed: ${response.status}`;
    try {
      const body = (await response.json()) as { message?: string };
      if (body.message) message = body.message;
    } catch {
      // Giữ lỗi HTTP tổng quát nếu server không trả JSON.
    }
    throw new Error(message);
  }

  // Ép kiểu ở biên HTTP vì kiểu T do endpoint caller khai báo, không được runtime kiểm tra.
  return response.json() as Promise<T>;
}

export async function postJson<TRequest, TResponse>(path: string, body: TRequest, accessToken?: string): Promise<TResponse> {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {})
    },
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    let message = `API request failed: ${response.status}`;
    try { const data = (await response.json()) as { message?: string }; if (data.message) message = data.message; } catch { /* keep HTTP fallback */ }
    throw new Error(message);
  }
  return response.json() as Promise<TResponse>;
}
