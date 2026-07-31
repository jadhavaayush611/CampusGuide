/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string;
  readonly API_BASE_URL?: string;
  readonly VITE_APP_NAME?: string;
  readonly APP_NAME?: string;
  readonly VITE_APP_VERSION?: string;
  readonly APP_VERSION?: string;
  readonly VITE_ENABLE_DEBUG?: string;
  readonly ENABLE_DEBUG?: string;
  readonly VITE_ENABLE_ANALYTICS?: string;
  readonly ENABLE_ANALYTICS?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

declare module "lucide-react";
