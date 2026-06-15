import { chromium } from 'playwright-core'
import { existsSync, mkdirSync } from 'fs'
import { dirname, join } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const USER_DATA_DIR = join(__dirname, '..', '.browser-profile')
const DOWNLOADS_DIR = join(__dirname, '..', '.downloads')

function ensureDirs() {
  for (const d of [USER_DATA_DIR, DOWNLOADS_DIR]) {
    if (!existsSync(d)) mkdirSync(d, { recursive: true })
  }
}

async function resolveExecutable() {
  const candidates = [
    join(process.env['LOCALAPPDATA'] || '', 'ms-playwright', 'chromium-*', 'chrome-win', 'chrome.exe'),
    join(process.env['PROGRAMFILES'] || '', 'Google', 'Chrome', 'Application', 'chrome.exe'),
    join(process.env['PROGRAMFILES(X86)'] || '', 'Google', 'Chrome', 'Application', 'chrome.exe'),
    join(process.env['LOCALAPPDATA'] || '', 'Google', 'Chrome', 'Application', 'chrome.exe'),
    join(process.env['PROGRAMFILES'] || '', 'Microsoft', 'Edge', 'Application', 'msedge.exe'),
    join(process.env['PROGRAMFILES(X86)'] || '', 'Microsoft', 'Edge', 'Application', 'msedge.exe'),
    join(process.env['LOCALAPPDATA'] || '', 'Microsoft', 'Edge', 'Application', 'msedge.exe')
  ]
  for (const c of candidates) {
    if (existsSync(c.replace(/\*/g, ''))) {
      return c.replace(/\*/g, '')
    }
  }
  return undefined
}

export async function launchBrowser(options = {}) {
  ensureDirs()
  const executablePath = options.executablePath || (await resolveExecutable())
  const headless = options.headless !== undefined ? options.headless : true
  const browser = await chromium.launch({
    executablePath,
    headless,
    downloadsPath: DOWNLOADS_DIR,
    args: [
      '--disable-dev-shm-usage',
      '--no-sandbox',
      '--disable-setuid-sandbox',
      '--disable-blink-features=AutomationControlled'
    ]
  })
  return browser
}

export async function newPage(browser, options = {}) {
  const context = await browser.newContext({
    viewport: options.viewport || { width: 1280, height: 900 },
    userAgent: options.userAgent || 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36',
    acceptDownloads: true,
    ignoreHTTPSErrors: true
  })
  const page = await context.newPage()
  page.setDefaultTimeout(options.timeout || 30000)
  page.setDefaultNavigationTimeout(options.navigationTimeout || 60000)
  return { context, page }
}

export async function takeScreenshot(page, path, options = {}) {
  await page.screenshot({ path, fullPage: options.fullPage !== false })
}

export async function closeBrowser(browser) {
  if (browser && browser.close) await browser.close()
}

if (process.argv[1] && process.argv[1].endsWith('browser-client.mjs')) {
  const [action, ...args] = process.argv.slice(2)
  if (action === 'screenshot' && args.length >= 2) {
    const [url, outPath] = args
    const browser = await launchBrowser({ headless: true })
    const { context, page } = await newPage(browser)
    try {
      await page.goto(url, { waitUntil: 'domcontentloaded' })
      await page.waitForTimeout(2000)
      await takeScreenshot(page, outPath)
      console.log('Screenshot saved to', outPath)
    } finally {
      await context.close()
      await closeBrowser(browser)
    }
  } else if (action === 'check') {
    const exe = await resolveExecutable()
    console.log('Browser executable:', exe || 'NOT FOUND')
  } else {
    console.log('Usage: node browser-client.mjs <screenshot <url> <outPath>|check>')
  }
}

export default { launchBrowser, newPage, takeScreenshot, closeBrowser }
