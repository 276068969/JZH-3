import { spawn, exec } from 'child_process'
import { existsSync, readdirSync } from 'fs'
import { dirname, resolve } from 'path'
import { fileURLToPath } from 'url'
import { createServer, request } from 'http'

const __dirname = dirname(fileURLToPath(import.meta.url))
const rootDir = resolve(__dirname)
const DEFAULT_BACKEND_PORT = 8080
const FALLBACK_BACKEND_PORT = 8081
const FRONTEND_PORT = 5173

function findExecutable(names) {
  return new Promise((res) => {
    const where = process.platform === 'win32' ? 'where' : 'which'
    let found = false
    let pending = names.length
    for (const name of names) {
      exec(`${where} ${name}`, (err, stdout) => {
        if (!found && !err && stdout && stdout.trim()) {
          found = true
          const line = stdout.trim().split(/\r?\n/)[0].trim()
          if (line) return res(line)
        }
        if (--pending === 0 && !found) res(null)
      })
    }
    setTimeout(() => { if (!found) res(null) }, 4000)
  })
}

function httpPing(port, path = '/api/pollutant-limit-rules/fuel-types') {
  return new Promise((res) => {
    const req = request({ hostname: '127.0.0.1', port, path, method: 'GET', timeout: 1500 }, (r) => {
      let d = ''
      r.on('data', c => d += c)
      r.on('end', () => res({ status: r.statusCode || 0, body: d }))
    })
    req.on('error', () => res({ status: 0, body: '' }))
    req.on('timeout', () => { req.destroy(); res({ status: 0, body: '' }) })
    req.end()
  })
}

async function detectExistingBackend() {
  const r1 = await httpPing(DEFAULT_BACKEND_PORT)
  if (r1.status >= 200 && r1.status < 500 && r1.body.includes('汽油')) {
    return { port: DEFAULT_BACKEND_PORT, running: true }
  }
  const r2 = await httpPing(FALLBACK_BACKEND_PORT)
  if (r2.status >= 200 && r2.status < 500 && r2.body.includes('汽油')) {
    return { port: FALLBACK_BACKEND_PORT, running: true }
  }
  return { port: 0, running: false }
}

