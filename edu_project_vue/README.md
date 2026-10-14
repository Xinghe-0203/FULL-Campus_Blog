# Frontend

Vue 3 + TypeScript single-page application built with Vite. Use Node.js 22.13+ or 24+ (`engine-strict` is enabled).

## Commands

```sh
npm ci
npm run dev
npm run lint:check
npm run format:check
npm run type-check
npm run test:run
npm run test:coverage
npm run test:ui
npm run build
```

The dev server listens on port 3000. `VITE_API_TARGET` controls its `/api` proxy and the `/uploads` to `/api/uploads` rewrite. Axios uses `/api` directly; production proxying is configured separately in `vercel.json` or your reverse proxy.

## Structure

- `src/api/`: shared Axios client and domain API modules
- `src/router/`: route modules and navigation guards
- `src/stores/`: Pinia state
- `src/components/`, `src/views/`: reusable UI and pages
- `src/styles/`: global style entrypoint and design tokens
- `src/utils/`: shared utilities

Tests use Vitest + jsdom and load `src/test/setup.ts`. `npm run build` runs the TypeScript check before Vite. See the root [AGENTS.md](../AGENTS.md) for integration constraints and verified setup details.
