import express from 'express'
import path from 'path'
import { fileURLToPath } from 'url'
import { handleVerify } from './handlers.js'
import 'dotenv/config'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

console.log('[server] __dirname:', __dirname)
console.log('[server] public path:', path.join(__dirname, 'public'))

const app = express()
const PORT = process.env.PORT || 3000

app.set('trust proxy', 1)
app.use(express.json())
app.use((req, res, next) => {
  console.log('[server] request:', req.method, req.path)
  next()
})

// Serve .well-known files with JSON content-type (required for app linking)
app.use('/.well-known', express.static(path.join(__dirname, 'public/.well-known'), {
  setHeaders: (res) => res.setHeader('Content-Type', 'application/json')
}))

// Serve static files
app.use(express.static(path.join(__dirname, 'public')))

// Health check endpoint
app.get('/health', (_, res) => {
  res.json({ status: 'ok' })
})

// /verify - use default project config
app.get('/verify', (req, res) => handleVerify(req, res))

// /verify/:projectId - explicit project ID
app.get('/verify/:projectId', (req, res) => {
  return handleVerify(req, res, req.params.projectId)
})

// Start server only when run directly (not imported for testing)
if (process.argv[1] === __filename) {
  app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`)
  })
}

export default (req, res) => app(req, res)
