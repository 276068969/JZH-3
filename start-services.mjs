import { spawn, exec } from 'child_process'
import { existsSync, readdirSync } from 'fs'
import { dirname, resolve } from 'path'
import { fileURLToPath } from 'url'
import { createServer, request } from 'http'

const __dirname = dirname(fileURLToPath(import.meta.url))

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

function httpPing(port, path = '/') {
  return new Promise((res) => {
    const req = request({ hostname: '127.0.0.1', port, path, method: 'GET', timeout: 1500 }, (r) => {
      res(r.statusCode || 200)
    })
    req.on('error', () => res(0))
    req.on('timeout', () => { req.destroy(); res(0) })
    req.end()
  })
}

function runCommand(command, args, cwd, label) {
  return new Promise((res, rej) => {
    console.log(`[${label}] ${command} ${args.join(' ')}`)
    const child = spawn(command, args, { cwd, stdio: 'inherit', shell: true })
    child.on('error', rej)
    child.on('exit', (code) => {
      if (code === 0) res(code); else rej(new Error(`${label} exit code ${code}`))
    })
  })
}

async function findMaven() {
  const fromPath = await findExecutable(['mvn', 'mvn.cmd', 'mvn.bat'])
  if (fromPath) return fromPath

  const candidates = [
    'C:\\Program Files\\Apache\\maven\\bin\\mvn.cmd',
    'C:\\Program Files\\Apache Software Foundation\\maven\\bin\\mvn.cmd',
    'C:\\ProgramData\\chocolatey\\lib\\maven\\bin\\mvn.cmd',
    `${process.env['USERPROFILE'] || ''}\\scoop\\apps\\maven\\current\\bin\\mvn.cmd`,
    `${process.env['USERPROFILE'] || ''}\\scoop\\apps\\maven\\current\\bin\\mvn`,
    `${process.env['USERPROFILE'] || ''}\\.sdkman\\candidates\\maven\\current\\bin\\mvn`,
    `${process.env['USERPROFILE'] || ''}\\AppData\\Local\\JetBrains\\Toolbox\\apps\\IDEA-U\\ch-0\\*\\plugins\\maven\\lib\\maven3\\bin\\mvn.cmd`,
    'C:\\Program Files\\JetBrains\\IntelliJ IDEA\\plugins\\maven\\lib\\maven3\\bin\\mvn.cmd'
  ]
  for (const p of candidates) {
    const pattern = p.replace(/\*/g, '')
    if (existsSync(pattern)) return pattern
    if (p !== pattern) {
      const dir = dirname(pattern)
      if (existsSync(dir)) {
        try {
          const files = readdirSync(dirname(dir))
          for (const f of files) {
            const guess = resolve(dirname(dir), f, 'bin', 'mvn.cmd')
            if (existsSync(guess)) return guess
          }
        } catch (_) {}
      }
    }
  }
  return null
}

async function findJava() {
  const fromPath = await findExecutable(['java', 'java.exe'])
  if (fromPath) return fromPath
  if (process.env['JAVA_HOME'] && existsSync(resolve(process.env['JAVA_HOME'], 'bin', 'java.exe'))) {
    return resolve(process.env['JAVA_HOME'], 'bin', 'java.exe')
  }
  const candidates = [
    'C:\\Program Files\\Java\\jdk-21\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk-17\\bin\\java.exe',
    'C:\\Program Files\\Java\\jdk-11\\bin\\java.exe',
    'C:\\Program Files\\Eclipse Adoptium\\jdk-21\\bin\\java.exe',
    'C:\\Program Files\\Eclipse Adoptium\\jdk-17\\bin\\java.exe',
    'C:\\Program Files\\Microsoft\\jdk-21\\bin\\java.exe',
    'C:\\Program Files\\Microsoft\\jdk-17\\bin\\java.exe',
    'C:\\Program Files\\Zulu\\zulu-21\\bin\\java.exe',
    'C:\\Program Files\\Zulu\\zulu-17\\bin\\java.exe',
    `${process.env['USERPROFILE'] || ''}\\scoop\\apps\\openjdk\\current\\bin\\java.exe`,
    `${process.env['USERPROFILE'] || ''}\\.sdkman\\candidates\\java\\current\\bin\\java.exe`
  ]
  for (const p of candidates) {
    if (existsSync(p)) return p
  }
  return null
}

