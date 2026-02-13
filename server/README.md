# Server

Express server for mobile app authentication — hosts the flow testing page, enchanted link verification, and deep linking configuration files.

## Setup

```bash
# Install dependencies
npm install

# Copy and configure environment variables (optional)
cp .env.example .env

# Start development server (with auto-reload)
npm run dev

# Start production server
npm start
```

## Configuration

Edit `public/flow/preset.js` with your Descope project ID.

Optionally set environment variables (see `.env.example`):
- `PORT` — Server port (default: `3000`)
- `DESCOPE_BASE_URL` — Descope API base URL (default: `https://api.descope.com`)

## Endpoints

- `GET /` — Main page
- `GET /health` — Health check
- `GET /flow` — Authentication flow testing page (configurable via query params)
- `GET /verify?t=TOKEN` — Verify enchanted link (uses default project config)
- `GET /verify/:projectId?t=TOKEN` — Verify with explicit project ID
- `GET /.well-known/apple-app-site-association` — iOS Universal Links config
- `GET /.well-known/assetlinks.json` — Android App Links config

## Deep Linking Configuration

Update the `.well-known` files with your app identifiers:

- **iOS** — Edit `public/.well-known/apple-app-site-association` with your Apple Team ID and Bundle ID
- **Android** — Edit `public/.well-known/assetlinks.json` with your package name and SHA-256 fingerprint

## Testing

```bash
# Run tests once
npm test

# Run tests in watch mode
npm run test:watch
```

## Deployment

**Vercel**: Set Root Directory to `server` in project settings.
