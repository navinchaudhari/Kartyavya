import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const frontendDirectory = path.dirname(fileURLToPath(import.meta.url));
const projectRoot = path.resolve(frontendDirectory, '..');

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, projectRoot, '');
  return {
    plugins: [react()],
    envDir: projectRoot,
    server: {
      host: env.FRONTEND_HOST || 'localhost',
      port: Number(env.FRONTEND_PORT || 5173),
      open: true,
    },
  };
});
