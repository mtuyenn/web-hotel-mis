/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** URL gốc của backend; Vite thay giá trị này tại thời điểm build. */
  readonly VITE_API_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
