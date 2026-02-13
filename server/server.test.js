import { describe, it, expect } from 'vitest'
import request from 'supertest'
import app from './server.js'

describe('Server', () => {
  describe('GET /health', () => {
    it('returns ok status', async () => {
      const response = await request(app).get('/health')
      expect(response.status).toBe(200)
      expect(response.body).toEqual({ status: 'ok' })
    })
  })

  describe('GET /', () => {
    it('serves index.html', async () => {
      const response = await request(app).get('/')
      expect(response.status).toBe(200)
      expect(response.type).toBe('text/html')
    })
  })

  describe('GET /.well-known/apple-app-site-association', () => {
    it('returns JSON with correct content-type', async () => {
      const response = await request(app).get('/.well-known/apple-app-site-association')
      expect(response.status).toBe(200)
      expect(response.type).toBe('application/json')
      expect(response.body).toHaveProperty('applinks')
    })
  })

  describe('GET /.well-known/assetlinks.json', () => {
    it('returns JSON with correct content-type', async () => {
      const response = await request(app).get('/.well-known/assetlinks.json')
      expect(response.status).toBe(200)
      expect(response.type).toBe('application/json')
      expect(Array.isArray(response.body)).toBe(true)
    })
  })

  describe('GET /flow', () => {
    it('redirects to /flow/', async () => {
      const response = await request(app).get('/flow')
      expect([301, 302]).toContain(response.status)
      expect(response.headers.location).toBe('/flow/')
    })

    it('serves index.html at /flow/', async () => {
      const response = await request(app).get('/flow/')
      expect(response.status).toBe(200)
      expect(response.type).toBe('text/html')
    })
  })

  describe('GET /verify', () => {
    it('returns 400 when token is missing', async () => {
      const response = await request(app).get('/verify')
      expect(response.status).toBe(400)
      expect(response.body).toHaveProperty('error', 'Missing token parameter (t)')
    })

    it('returns error for invalid token', async () => {
      const response = await request(app).get('/verify?t=invalid-token')
      expect([400, 500]).toContain(response.status)
      expect(response.body).toHaveProperty('error')
    })
  })

  describe('GET /verify/:projectId', () => {
    it('returns 400 when token is missing', async () => {
      const response = await request(app).get('/verify/test-project-id')
      expect(response.status).toBe(400)
      expect(response.body).toHaveProperty('error', 'Missing token parameter (t)')
    })

    it('returns error for invalid token', async () => {
      const response = await request(app).get('/verify/test-project-id?t=invalid-token')
      expect([400, 500]).toContain(response.status)
      expect(response.body).toHaveProperty('error')
    })
  })
})
