import { spawn } from 'child_process'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'
import http from 'http'
import { existsSync } from 'fs'

const __dirname = dirname(fileURLToPath(import.meta.url))
const rootDir = resolve(__dirname)

const DEFAULT_BACKEND_PORT = 8080
const FRONTEND_PORT = 5173
const FALLBACK_BACKEND_PORT = 8081

function log(tag, msg) {
  const ts = new Date().toISOString().substr(11, 8)
  console.log(`[${ts}] [${tag}] ${msg}`)
}

function httpPing(port, path = '/api/pollutant-limit-rules/fuel-types') {
  return new Promise((res) => {
    const req = http.request({ hostname: '127.0.0.1', port, path, method: 'GET', timeout: 1500 }, (r) => {
      let d = ''
      r.on('data', c => d += c)
      r.on('end', () => res({ status: r.statusCode || 0, body: d }))
    })
    req.on('error', () => res({ status: 0, body: '' }))
    req.on('timeout', () => { req.destroy(); res({ status: 0, body: '' }) })
    req.end()
  })
}

async function detectBackendPort() {
  const r1 = await httpPing(DEFAULT_BACKEND_PORT)
  if (r1.status >= 200 && r1.status < 500 && r1.body.includes('汽油')) {
    return { port: DEFAULT_BACKEND_PORT, real: true, running: true }
  }
  const r2 = await httpPing(FALLBACK_BACKEND_PORT)
  if (r2.status >= 200 && r2.status < 500 && r2.body.includes('汽油')) {
    return { port: FALLBACK_BACKEND_PORT, real: false, running: true }
  }
  return { port: 0, real: false, running: false }
}

const vehicles = [
  { plateNumber: '京A12345', vin: 'LHGCM82633A004352', vehicleType: '小型轿车', fuelType: '汽油', emissionStandard: '国六', owner: '张先生', registerDate: '2021-06-18', environmentalStatus: '合格' },
  { plateNumber: '京B67890', vin: 'LSVNV2187N2039456', vehicleType: '轻型货车', fuelType: '柴油', emissionStandard: '国五', owner: '北京绿运物流', registerDate: '2019-03-12', environmentalStatus: '待复检' },
  { plateNumber: '京C24680', vin: 'LFPH4ACC9N1A20458', vehicleType: '小型客车', fuelType: '混合动力', emissionStandard: '国六', owner: '李女士', registerDate: '2022-10-09', environmentalStatus: '合格' }
]

