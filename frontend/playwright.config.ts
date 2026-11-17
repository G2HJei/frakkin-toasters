import { defineConfig, devices } from '@playwright/test';

// Runs against the application with the dev profile: ./gradlew :backend:bootRun --args='--spring.profiles.active=dev'
// (or the Vite dev server, E2E_BASE_URL=http://localhost:5173).
export default defineConfig({
  testDir: 'e2e',
  timeout: 60_000,
  fullyParallel: false,
  workers: 1,
  reporter: [['list']],
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:8080',
    trace: 'retain-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
});