async function findMaven() {
  const fromPath = await findExecutable(['mvn', 'mvn.cmd', 'mvn.bat'])
  if (fromPath) return fromPath
  if (process.env['MAVEN_HOME']) {
    const p = resolve(process.env['MAVEN_HOME'], 'bin', 'mvn.cmd')
    if (existsSync(p)) return p
  }
  if (process.env['M2_HOME']) {
    const p = resolve(process.env['M2_HOME'], 'bin', 'mvn.cmd')
    if (existsSync(p)) return p
  }
  const wrapper = resolve(rootDir, 'backend', 'mvnw.cmd')
  const wrapperJar = resolve(rootDir, 'backend', '.mvn', 'wrapper', 'maven-wrapper.jar')
  if (existsSync(wrapper) && existsSync(wrapperJar)) return wrapper
  if (existsSync(wrapper)) return wrapper
  const home = process.env['USERPROFILE'] || ''
  const candidates = [
    'C:\\Program Files\\Apache\\maven\\bin\\mvn.cmd',
    'C:\\Program Files\\Apache Software Foundation\\maven\\bin\\mvn.cmd',
    'C:\\Program Files\\Apache Software Foundation\\maven-3.9.9\\bin\\mvn.cmd',
    'C:\\Program Files\\Apache Software Foundation\\maven-3.9.8\\bin\\mvn.cmd',
    'C:\\Program Files\\Apache Software Foundation\\maven-3.9.7\\bin\\mvn.cmd',
    'C:\\Program Files\\Apache Software Foundation\\maven-3.9.6\\bin\\mvn.cmd',
    'C:\\Program Files\\Apache Software Foundation\\maven-3.9.5\\bin\\mvn.cmd',
    'C:\\Program Files\\Apache Software Foundation\\maven-3.9.4\\bin\\mvn.cmd',
    'C:\\Program Files\\Apache Software Foundation\\maven-3.9.3\\bin\\mvn.cmd',
    'C:\\Program Files\\Apache Software Foundation\\maven-3.9.2\\bin\\mvn.cmd',
    'C:\\Program Files\\Apache Software Foundation\\maven-3.9.1\\bin\\mvn.cmd',
    'C:\\Program Files\\Apache Software Foundation\\maven-3.9.0\\bin\\mvn.cmd',
    'C:\\Program Files\\Apache Software Foundation\\maven-3.8.8\\bin\\mvn.cmd',
    'C:\\Program Files\\Apache Software Foundation\\maven-3.8.7\\bin\\mvn.cmd',
    'C:\\Program Files\\Apache Software Foundation\\maven-3.8.6\\bin\\mvn.cmd',
    'C:\\Program Files\\Apache Software Foundation\\maven-3.8.5\\bin\\mvn.cmd',
    'C:\\ProgramData\\chocolatey\\lib\\maven\\bin\\mvn.cmd',
    'C:\\ProgramData\\chocolatey\\lib\\maven\\tools\\apache-maven\\bin\\mvn.cmd',
    `${home}\\scoop\\apps\\maven\\current\\bin\\mvn.cmd`,
    `${home}\\.sdkman\\candidates\\maven\\current\\bin\\mvn.cmd`,
    'C:\\Program Files\\JetBrains\\IntelliJ IDEA Ultimate\\plugins\\maven\\lib\\maven3\\bin\\mvn.cmd',
    'C:\\Program Files\\JetBrains\\IntelliJ IDEA Community\\plugins\\maven\\lib\\maven3\\bin\\mvn.cmd',
    'C:\\Program Files\\JetBrains\\IntelliJ IDEA\\plugins\\maven\\lib\\maven3\\bin\\mvn.cmd',
    'C:\\Program Files (x86)\\JetBrains\\IntelliJ IDEA Ultimate\\plugins\\maven\\lib\\maven3\\bin\\mvn.cmd',
    'C:\\Program Files (x86)\\JetBrains\\IntelliJ IDEA Community\\plugins\\maven\\lib\\maven3\\bin\\mvn.cmd',
    `${home}\\AppData\\Local\\JetBrains\\Toolbox\\apps\\IDEA-U\\ch-0\\plugins\\maven\\lib\\maven3\\bin\\mvn.cmd`,
    `${home}\\AppData\\Local\\JetBrains\\Toolbox\\apps\\IDEA-C\\ch-0\\plugins\\maven\\lib\\maven3\\bin\\mvn.cmd`,
    'C:\\Program Files\\NetBeans\\java\\maven\\bin\\mvn.cmd',
    `${home}\\.m2\\wrapper\\dists\\apache-maven-3.9.9-bin\\*\\apache-maven-3.9.9\\bin\\mvn.cmd`
  ]
  for (const p of candidates) {
    if (existsSync(p)) return p
  }
  return null
}

