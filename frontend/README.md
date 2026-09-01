# PlantAssure Frontend

Vue 3 and TypeScript frontend for PlantAssure, built with Vite, Vuetify, Vue Router,
Pinia, Axios, Vitest, and Playwright.

## Requirements

- A Node.js version matching `package.json`
- pnpm 10

## Commands

```sh
pnpm install
pnpm dev
pnpm type-check
pnpm lint
pnpm test:unit -- --run
pnpm test:e2e
pnpm build
```

See `DESIGN.md` for UI rules and `API.md` for the frontend/backend contract.

Safe shared environment defaults may be committed in `.env` if the team chooses. Secrets must
use ignored `.env.local` or `.env.*.local` files.