const pollutantRules = [
  { id: 1, fuelType: '汽油', emissionStandard: '国六', coLimit: 0.3, hcLimit: 30.0, noxLimit: 70.0, opacityLimit: 0.25, status: '启用', remark: '汽油车国六排放标准限值', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' },
  { id: 2, fuelType: '汽油', emissionStandard: '国五', coLimit: 0.5, hcLimit: 50.0, noxLimit: 90.0, opacityLimit: 0.35, status: '启用', remark: '汽油车国五排放标准限值', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' },
  { id: 3, fuelType: '汽油', emissionStandard: '国四', coLimit: 0.8, hcLimit: 80.0, noxLimit: 120.0, opacityLimit: 0.50, status: '启用', remark: '汽油车国四排放标准限值', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' },
  { id: 4, fuelType: '柴油', emissionStandard: '国六', coLimit: 0.4, hcLimit: 40.0, noxLimit: 80.0, opacityLimit: 0.20, status: '启用', remark: '柴油车国六排放标准限值', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' },
  { id: 5, fuelType: '柴油', emissionStandard: '国五', coLimit: 0.6, hcLimit: 60.0, noxLimit: 100.0, opacityLimit: 0.30, status: '启用', remark: '柴油车国五排放标准限值', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' },
  { id: 6, fuelType: '柴油', emissionStandard: '国四', coLimit: 0.9, hcLimit: 90.0, noxLimit: 130.0, opacityLimit: 0.45, status: '启用', remark: '柴油车国四排放标准限值', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' },
  { id: 7, fuelType: '混合动力', emissionStandard: '国六', coLimit: 0.25, hcLimit: 25.0, noxLimit: 60.0, opacityLimit: 0.20, status: '启用', remark: '混合动力国六排放标准限值', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' },
  { id: 8, fuelType: '混合动力', emissionStandard: '国五', coLimit: 0.45, hcLimit: 45.0, noxLimit: 85.0, opacityLimit: 0.30, status: '启用', remark: '混合动力国五排放标准限值', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' },
  { id: 9, fuelType: '混合动力', emissionStandard: '国四', coLimit: 0.75, hcLimit: 75.0, noxLimit: 110.0, opacityLimit: 0.45, status: '启用', remark: '混合动力国四排放标准限值', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' }
]

const stations = [
  { id: 1, name: '朝阳机动车环保检测站', district: '朝阳区', address: '北京市朝阳区环保科技园 18 号', phone: '010-61112222', status: '正常' },
  { id: 2, name: '海淀机动车检测中心', district: '海淀区', address: '北京市海淀区清河路 66 号', phone: '010-62223333', status: '正常' },
  { id: 3, name: '亦庄机动车检测站', district: '经开区', address: '北京市经济技术开发区荣华南路 9 号', phone: '010-63334444', status: '正常' },
  { id: 4, name: '西城机动车检测站', district: '西城区', address: '北京市西城区西直门南大街 25 号', phone: '010-64445555', status: '正常' },
  { id: 5, name: '东城机动车检测站', district: '东城区', address: '北京市东城区东四十条 12 号', phone: '010-65556666', status: '正常' },
  { id: 6, name: '丰台机动车检测站', district: '丰台区', address: '北京市丰台区丰台路 88 号', phone: '010-66667777', status: '正常' },
  { id: 7, name: '通州机动车检测站', district: '通州区', address: '北京市通州区新华大街 100 号', phone: '010-67778888', status: '停运' },
  { id: 8, name: '石景山机动车检测站', district: '石景山区', address: '北京市石景山区古城大街 30 号', phone: '010-68889999', status: '正常' }
]

let inspectionRecords = [
  { id: 1, inspectionNo: 'JC20260614101', plateNumber: '京A12345', stationName: '朝阳机动车环保检测站', inspectionTime: '2026-06-14 10:15', coValue: 0.18, hcValue: 18.4, noxValue: 32.8, opacityValue: 0.11, result: '合格', inspector: '王工', reportStatus: '已审核', auditor: '李监管', auditTime: '2026-06-14 11:00' },
  { id: 2, inspectionNo: 'JC20260614102', plateNumber: '京A12345', stationName: '朝阳机动车环保检测站', inspectionTime: '2026-06-14 13:28', coValue: 0.4, hcValue: 40.0, noxValue: 80.0, opacityValue: 0.22, result: '不合格', inspector: 'solo-audit', reportStatus: '待审核', auditor: null, auditTime: null },
  { id: 3, inspectionNo: 'JC20251215023', plateNumber: '京A12345', stationName: '海淀机动车检测中心', inspectionTime: '2025-12-15 14:20', coValue: 0.22, hcValue: 22.1, noxValue: 45.3, opacityValue: 0.15, result: '合格', inspector: '赵工', reportStatus: '已审核', auditor: '王监管', auditTime: '2025-12-15 15:30' },
  { id: 4, inspectionNo: 'JC20250620018', plateNumber: '京B67890', stationName: '朝阳机动车环保检测站', inspectionTime: '2025-06-20 09:45', coValue: 0.7, hcValue: 70.0, noxValue: 110.0, opacityValue: 0.42, result: '不合格', inspector: '张工', reportStatus: '已审核', auditor: '李监管', auditTime: '2025-06-20 10:30' },
  { id: 5, inspectionNo: 'JC20241210031', plateNumber: '京C24680', stationName: '亦庄机动车检测站', inspectionTime: '2024-12-10 16:08', coValue: 0.15, hcValue: 15.0, noxValue: 28.0, opacityValue: 0.08, result: '合格', inspector: '刘工', reportStatus: '已审核', auditor: '赵监管', auditTime: '2024-12-10 17:00' }
]

function parseUrl(req) {
  const url = new URL(req.url, `http://${req.headers.host}`)
  return url
}

function jsonResp(res, data, code = 200) {
  res.writeHead(code, {
    'Content-Type': 'application/json; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': '*',
    'Access-Control-Allow-Methods': '*'
  })
  res.end(JSON.stringify(data))
}

function readBody(req) {
  return new Promise((resolve) => {
    let body = ''
    req.on('data', (chunk) => (body += chunk))
    req.on('end', () => {
      try { resolve(body ? JSON.parse(body) : {}) } catch { resolve({}) }
    })
  })
}

function buildMockServer() {
  return http.createServer(async (req, res) => {
    const url = parseUrl(req)
    const path = url.pathname.replace(/^\/api/, '')
    log('API', `${req.method} ${path}`)

    if (req.method === 'OPTIONS') {
      res.writeHead(204, {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Headers': '*',
        'Access-Control-Allow-Methods': '*'
      })
      res.end()
      return
    }

    if (path === '/auth/login' && req.method === 'POST') {
      const body = await readBody(req)
      const users = {
        admin: { role: '平台管理员', displayName: '平台管理员' },
        regulator: { role: '监管人员', displayName: '东城区监管员' },
        station: { role: '检测站工作人员', displayName: '朝阳检测站' },
        user: { role: '普通用户', displayName: '车主用户' }
      }
      if (users[body.username] && body.password === '123456') {
        jsonResp(res, {
          success: true,
          token: 'mock-jwt-token-' + body.username,
          user: { username: body.username, ...users[body.username] }
        })
        return
      }
      jsonResp(res, { success: false, message: '账号或密码不正确' }, 401)
      return
    }

    if (path === '/vehicles/search') {
      const kw = (url.searchParams.get('keyword') || '').trim().toUpperCase()
      const v = vehicles.find(x => x.plateNumber.toUpperCase() === kw || x.vin.toUpperCase() === kw)
      if (v) { jsonResp(res, { success: true, code: 'OK', message: '查询成功', data: v }); return }
      jsonResp(res, { success: false, code: 'NOT_FOUND', message: '未找到车辆' })
      return
    }

    if (path === '/pollutant-limit-rules/query') {
      const ft = url.searchParams.get('fuelType')
      const es = url.searchParams.get('emissionStandard')
      const r = pollutantRules.find(x => x.fuelType === ft && x.emissionStandard === es && x.status === '启用')
      if (r) { jsonResp(res, r); return }
      res.writeHead(404, { 'Access-Control-Allow-Origin': '*' }); res.end()
      return
    }

    if (path === '/pollutant-limit-rules/list') {
      const page = parseInt(url.searchParams.get('page') || '1')
      const pageSize = parseInt(url.searchParams.get('pageSize') || '10')
      const ft = url.searchParams.get('fuelType')
      const es = url.searchParams.get('emissionStandard')
      const status = url.searchParams.get('status')
      let filtered = pollutantRules.filter(r =>
        (!ft || r.fuelType === ft) &&
        (!es || r.emissionStandard === es) &&
        (!status || r.status === status)
      )
      const start = (page - 1) * pageSize
      jsonResp(res, { total: filtered.length, page, pageSize, records: filtered.slice(start, start + pageSize) })
      return
    }

    if (path === '/pollutant-limit-rules/all') {
      jsonResp(res, pollutantRules.filter(r => r.status === '启用'))
      return
    }

    if (path === '/pollutant-limit-rules/fuel-types') {
      jsonResp(res, ['汽油', '柴油', '混合动力', '天然气', '纯电动'])
      return
    }

    if (path === '/pollutant-limit-rules/emission-standards') {
      jsonResp(res, ['国六', '国五', '国四', '国三', '国二'])
      return
    }

    if (path === '/pollutant-limit-rules/create' && req.method === 'POST') {
      const body = await readBody(req)
      const exists = pollutantRules.find(r => r.fuelType === body.fuelType && r.emissionStandard === body.emissionStandard)
      if (exists) { jsonResp(res, { success: false, message: '该燃料类型和排放标准的规则已存在' }); return }
      const id = Math.max(...pollutantRules.map(r => r.id)) + 1
      const newRule = {
        id,
        fuelType: body.fuelType,
        emissionStandard: body.emissionStandard,
        coLimit: body.coLimit,
        hcLimit: body.hcLimit,
        noxLimit: body.noxLimit,
        opacityLimit: body.opacityLimit,
        status: body.status || '启用',
        remark: body.remark || '',
        createTime: new Date().toLocaleString('zh-CN'),
        updateTime: new Date().toLocaleString('zh-CN')
      }
      pollutantRules.push(newRule)
      jsonResp(res, { success: true, message: '创建成功', data: newRule })
      return
    }

    if (path === '/pollutant-limit-rules/update' && req.method === 'POST') {
      const body = await readBody(req)
      const idx = pollutantRules.findIndex(r => r.id === body.id)
      if (idx < 0) { jsonResp(res, { success: false, message: '规则不存在' }); return }
      pollutantRules[idx] = { ...pollutantRules[idx], ...body, updateTime: new Date().toLocaleString('zh-CN') }
      jsonResp(res, { success: true, message: '更新成功', data: pollutantRules[idx] })
      return
    }

    if (path.startsWith('/pollutant-limit-rules/delete') && req.method === 'POST') {
      const id = parseInt(url.searchParams.get('id') || '0')
      const idx = pollutantRules.findIndex(r => r.id === id)
      if (idx < 0) { jsonResp(res, { success: false, message: '规则不存在' }); return }
      pollutantRules.splice(idx, 1)
      jsonResp(res, { success: true, message: '删除成功' })
      return
    }

    if (path === '/stations') {
      jsonResp(res, stations)
      return
    }

    if (path === '/inspections') {
      const plate = url.searchParams.get('plateNumber')
      const list = plate ? inspectionRecords.filter(r => r.plateNumber === plate) : inspectionRecords
      jsonResp(res, list.sort((a, b) => new Date(b.inspectionTime) - new Date(a.inspectionTime)))
      return
    }

    if (path === '/inspections/detail') {
      const no = url.searchParams.get('inspectionNo')
      const r = inspectionRecords.find(x => x.inspectionNo === no) || inspectionRecords[0]
      jsonResp(res, r)
      return
    }

    if (path === '/inspections/create' && req.method === 'POST') {
      const body = await readBody(req)
      const no = 'JC' + new Date().toISOString().replace(/\D/g, '').slice(0, 14)
      const newRec = { id: inspectionRecords.length + 1, inspectionNo: no, ...body, result: '待审核', reportStatus: '待审核', inspectionTime: new Date().toLocaleString('zh-CN') }
      inspectionRecords.unshift(newRec)
      jsonResp(res, { success: true, message: '检测记录录入成功', data: newRec })
      return
    }

    if (path === '/inspections/judge') {
      let record
      if (req.method === 'POST') {
        record = await readBody(req)
      } else {
        record = { coValue: 0.18, hcValue: 18.4, noxValue: 32.8, opacityValue: 0.11 }
      }
      const ft = url.searchParams.get('fuelType') || '汽油'
      const es = url.searchParams.get('emissionStandard') || '国六'
      const rule = pollutantRules.find(r => r.fuelType === ft && r.emissionStandard === es) || pollutantRules[0]
      const exceededItems = []
      if (record.coValue > rule.coLimit) {
        exceededItems.push({ pollutant: '一氧化碳', pollutantKey: 'co', value: record.coValue, limit: rule.coLimit, exceedRatio: Number(((record.coValue - rule.coLimit) / rule.coLimit * 100).toFixed(1)) })
      }
      if (record.hcValue > rule.hcLimit) {
        exceededItems.push({ pollutant: '碳氢化合物', pollutantKey: 'hc', value: record.hcValue, limit: rule.hcLimit, exceedRatio: Number(((record.hcValue - rule.hcLimit) / rule.hcLimit * 100).toFixed(1)) })
      }
      if (record.noxValue > rule.noxLimit) {
        exceededItems.push({ pollutant: '氮氧化物', pollutantKey: 'nox', value: record.noxValue, limit: rule.noxLimit, exceedRatio: Number(((record.noxValue - rule.noxLimit) / rule.noxLimit * 100).toFixed(1)) })
      }
      if (record.opacityValue > rule.opacityLimit) {
        exceededItems.push({ pollutant: '烟度', pollutantKey: 'opacity', value: record.opacityValue, limit: rule.opacityLimit, exceedRatio: Number(((record.opacityValue - rule.opacityLimit) / rule.opacityLimit * 100).toFixed(1)) })
      }
      const exceedCount = exceededItems.length
      let statusLevel = '合格'
      let environmentalStatus = '环保合格'
      if (exceedCount > 0) {
        environmentalStatus = exceedCount >= 2 ? '严重超标' : '超标'
        statusLevel = exceedCount >= 2 ? '高级' : '中级'
      }
      jsonResp(res, {
        environmentalStatus,
        statusLevel,
        exceededItems,
        suggestion: exceedCount ? '存在污染物检测项目超出限值，建议尽快进行车辆维修并复检' : '车辆排放正常',
        appliedStandard: `${ft}/${es}`,
        fuelType: ft,
        emissionStandard: es,
        coLimit: rule.coLimit,
        hcLimit: rule.hcLimit,
        noxLimit: rule.noxLimit,
        opacityLimit: rule.opacityLimit
      })
      return
    }

    if (path === '/dashboard') {
      jsonResp(res, {
        days: 7,
        totalInspections: 15,
        passedVehicles: 12,
        failedVehicles: 3,
        exceedRate: 20.0,
        pendingAudit: 4,
        stationCount: 8,
        trend: [],
        emissionStandards: [
          { name: '国六', value: 60 },
          { name: '国五', value: 30 },
          { name: '国四及以下', value: 10 }
        ]
      })
      return
    }

    if (path === '/warnings') {
      jsonResp(res, [
        { id: 1, plateNumber: '京B67890', pollutant: 'NOx', level: '高', description: '氮氧化物检测值超过限值，需复检', status: '待处置', createdAt: '2026-06-14 10:30' },
        { id: 2, plateNumber: '京A12345', pollutant: 'CO', level: '中', description: '一氧化碳轻微超标，建议关注', status: '处置中', createdAt: '2026-06-13 15:45' }
      ])
      return
    }

    if (path === '/inspections/judge-by-no') {
      const no = url.searchParams.get('inspectionNo')
      const r = inspectionRecords.find(x => x.inspectionNo === no) || inspectionRecords[0]
      const v = vehicles.find(x => x.plateNumber === r.plateNumber)
      const rule = v ? pollutantRules.find(ru => ru.fuelType === v.fuelType && ru.emissionStandard === v.emissionStandard) : pollutantRules[0]
      const exceededItems = []
      if (r.coValue > rule.coLimit) {
        exceededItems.push({ pollutant: '一氧化碳', pollutantKey: 'co', value: r.coValue, limit: rule.coLimit, exceedRatio: Number(((r.coValue - rule.coLimit) / rule.coLimit * 100).toFixed(1)) })
      }
      if (r.hcValue > rule.hcLimit) {
        exceededItems.push({ pollutant: '碳氢化合物', pollutantKey: 'hc', value: r.hcValue, limit: rule.hcLimit, exceedRatio: Number(((r.hcValue - rule.hcLimit) / rule.hcLimit * 100).toFixed(1)) })
      }
      if (r.noxValue > rule.noxLimit) {
        exceededItems.push({ pollutant: '氮氧化物', pollutantKey: 'nox', value: r.noxValue, limit: rule.noxLimit, exceedRatio: Number(((r.noxValue - rule.noxLimit) / rule.noxLimit * 100).toFixed(1)) })
      }
      if (r.opacityValue > rule.opacityLimit) {
        exceededItems.push({ pollutant: '烟度', pollutantKey: 'opacity', value: r.opacityValue, limit: rule.opacityLimit, exceedRatio: Number(((r.opacityValue - rule.opacityLimit) / rule.opacityLimit * 100).toFixed(1)) })
      }
      const exceedCount = exceededItems.length
      jsonResp(res, {
        environmentalStatus: exceedCount ? (exceedCount >= 2 ? '严重超标' : '超标') : '环保合格',
        statusLevel: exceedCount ? (exceedCount >= 2 ? '高级' : '中级') : '合格',
        exceededItems,
        suggestion: exceedCount ? '存在污染物检测项目超出限值，建议尽快进行车辆维修并复检' : '车辆排放正常',
        appliedStandard: `${rule.fuelType}/${rule.emissionStandard}`,
        fuelType: rule.fuelType,
        emissionStandard: rule.emissionStandard,
        coLimit: rule.coLimit,
        hcLimit: rule.hcLimit,
        noxLimit: rule.noxLimit,
        opacityLimit: rule.opacityLimit
      })
      return
    }

    res.writeHead(404, { 'Access-Control-Allow-Origin': '*' })
    res.end(JSON.stringify({ error: 'Not Found', path }))
  })
}

function startFrontend(backendPort) {
  const frontendDir = resolve(rootDir, 'frontend')
  const viteBins = [
    resolve(frontendDir, 'node_modules', '.bin', 'vite.cmd'),
    resolve(frontendDir, 'node_modules', 'vite', 'bin', 'vite.js')
  ]
  let viteBin = null
  for (const p of viteBins) {
    if (existsSync(p)) { viteBin = p; break }
  }
  if (!viteBin) {
    log('Frontend', '未找到 Vite，请先执行 npm install')
    return null
  }
  const env = { ...process.env, VITE_BACKEND_PORT: String(backendPort) }
  const args = [viteBin, '--host', '0.0.0.0', '--port', String(FRONTEND_PORT)]
  const useShell = process.platform === 'win32'
  const proc = spawn(process.execPath, args, { cwd: frontendDir, stdio: 'inherit', env })
  proc.on('error', (err) => log('Frontend', '启动失败: ' + err.message))
  proc.on('exit', (code) => log('Frontend', '进程退出: ' + code))
  return proc
}

async function findAvailablePort(defaultPort, fallbackPort) {
  const server = buildMockServer()
  return new Promise((res) => {
    server.once('error', () => {
      const server2 = buildMockServer()
      server2.once('error', () => res({ port: 0, server: null }))
      server2.once('listening', () => {
        server2.close()
        res({ port: fallbackPort, server: null })
      })
      server2.listen(fallbackPort, '0.0.0.0')
    })
    server.once('listening', () => {
      server.close()
      res({ port: defaultPort, server: null })
    })
    server.listen(defaultPort, '0.0.0.0')
  })
}

async function main() {
  const args = process.argv.slice(2)
  const onlyBackend = args.includes('--backend-only')
  const forceMock = args.includes('--force-mock')

  console.log('=== 机动车尾气监管平台 - Mock 后端 + 前端 ===')

  if (!forceMock) {
    const detected = await detectBackendPort()
    if (detected.running && detected.real) {
      log('Detect', `检测到真实后端已在端口 ${detected.port} 运行，直接使用`)
      if (!onlyBackend) {
        startFrontend(detected.port)
        log('Frontend', `前端地址: http://localhost:${FRONTEND_PORT}`)
      } else {
        log('Backend', `真实后端已在端口 ${detected.port} 运行`)
      }
      return
    }
  }

  const { port: backendPort } = await findAvailablePort(DEFAULT_BACKEND_PORT, FALLBACK_BACKEND_PORT)
  if (!backendPort) {
    log('Error', `端口 ${DEFAULT_BACKEND_PORT} 和 ${FALLBACK_BACKEND_PORT} 均被占用，无法启动 Mock 后端`)
    process.exit(1)
    return
  }
  if (backendPort !== DEFAULT_BACKEND_PORT) {
    log('Port', `端口 ${DEFAULT_BACKEND_PORT} 已被占用，使用 ${backendPort}`)
  }

  const server = buildMockServer()
  server.listen(backendPort, '0.0.0.0', () => {
    log('Mock', `Mock 后端已启动: http://localhost:${backendPort}`)
    log('Mock', `  预置数据: 9 条规则(3燃料×3标准)、3 辆车、8 个检测站、5 条检测记录`)
    log('Mock', `  登录账号: admin/123456 (平台管理员)`)

    if (!onlyBackend) {
      const frontendProc = startFrontend(backendPort)
      if (frontendProc) {
        log('Frontend', `前端地址: http://localhost:${FRONTEND_PORT}`)
      }
    }
  })

  server.on('error', (err) => {
    log('Error', 'Mock 后端启动失败: ' + err.message)
    process.exit(1)
  })
}

main().catch((e) => { console.error(e); process.exit(1) })

export { buildMockServer, startFrontend, httpPing, pollutantRules, vehicles, stations, inspectionRecords }