async function findJava() {
  const fromPath = await findExecutable(['java', 'java.exe'])
  if (fromPath) return fromPath
  if (process.env['JAVA_HOME'] && existsSync(resolve(process.env['JAVA_HOME'], 'bin', 'java.exe'))) {
    return resolve(process.env['JAVA_HOME'], 'bin', 'java.exe')
  }
  if (process.env['JDK_HOME'] && existsSync(resolve(process.env['JDK_HOME'], 'bin', 'java.exe'))) {
    return resolve(process.env['JDK_HOME'], 'bin', 'java.exe')
  }
  const home = process.env['USERPROFILE'] || ''
  const candidates = [
    'C:\\Program Files\\Java\\jdk-23\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk-22\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk-21\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk-20\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk-19\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk-18\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk-17\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk-16\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk-15\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk-14\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk-13\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk-12\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk-11\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk1.8.0_301\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk1.8.0_291\\bin\\java.exe',
    'C:\\Program Files\\Eclipse Adoptium\\jdk-23.0.0.37-hotspot\\bin\\java.exe',
    'C:\\Program Files\\Eclipse Adoptium\\jdk-22.0.0.36-hotspot\\bin\\java.exe',
    'C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.0.35-hotspot\\bin\\java.exe',
    'C:\\Program Files\\Eclipse Adoptium\\jdk-20.0.0.36-hotspot\\bin\\java.exe',
    'C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.0.35-hotspot\\bin\\java.exe',
    'C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.0.35\\bin\\java.exe',
    'C:\\Program Files\\Eclipse Adoptium\\jdk-11.0.0.28-hotspot\\bin\\java.exe',
    'C:\\Program Files\\Eclipse Adoptium\\jdk-11.0.0.28\\bin\\java.exe',
    'C:\\Program Files\\Eclipse Adoptium\\temurin-21\\bin\\java.exe',
    'C:\\Program Files\\Eclipse Adoptium\\temurin-17\\bin\\java.exe',
    'C:\\Program Files\\Eclipse Adoptium\\temurin-11\\bin\\java.exe',
    'C:\\Program Files\\Microsoft\\jdk-23.0.1.11-hotspot\\bin\\java.exe',
    'C:\\Program Files\\Microsoft\\jdk-21.0.2.12-hotspot\\bin\\java.exe',
    'C:\\Program Files\\Microsoft\\jdk-17.0.7.7-hotspot\\bin\\java.exe',
    'C:\\Program Files\\Microsoft\\jdk-11.0.25.9-hotspot\\bin\\java.exe',
    'C:\\Program Files\\Zulu\\zulu-23\\bin\\java.exe',
    'C:\\Program Files\\Zulu\\zulu-21\\bin\\java.exe',
    'C:\\Program Files\\Zulu\\zulu-17\\bin\\java.exe',
    'C:\\Program Files\\Zulu\\zulu-11\\bin\\java.exe',
    'C:\\Program Files\\Zulu\\zulu-8\\bin\\java.exe',
    'C:\\Program Files\\BellSoft\\LibericaJDK-21\\bin\\java.exe',
    'C:\\Program Files\\BellSoft\\LibericaJDK-17\\bin\\java.exe',
    'C:\\Program Files\\BellSoft\\LibericaJDK-11\\bin\\java.exe',
    'C:\\Program Files\\Amazon Corretto\\jdk23.0.1_11\\bin\\java.exe',
    'C:\\Program Files\\Amazon Corretto\\jdk21.0.2_13\\bin\\java.exe',
    'C:\\Program Files\\Amazon Corretto\\jdk17.0.7_7\\bin\\java.exe',
    'C:\\Program Files\\Amazon Corretto\\jdk11.0.19_7\\bin\\java.exe',
    'C:\\Program Files\\SapMachine\\sapmachine-21\\bin\\java.exe',
    'C:\\Program Files\\SapMachine\\sapmachine-17\\bin\\java.exe',
    'C:\\Program Files\\SapMachine\\sapmachine-11\\bin\\java.exe',
    'C:\\Program Files\\GraalVM\\graalvm-jdk-21\\bin\\java.exe',
    'C:\\Program Files\\GraalVM\\graalvm-jdk-17\\bin\\java.exe',
    'C:\\Program Files (x86)\\Java\\jdk-21\\bin\\java.exe',
    'C:\\Program Files (x86)\\Java\\jdk-17\\bin\\java.exe',
    'C:\\Program Files (x86)\\Eclipse Adoptium\\jdk-21\\bin\\java.exe',
    'C:\\Program Files (x86)\\Eclipse Adoptium\\jdk-17\\bin\\java.exe',
    `${home}\\scoop\\apps\\openjdk\\current\\bin\\java.exe`,
    `${home}\\scoop\\apps\\temurin-jdk\\current\\bin\\java.exe`,
    `${home}\\scoop\\apps\\oraclejdk\\current\\bin\\java.exe`,
    `${home}\\scoop\\apps\\zulu-jdk\\current\\bin\\java.exe`,
    `${home}\\.sdkman\\candidates\\java\\current\\bin\\java.exe`,
    `${home}\\.jdks\\corretto-21\\bin\\java.exe`,
    `${home}\\.jdks\\corretto-17\\bin\\java.exe`,
    `${home}\\.jdks\\temurin-21\\bin\\java.exe`,
    `${home}\\.jdks\\temurin-17\\bin\\java.exe`,
    `${home}\\.jdks\\openjdk-21\\bin\\java.exe`,
    `${home}\\.jdks\\openjdk-17\\bin\\java.exe`,
    `${home}\\.gradle\\jdks\\jdk-21\\bin\\java.exe`,
    `${home}\\.gradle\\jdks\\jdk-17\\bin\\java.exe`,
    `${home}\\AppData\\Local\\Programs\\Eclipse Adoptium\\jdk-21\\bin\\java.exe`,
    `${home}\\AppData\\Local\\Programs\\Eclipse Adoptium\\jdk-17\\bin\\java.exe`,
    `${home}\\AppData\\Local\\Programs\\Java\\jdk-21\\bin\\java.exe`,
    `${home}\\AppData\\Local\\Programs\\Java\\jdk-17\\bin\\java.exe`,
    `${home}\\AppData\\Local\\JetBrains\\Toolbox\\apps\\IDEA-U\\ch-0\\jbr\\bin\\java.exe`,
    `${home}\\AppData\\Local\\JetBrains\\Toolbox\\apps\\IDEA-C\\ch-0\\jbr\\bin\\java.exe`
  ]
  for (const p of candidates) {
    if (existsSync(p)) return p
  }
  return null
}

