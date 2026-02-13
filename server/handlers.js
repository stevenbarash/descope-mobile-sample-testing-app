import DescopeClient from '@descope/node-sdk'
import { getPreset } from './public/flow/preset.js'

export async function handleVerify(req, res, pid) {
  const preset = getPreset()
  const projectId = pid ?? preset.projectId
  const baseUrl = process.env.DESCOPE_BASE_URL ?? preset.baseUrl

  console.info(`[verify] projectId=${projectId} baseUrl=${baseUrl}`)

  const { t: token } = req.query
  if (!token) {
    console.warn('[verify] Missing token parameter')
    return res.status(400).json({ error: 'Missing token parameter (t)' })
  }

  try {
    const descopeClient = DescopeClient({ projectId, baseUrl })
    const resp = await descopeClient.enchantedLink.verify(token)
    if (!resp.ok) {
      console.error('[verify] Verification failed:', resp.error)
      return res.status(400).json({
        error: 'Verification failed',
        code: resp.error?.errorCode,
        description: resp.error?.errorDescription,
        message: resp.error?.errorMessage,
      })
    }
    res.json({ success: true, message: 'Enchanted link verified successfully' })
  } catch (error) {
    console.error('[verify] Verification error:', error)
    res.status(500).json({ error: 'Verification error', message: error.message })
  }
}
