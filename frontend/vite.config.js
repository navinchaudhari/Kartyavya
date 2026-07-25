import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Frontend calls ONLY the API Gateway base URL (port 8080). Never call business-service ports directly.
export default defineConfig({
  plugins: [react()],
  server: { port: 5173 },
});
