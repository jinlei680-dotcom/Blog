# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Full-stack blog application with:
- **Frontend**: Nuxt 3 (Vue 3) + Pinia + TypeScript at `blog-frontend/`
- **Backend**: Spring Boot 2.7.18 + MySQL + JWT at `blog-backend/`
- **Frontend** proxies `/api/**` → `http://localhost:8080/api/**`

## Commands

### Frontend (`blog-frontend/`)
```bash
npm install
npm run dev        # Dev server at http://localhost:3000
npm run build      # Production build
npm run generate   # Static site generation
npm run preview    # Preview production build
npm run test       # Run Vitest tests (--run, non-watch)
```

### Backend (`blog-backend/`)
```bash
mvn spring-boot:run   # Start on port 8080
mvn test              # Run all tests
mvn package           # Build JAR
```

### Database
- MySQL 运行在云服务器 Docker 容器中：`124.222.154.124:13306`，容器名 `mysql-blog`
- 数据库名：`my_blog`，root 密码：`Blog@2024!`
- 容器已设置 `--restart always`，服务器重启后自动拉起
- H2 in-memory DB used for backend tests

## Architecture

### Frontend (`blog-frontend/app/`)

**Rendering strategy** (set per-route in `nuxt.config.ts`):
- SSR: homepage, article detail, tag pages, user profiles, search
- CSR: create/edit article, login, register

**State management**: Pinia stores in `stores/` (`auth.js`, `notification.js`). Auth state is initialized in `plugins/auth.client.js`. API client is set up in `plugins/api.client.js`.

**Route guards**: `middleware/auth.js` (require login) and `middleware/admin.js` (require admin role).

**Composables** in `composables/` encapsulate all API call logic (e.g., `useArticle.js`, `useAuth.js`, `useComment.js`). Components consume composables rather than calling `$fetch` directly.

### Backend (`blog-backend/src/main/java/com/blog/`)

**Request flow**: Controller → Service interface → ServiceImpl → Repository (Spring Data JPA) → MySQL

**Security**: JWT filter (`JwtAuthenticationFilter`) validates tokens before controllers. Config in `SecurityConfig.java`. Tokens expire after 24 hours.

**API response format**: All endpoints return `ApiResponse<T>` wrapper from `common/ApiResponse.java`.

**File uploads**: Stored locally in `./uploads` directory relative to backend working directory. Served via `FileController`.

**Key entities**: User, Article, Comment, Tag, ArticleTag (many-to-many junction), ArticleLike, Music, Notification

### Testing

**Frontend**: Vitest + Vue Test Utils. Tests in `app/__tests__/`. Setup in `vitest.setup.js` (localStorage polyfill for jsdom).

**Backend**: JUnit 5 + jqwik (property-based testing) + Spring Test. H2 replaces MySQL in test context.