async function startMockBackend(onlyBackend = false) {
  const mockScript = resolve(rootDir, 'mock-backend-and-start.mjs')
  if (!existsSync(mockScript)) {
    console.error('[mock] Mock backend script not found:', mockScript)
    return null
  }
  const node = await findExecutable(['node', 'node.exe'])
  if (!node) {
    console.error('[mock] Node.js not found, cannot start mock backend')
    return null
  }
  const args = [mockScript, '--backend-only', '--force-mock']
  const proc = spawn(node, args, { cwd: rootDir, stdio: 'inherit' })
  proc.on('error', (e) => console.error('[mock] spawn error:', e))
  proc.on('exit', (code) => console.log('[mock] exit code:', code))
  return proc
}

async function startRealBackend() {
  const mvn = await findMaven()
  const java = await findJava()
  if (!mvn || !java) {
    console.log('[backend] Maven:', mvn || 'NOT FOUND')
    console.log('[backend] Java:', java || 'NOT FOUND')
    console.warn('[backend] Maven/Java missing, cannot start local Spring Boot backend')
    return null
  }
  const backendDir = resolve(rootDir, 'backend')
  const env = { ...process.env }
  if (!env['JAVA_HOME'] && java) {
    env['JAVA_HOME'] = resolve(java, '..', '..')
  }
  const args = ['spring-boot:run', '-Dspring-boot.run.profiles=h2']
  console.log('[backend] Starting with:', mvn, args.join(' '))
  const proc = spawn(mvn, args, { cwd: backendDir, stdio: 'inherit', env })
  proc.on('error', (e) => console.error('[backend] spawn error:', e))
  proc.on('exit', (code) => console.log('[backend] exit code:', code))
  return proc
}

