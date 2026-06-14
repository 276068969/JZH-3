import { spawn, exec } from 'child_process'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'
import http from 'http'

const __dirname = dirname(fileURLToPath(import.meta.url))
const rootDir = resolve(__dirname)

function log(tag, msg) {
  const ts = new Date().toISOString().substr(11, 8)
  console.log(`[${ts}] [${tag}] ${msg}`)
}

function findNodeModulesBin(name) {
  const candidates = [
    resolve(rootDir, 'frontend', 'node_modules', '.bin', name + '.cmd'),
    resolve(rootDir, 'frontend', 'node_modules', '.bin', name),
    resolve(rootDir, 'frontend', 'node_modules', 'vite', 'bin', name + '.js')
  ]
  const fs = require('fs')
  for (const p of candidates) {
    if (fs.existsSync(p)) return p
  }
  return null
}

const vehicles = [
  { plateNumber: '京A12345', vin: 'LHGCM82633A004352', vehicleType: '小型轿车', fuelType: '汽油', emissionStandard: '国六', owner: '张先生', registerDate: '2021-06-18', environmentalStatus: '合格' },
  { plateNumber: '京B67890', vin: 'LSVNV2187N2039456', vehicleType: '轻型货车', fuelType: '柴油', emissionStandard: '国五', owner: '北京绿运物流', registerDate: '2019-03-12', environmentalStatus: '待复检' },
  { plateNumber: '京C24680', vin: 'LFPH4ACC9N1A20458', vehicleType: '小型客车', fuelType: '混合动力', emissionStandard: '国六', owner: '李女士', registerDate: '2022-10-09', environmentalStatus: '合格' }
]

