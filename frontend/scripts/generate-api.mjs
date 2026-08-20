import { spawnSync } from 'node:child_process'
import { mkdir, writeFile } from 'node:fs/promises'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const frontendRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const schemaPath = path.join(frontendRoot, 'openapi', 'openapi.json')
const outputPath = path.join(frontendRoot, 'src', 'api', 'generated')
const schemaUrl = process.argv[2] || process.env.OPENAPI_SCHEMA_URL

const fail = (message) => {
  console.error(`\n[API 생성 실패] ${message}`)
  process.exit(1)
}

if (!schemaUrl) {
  fail('OPENAPI_SCHEMA_URL을 .env.development에 설정하거나 문서 주소를 인자로 전달해 주세요.')
}

let response

try {
  response = await fetch(schemaUrl)
} catch (error) {
  fail(`API 문서 서버에 연결할 수 없습니다: ${schemaUrl}\n${error.message}`)
}

if (!response.ok) {
  fail(`API 문서를 받지 못했습니다: ${response.status} ${response.statusText}\n${schemaUrl}`)
}

let schema

try {
  schema = await response.json()
} catch {
  fail(`서버 응답이 JSON 형식이 아닙니다: ${schemaUrl}`)
}

if (!schema.swagger && !schema.openapi) {
  fail(`Swagger/OpenAPI 문서가 아닌 응답을 받았습니다: ${schemaUrl}`)
}

// Do not commit deployment infrastructure details into the downloaded schema
// or the generated client's fallback base path.
delete schema.host
delete schema.schemes

await mkdir(path.dirname(schemaPath), { recursive: true })
await writeFile(schemaPath, `${JSON.stringify(schema, null, 2)}\n`, 'utf8')

console.log(`[API 문서 갱신] ${schemaUrl}`)
console.log(`[API 코드 생성] ${path.relative(frontendRoot, outputPath)}`)

const executable =
  process.platform === 'win32' ? 'openapi-generator-cli.cmd' : 'openapi-generator-cli'
const result = spawnSync(
  executable,
  [
    'generate',
    '-i',
    schemaPath,
    '-g',
    'typescript-axios',
    '-o',
    outputPath,
    '--additional-properties=withSeparateModelsAndApi=true,apiPackage=apis,modelPackage=models',
  ],
  {
    cwd: frontendRoot,
    stdio: 'inherit',
    shell: process.platform === 'win32',
  },
)

if (result.error) {
  fail(result.error.message)
}

if (result.status !== 0) {
  process.exit(result.status ?? 1)
}

console.log('\nAPI 문서와 생성 코드를 최신 상태로 갱신했습니다.')