async function startFrontend(backendPort = DEFAULT_BACKEND_PORT) {
  const code = await httpPing(FRONTEND_PORT, '/')
  if (code.status >= 200 && code.status < 500) {
    console.log('[frontend] port 5173 already responding, skip launch')
    return null
  }
  const frontendDir = resolve(rootDir, 'frontend')
  const node = await findExecutable(['node', 'node.exe'])
  const npm = await findExecutable(['npm', 'npm.cmd'])
  if (!node || !existsSync(resolve(frontendDir, 'node_modules'))) {
    const installer = npm || 'npm.cmd'
    console.log('[frontend] Installing dependencies...')
    try {
      await new Promise((res, rej) => {
        const child = spawn(installer, ['install'], { cwd: frontendDir, stdio: 'inherit', shell: true })
        child.on('error', rej)
        child.on('exit', (code) => code === 0 ? res() : rej(new Error(`npm install exit ${code}`)))
      })
    } catch (e) {
      console.warn('[frontend] npm install may have failed:', e.message)
    }
  }
  const viteEntry = resolve(frontendDir, 'node_modules', 'vite', 'bin', 'vite.js')
  const env = { ...process.env, VITE_BACKEND_PORT: String(backendPort) }
  let proc
  if (existsSync(viteEntry)) {
    proc = spawn(process.execPath, [viteEntry, '--host', '0.0.0.0', '--port', String(FRONTEND_PORT)], { cwd: frontendDir, stdio: 'inherit', env })
  } else if (npm) {
    proc = spawn(npm, ['run', 'dev', '--', '--port', String(FRONTEND_PORT)], { cwd: frontendDir, stdio: 'inherit', env, shell: true })
  } else {
    proc = spawn('npx.cmd', ['vite', '--host', '0.0.0.0', '--port', String(FRONTEND_PORT)], { cwd: frontendDir, stdio: 'inherit', env, shell: true })
  }
  proc.on('error', (e) => console.error('[frontend] spawn error:', e))
  proc.on('exit', (code) => console.log('[frontend] exit code:', code))
  return proc
}

async function main() {
  const args = process.argv.slice(2)
  const onlyBackend = args.includes('--backend-only')
  const forceMock = args.includes('--force-mock')

  console.log('=== 机动车尾气监管平台 服务启动 ===')
  console.log('')

  console.log('[1/3] Checking existing backend...')
  const existing = await detectExistingBackend()
  if (existing.running) {
    console.log(`  Backend already running on port ${existing.port}`)
  } else {
    console.log('  No existing backend found')
  }

  let backendPort = existing.port
  let backendProc = null
  let usingMock = false

  if (!existing.running) {
    console.log('')
    console.log('[2/3] Starting backend...')

    if (forceMock) {
      console.log('  --force-mock specified, using mock backend')
      backendProc = await startMockBackend(onlyBackend)
      usingMock = true
      backendPort = DEFAULT_BACKEND_PORT
    } else {
      backendProc = await startRealBackend()
      if (backendProc) {
        backendPort = DEFAULT_BACKEND_PORT
        console.log('  Local Spring Boot backend starting (H2 profile)...')
      } else {
        console.log('  Falling back to mock backend...')
        backendProc = await startMockBackend(onlyBackend)
        usingMock = true
        backendPort = DEFAULT_BACKEND_PORT
      }
    }
  } else {
    console.log('')
    console.log('[2/3] Backend already running, skip launch')
  }

  let frontendProc = null
  if (!onlyBackend) {
    console.log('')
    console.log('[3/3] Starting frontend...')
    frontendProc = await startFrontend(backendPort)
  }

  console.log('')
  console.log('========================================')
  console.log(`  Backend:  http://localhost:${backendPort}/api`)
  if (usingMock) console.log('             (Mock backend - Node.js simulation)')
  if (existing.running) console.log('             (Pre-existing backend)')
  if (!onlyBackend) console.log(`  Frontend: http://localhost:${FRONTEND_PORT}`)
  console.log('  Login:    admin / 123456')
  console.log('========================================')
  console.log('')
  console.log('Press Ctrl+C to stop all services')

  const shutdown = () => {
    console.log('\nShutting down...')
    try { backendProc && backendProc.kill('SIGTERM') } catch (_) {}
    try { frontendProc && frontendProc.kill('SIGTERM') } catch (_) {}
    setTimeout(() => process.exit(0), 500)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch((e) => { console.error(e); process.exit(1) })
