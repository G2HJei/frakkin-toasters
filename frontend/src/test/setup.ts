import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach } from 'vitest';

// Without the globals of Vitest, Testing Library doesn't clean up after each test by itself.
afterEach(() => cleanup());
