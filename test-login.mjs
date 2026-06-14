import http from 'http'

function post(url, data) {
  return new Promise((resolve) => {
    const body = JSON.stringify(data)
    const u = new URL(url)
    const req = http.request({
      hostname: u.hostname, port: u.port, path: u.pathname + u.search,
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(body) }
    }, (res) => {
      let d = ''
      res.on('data', c => d += c)
      res.on('end', () => resolve({ status: res.statusCode, body: d }))
    })
    req.on('error', e => resolve({ status: 0, error: e.message }))
    req.write(body)
    req.end()
  })
}

async function main() {
  const r = await post('http://localhost:8080/api/auth/login', { username: 'admin', password: '123456' })
  console.log('登录结果:', r.status, r.body || r.error)
}
main()
