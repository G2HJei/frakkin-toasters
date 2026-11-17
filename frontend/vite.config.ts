/// <reference types="vitest/config" />
import { fileURLToPath } from 'node:url';
import { defineConfig, searchForWorkspaceRoot } from 'vite';
import react from '@vitejs/plugin-react';

// In dev, the backend runs on 8080 with the dev profile, which lets this origin open the game socket.
const backend = 'http://localhost:8080';

// The images live outside the app, at the repository's root (src/art). The build copies them, and the dev server
// may read them.
const materials = fileURLToPath(new URL('../materials-sorted', import.meta.url));

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    fs: {
      allow: [searchForWorkspaceRoot(process.cwd()), materials],
    },
    proxy: {
      '/api': backend,
      '/ws': { target: backend, ws: true },
      '/oauth2': backend,
      '/login/oauth2': backend,
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    include: ['src/**/*.test.{ts,tsx}', 'scripts/**/*.test.mjs'],
  },
});
