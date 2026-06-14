import http from 'http'

function get(url) {
  return new Promise((resolve) => {
    const req = http.get(url, (res) => {
      let data = ''
      res.on('data', (c) => (data += c))
      res.on('end', () => resolve({ status: res.statusCode, body: data }))
    })
    req.on('error', (e) => resolve({ status: 0, body: '', error: e.message }))
    req.setTimeout(3000, () => { req.destroy(); resolve({ status: 0, body: '', error: 'timeout' }) })
  })
}

async function main() {
  console.log('测试 8080 后端...')
  const r1 = await get('http://localhost:8080/api/pollutant-limit-rules/fuel-types')
  console.log('fuel-types:', r1.status, r1.body || r1.error)

  const r2 = await get('http://localhost:8080/api/vehicles/search?keyword=%E4%BA%ACA12345')
  console.log('vehicle:', r2.status, r2.body || r2.error)

  const r3 = await get('http://localhost:8080/api/auth/login')
  console.log('auth root:', r3.status)
}
main()
