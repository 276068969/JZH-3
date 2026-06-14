import { spawn, exec } from 'child_process'
import { existsSync } from 'fs'
import { resolve } from 'path'

function findExecutable(names: string[]): Promise<string | null> {
  return new Promise((resolve) => {
    const where = process.platform === 'win32' ? 'where' : 'which'
    for (const name of names) {
      exec(`${where} ${name}`, (err, stdout) => {
        if (!err && stdout.trim()) {
          resolve(stdout.trim().split('\n')[0].trim())
        }
      })
    }
    setTimeout(() => resolve(null), 3000)
  })
}

async function main() {
  console.log('=== 启动后端服务 ===')
  const mvnPath = await findExecutable(['mvn', 'mvn.cmd'])
  console.log('Maven 路径:', mvnPath)

  if (!mvnPath) {
    console.error('找不到 Maven，尝试常见路径...')
    const candidates = [
      'C:\\Program Files\\Apache\\maven\\bin\\mvn.cmd',
      'C:\\ProgramData\\chocolatey\\lib\\maven\\bin\\mvn.cmd'
    ]
    for (const p of candidates) {
      if (existsSync(p)) {
        console.log('找到:', p)
        return startBackend(p)
      }
    }
    console.error('仍找不到 Maven，退出')
    process.exit(1)
    return
  }
  startBackend(mvnPath)
}

function startBackend(mvnPath: string) {
  const backendDir = resolve(__dirname, 'backend')
  console.log('后端目录:', backendDir)
  console.log('启动命令:', mvnPath, 'spring-boot:run -Dspring-boot.run.profiles=h2')

  const child = spawn(mvnPath, ['spring-boot:run', '-Dspring-boot.run.profiles=h2'], {
    cwd: backendDir,
    stdio: 'inherit',
    shell: true
  })

  child.on('error', (err) => {
    console.error('启动失败:', err)
  })

  child.on('exit', (code) => {
    console.log('进程退出，代码:', code)
  })
}

main()