async function startBackendIfNeeded() {
  const code = await httpPing(8080, '/api/pollutant-limit-rules/fuel-types')
  if (code >= 200 && code < 500) {
    console.log('[backend] port 8080 already responding, skip launch')
    return null
  }

  const mvn = await findMaven()
  const java = await findJava()
  console.log('[backend] Maven:', mvn || 'NOT FOUND')
  console.log('[backend] Java:', java || 'NOT FOUND')

  if (!mvn || !java) {
    console.error('[backend] Maven/Java missing. Start backend manually, or use mock-backend-and-start.mjs')
    console.error('[backend] Skip backend launch; proceed to frontend if possible')
    return null
  }

  const backendDir = resolve(__dirname, 'backend')
  const env = { ...process.env }
  if (!env['JAVA_HOME'] && java) {
    env['JAVA_HOME'] = resolve(java, '..', '..')
  }
  const args = [mvn, 'spring-boot:run', '-Dspring-boot.run.profiles=h2']
  const proc = spawn(process.platform === 'win32' ? 'cmd.exe' : 'sh',
    process.platform === 'win32' ? ['/c', ...args] : ['-c', args.join(' ')],
    { cwd: backendDir, stdio: 'inherit', env })
  proc.on('error', (e) => console.error('[backend] spawn error:', e))
  proc.on('exit', (code) => console.log('[backend] exit', code))
  return proc
}

async function startFrontend() {
  const code = await httpPing(5173, '/')
  if (code >= 200 && code < 500) {
    console.log('[frontend] port 5173 already responding, skip launch')
    return null
  }
  const frontendDir = resolve(__dirname, 'frontend')
  const node = await findExecutable(['node', 'node.exe'])
  const npm = await findExecutable(['npm', 'npm.cmd'])
  if (!node || !existsSync(resolve(frontendDir, 'node_modules'))) {
    const installer = npm || 'npm.cmd'
    try { await runCommand(installer, ['install'], frontendDir, 'frontend-npm-install') } catch (e) { console.warn('[frontend] npm install may have failed:', e.message) }
  }
  const viteBin = resolve(frontendDir, 'node_modules', '.bin', process.platform === 'win32' ? 'vite.cmd' : 'vite')
  let args
  if (existsSync(viteBin)) {
    args = [viteBin, '--host', '0.0.0.0', '--port', '5173']
  } else if (npm) {
    args = [npm, 'run', 'dev', '--', '--port', '5173']
  } else {
    args = ['npx.cmd', 'vite', '--host', '0.0.0.0', '--port', '5173']
  }
  const proc = spawn(process.platform === 'win32' ? 'cmd.exe' : 'sh',
    process.platform === 'win32' ? ['/c', ...args] : ['-c', args.join(' ')],
    { cwd: frontendDir, stdio: 'inherit' })
  proc.on('error', (e) => console.error('[frontend] spawn error:', e))
  proc.on('exit', (code) => console.log('[frontend] exit', code))
  return proc
}

async function main() {
  console.log('=== 机动车尾气监管平台 服务启动 ===')
  const backendProc = await startBackendIfNeeded()
  const frontendProc = await startFrontend()
  console.log('  Backend:  http://localhost:8080/api')
  console.log('  Frontend: http://localhost:5173')
  const shutdown = () => {
    try { backendProc && backendProc.kill('SIGINT') } catch (_) {}
    try { frontendProc && frontendProc.kill('SIGINT') } catch (_) {}
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch((e) => { console.error(e); process.exit(1) })
