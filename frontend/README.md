# frontend

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VS Code](https://code.visualstudio.com/) + [Vue (Official)](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Recommended Browser Setup

- Chromium-based browsers (Chrome, Edge, Brave, etc.):
  - [Vue.js devtools](https://chromewebstore.google.com/detail/vuejs-devtools/nhdogjmejiglipccpnnnanhbledajbpd)
  - [Turn on Custom Object Formatter in Chrome DevTools](http://bit.ly/object-formatters)
- Firefox:
  - [Vue.js devtools](https://addons.mozilla.org/en-US/firefox/addon/vue-js-devtools/)
  - [Turn on Custom Object Formatter in Firefox DevTools](https://fxdx.dev/firefox-devtools-custom-object-formatters/)

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) to make the TypeScript language service aware of `.vue` types.

## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).

## Project Setup

```sh
npm install
```

### Compile and Hot-Reload for Development

```sh
npm run dev
```

### Type-Check, Compile and Minify for Production

```sh
npm run build
```

### Lint with [ESLint](https://eslint.org/)

```sh
npm run lint
```

### Generate API Client from Backend

`.env.development`의 `OPENAPI_SCHEMA_URL`에 지정된 Swagger 문서에서 API 타입과 Axios 클라이언트를 다시 생성합니다.

```sh
npm run generate:api
```

일회성으로 다른 주소를 사용할 때는 명령 뒤에 Swagger/OpenAPI JSON 주소를 전달합니다.

```sh
npm run generate:api -- http://localhost:8080/api/v2/api-docs
```

환경변수로 고정할 수도 있습니다.

```sh
OPENAPI_SCHEMA_URL=http://localhost:8080/api/v2/api-docs npm run generate:api
```

명령을 실행하면 `openapi/openapi.json`과 `src/api/generated`가 함께 갱신됩니다.