const pollutantRules = [
  { id: 1, fuelType: '汽油', emissionStandard: '国六', coLimit: 0.3, hcLimit: 30.0, noxLimit: 70.0, opacityLimit: 0.25, status: '启用', remark: '汽油车国六', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' },
  { id: 2, fuelType: '汽油', emissionStandard: '国五', coLimit: 0.5, hcLimit: 50.0, noxLimit: 90.0, opacityLimit: 0.35, status: '启用', remark: '汽油车国五', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' },
  { id: 3, fuelType: '汽油', emissionStandard: '国四', coLimit: 0.8, hcLimit: 80.0, noxLimit: 120.0, opacityLimit: 0.50, status: '启用', remark: '汽油车国四', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' },
  { id: 4, fuelType: '柴油', emissionStandard: '国六', coLimit: 0.4, hcLimit: 40.0, noxLimit: 80.0, opacityLimit: 0.20, status: '启用', remark: '柴油车国六', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' },
  { id: 5, fuelType: '柴油', emissionStandard: '国五', coLimit: 0.6, hcLimit: 60.0, noxLimit: 100.0, opacityLimit: 0.30, status: '启用', remark: '柴油车国五', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' },
  { id: 6, fuelType: '柴油', emissionStandard: '国四', coLimit: 0.9, hcLimit: 90.0, noxLimit: 130.0, opacityLimit: 0.45, status: '启用', remark: '柴油车国四', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' },
  { id: 7, fuelType: '混合动力', emissionStandard: '国六', coLimit: 0.25, hcLimit: 25.0, noxLimit: 60.0, opacityLimit: 0.20, status: '启用', remark: '混合动力国六', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' },
  { id: 8, fuelType: '混合动力', emissionStandard: '国五', coLimit: 0.45, hcLimit: 45.0, noxLimit: 85.0, opacityLimit: 0.30, status: '启用', remark: '混合动力国五', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' },
  { id: 9, fuelType: '混合动力', emissionStandard: '国四', coLimit: 0.75, hcLimit: 75.0, noxLimit: 110.0, opacityLimit: 0.45, status: '启用', remark: '混合动力国四', createTime: '2026-06-01 10:00:00', updateTime: '2026-06-01 10:00:00' }
]

const stations = [
  { id: 1, name: '朝阳机动车环保检测站', district: '朝阳区', address: '北京市朝阳区环保科技园 18 号', phone: '010-61112222', status: '正常' },
  { id: 2, name: '海淀机动车检测中心', district: '海淀区', address: '北京市海淀区清河路 66 号', phone: '010-62223333', status: '正常' },
  { id: 3, name: '亦庄机动车检测站', district: '经开区', address: '北京市经济技术开发区荣华南路 9 号', phone: '010-63334444', status: '正常' }
]

function parseUrl(req) {
  const url = new URL(req.url, `http://${req.headers.host}`)
  return url
}

function jsonResp(res, data, code = 200) {
  res.writeHead(code, { 'Content-Type': 'application/json; charset=utf-8', 'Access-Control-Allow-Origin': '*', 'Access-Control-Allow-Headers': '*', 'Access-Control-Allow-Methods': '*' })
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

const mockServer = http.createServer(async (req, res) => {
  const url = parseUrl(req)
  const path = url.pathname.replace(/^\/api/, '')
  log('API', `${req.method} ${path}`)

  if (req.method === 'OPTIONS') {
    res.writeHead(204, { 'Access-Control-Allow-Origin': '*', 'Access-Control-Allow-Headers': '*', 'Access-Control-Allow-Methods': '*' })
    res.end()
    return
  }

  if (path === '/auth/login' && req.method === 'POST') {
    const body = await readBody(req)
    const users = { admin: { role: '平台管理员', displayName: '平台管理员' }, regulator: { role: '监管人员', displayName: '东城区监管员' }, station: { role: '检测站工作人员', displayName: '朝阳检测站' }, user: { role: '普通用户', displayName: '车主用户' } }
    if (users[body.username] && body.password === '123456') {
      jsonResp(res, { success: true, token: 'mock-jwt-token-' + body.username, user: { username: body.username, ...users[body.username] } })
      return
    }
    jsonResp(res, { success: false, message: '用户名或密码错误' })
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
    let filtered = pollutantRules.filter(r => (!ft || r.fuelType === ft) && (!es || r.emissionStandard === es) && (!status || r.status === status))
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
    const newRule = { id, ...body, status: body.status || '启用', createTime: new Date().toLocaleString(), updateTime: new Date().toLocaleString() }
    pollutantRules.push(newRule)
    jsonResp(res, { success: true, message: '创建成功', record: newRule })
    return
  }

  if (path === '/pollutant-limit-rules/update' && req.method === 'POST') {
    const body = await readBody(req)
    const idx = pollutantRules.findIndex(r => r.id === body.id)
    if (idx < 0) { jsonResp(res, { success: false, message: '规则不存在' }); return }
    pollutantRules[idx] = { ...pollutantRules[idx], ...body, updateTime: new Date().toLocaleString() }
    jsonResp(res, { success: true, message: '更新成功', record: pollutantRules[idx] })
    return
  }

  if (path === '/pollutant-limit-rules/delete' && req.method === 'POST') {
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

  if (path === '/inspections/create' && req.method === 'POST') {
    const body = await readBody(req)
    const no = 'JC' + Date.now()
    jsonResp(res, { success: true, message: '检测记录录入成功', record: { inspectionNo: no, ...body, result: '合格', reportStatus: '待审核' } })
    return
  }

  if (path === '/inspections/detail') {
    const no = url.searchParams.get('inspectionNo') || 'JC20260611001'
    const sample = { inspectionNo: no, plateNumber: '京A12345', stationName: '朝阳机动车环保检测站', inspectionTime: '2026-06-11 09:12', coValue: 0.18, hcValue: 18.4, noxValue: 32.8, opacityValue: 0.11, result: '合格', inspector: '王工', reportStatus: '已审核', auditor: '监管员', auditTime: '2026-06-11 10:00' }
    jsonResp(res, sample)
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
    const exceeded = []
    if (record.coValue > rule.coLimit) exceeded.push({ pollutant: 'CO', value: record.coValue, limit: rule.coLimit, exceedRatio: (record.coValue - rule.coLimit) / rule.coLimit })
    if (record.hcValue > rule.hcLimit) exceeded.push({ pollutant: 'HC', value: record.hcValue, limit: rule.hcLimit, exceedRatio: (record.hcValue - rule.hcLimit) / rule.hcLimit })
    if (record.noxValue > rule.noxLimit) exceeded.push({ pollutant: 'NOx', value: record.noxValue, limit: rule.noxLimit, exceedRatio: (record.noxValue - rule.noxLimit) / rule.noxLimit })
    if (record.opacityValue > rule.opacityLimit) exceeded.push({ pollutant: '烟度', value: record.opacityValue, limit: rule.opacityLimit, exceedRatio: (record.opacityValue - rule.opacityLimit) / rule.opacityLimit })
    jsonResp(res, {
      environmentalStatus: exceeded.length ? '超标' : '环保合格',
      statusLevel: exceeded.length ? (exceeded.length > 1 ? '高' : '中') : '合格',
      exceededItems: exceeded,
      suggestion: exceeded.length ? '存在污染物超标，建议尽快维修并复检' : '车辆排放正常',
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
    jsonResp(res, { days: 7, totalInspections: 10, passedVehicles: 9, failedVehicles: 1, exceedRate: 10.0, pendingAudit: 3, stationCount: 8, trend: [], emissionStandards: [{ name: '国六', value: 60 }, { name: '国五', value: 30 }, { name: '国四及以下', value: 10 }] })
    return
  }

  if (path === '/inspections') {
    jsonResp(res, [
      { inspectionNo: 'JC20260611001', plateNumber: '京A12345', stationName: '朝阳机动车环保检测站', inspectionTime: '2026-06-11 09:12', coValue: 0.18, hcValue: 18.4, noxValue: 32.8, opacityValue: 0.11, result: '合格', inspector: '王工', reportStatus: '已审核' },
      { inspectionNo: 'JC20260611002', plateNumber: '京B67890', stationName: '海淀机动车检测中心', inspectionTime: '2026-06-11 10:05', coValue: 0.42, hcValue: 39.7, noxValue: 88.1, opacityValue: 0.38, result: '不合格', inspector: '赵工', reportStatus: '待审核' }
    ])
    return
  }

  if (path === '/warnings') {
    jsonResp(res, [
      { id: 1, plateNumber: '京B67890', pollutant: 'NOx', level: '高', description: '氮氧化物检测值超过限值，需复检', status: '待处置', createdAt: '2026-06-11 10:30' }
    ])
    return
  }

  res.writeHead(404, { 'Access-Control-Allow-Origin': '*' })
  res.end(JSON.stringify({ error: 'Not Found', path }))
})

mockServer.listen(8080, () => {
  log('Mock', 'Mock 后端已启动: http://localhost:8080')
})

const frontendDir = resolve(rootDir, 'frontend')
const viteBin = resolve(frontendDir, 'node_modules', 'vite', 'bin', 'vite.js')
log('Frontend', `启动前端 Vite dev server: ${viteBin}`)
const nodeExe = process.execPath
const frontend = spawn(nodeExe, [viteBin, '--host', '0.0.0.0', '--port', '5173'], {
  cwd: frontendDir,
  stdio: 'inherit',
  env: { ...process.env }
})

frontend.on('error', (err) => log('Frontend', '启动失败: ' + err.message))
frontend.on('exit', (code) => log('Frontend', '进程退出: ' + code))
