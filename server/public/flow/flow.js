import { getPreset } from './preset.js'

export function getConfig() {
  const params = new URLSearchParams(window.location.search)
  const preset = getPreset()
  return {
    projectId: params.get('projectId') ?? preset.projectId,
    baseUrl: params.get('baseUrl') ?? preset.baseUrl,
    contentUrl: params.get('contentUrl') ?? preset.contentUrl,
    flowId: params.get('flowId') ?? 'sign-up-or-in',
    theme: params.get('theme') ?? 'os',
    tenant: params.get('tenant'),
    locale: params.get('locale'),
    debug: params.get('debug'),
    styleId: params.get('styleId'),
    redirectUrl: params.get('redirectUrl'),
  }
}

export function addComponent() {
  const config = getConfig()
  const component = document.createElement('descope-wc')
  component.setAttribute('project-id', config.projectId)
  component.setAttribute('base-url', config.baseUrl)
  component.setAttribute('base-static-url', config.contentUrl)
  if (config.flowId) component.setAttribute('flow-id', config.flowId)
  if (config.theme) component.setAttribute('theme', config.theme)
  if (config.tenant) component.setAttribute('tenant', config.tenant)
  if (config.locale) component.setAttribute('locale', config.locale)
  if (config.debug) component.setAttribute('debug', config.debug)
  if (config.styleId) component.setAttribute('style-id', config.styleId)
  if (config.redirectUrl) component.setAttribute('redirect-url', config.redirectUrl)

  component.addEventListener('success', (e) => {
    console.info('[Event] Success:', e.detail)
  })

  component.addEventListener('error', (e) => {
    console.error('[Event] Error:', e.detail)
  })

  document.querySelector('.login-container').appendChild(component)

  document.addEventListener('visibilitychange', () => {
    console.debug(`[Event] Document ${document.hidden ? 'hidden' : 'visible'}`)
  })
}

export function addFooter() {
  const sdkConfig = window.customElements?.get('descope-wc')?.sdkConfigOverrides || {}
  const sdkHeaders = sdkConfig?.baseHeaders || {}
  const sdkName = sdkHeaders['x-descope-sdk-name'] || 'unknown'
  const sdkVersion = sdkHeaders['x-descope-sdk-version'] || 'unknown'
  console.debug(`Descope ${sdkName} version "${sdkVersion}"`)
  document.querySelector('.sdk-info').textContent = `${sdkName} v${sdkVersion}`
}
