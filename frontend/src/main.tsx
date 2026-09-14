import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { App } from "./app/App";
import "./styles/global.css";

// Tạo root React duy nhất, bọc ứng dụng trong StrictMode để phát hiện lỗi vòng đời khi phát triển.
createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <App />
  </StrictMode>
);
