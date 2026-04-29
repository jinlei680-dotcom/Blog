# CLAUDE.md

## Project Overview

Full-stack blog application (Wanderlust) — Nuxt 3 frontend + Spring Boot 2.7.18 backend + MySQL 8.0.
Frontend dev server proxies `/api/**` → `http://localhost:8080/api/**`.

## Commands

### Frontend (`blog-frontend/`)
```bash
npm install && npm run dev     # http://localhost:3000
npm run build                  # Production build
npm run test                   # Vitest (--run, non-watch)
```

### Backend (`blog-backend/`)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local   # Port 8080, remote DB
mvn test                                                # H2, no remote DB needed
mvn package                                             # Build JAR
```

### Database
- MySQL Docker container: `124.222.154.124:13306`, database `my_blog`, root/`Blog@2024!`
- Local dev requires `local` profile; default config points to `localhost:3306`
- H2 in-memory for tests

## Frontend Architecture (`blog-frontend/app/`)

**Rendering**: SSR for homepage/article/tag/user/search; CSR for create/edit/login/register (see `nuxt.config.ts` routeRules).

**Composables-first pattern**: All API calls live in `composables/`. Components are thin — they consume composables, never call `$fetch` directly. 12 composables covering articles, auth, bookmarks, comments, drafts, likes, music, notifications, profiles, search, tags, uploads.

**State**: Two Pinia stores — `auth.js` (token/user/role, persisted to localStorage) and `notification.js` (unreadCount). Auth restored on startup via `plugins/auth.client.js`. Global `$fetch` interceptor in `plugins/api.client.js` redirects to login on 401.

**Auth flow**: JWT stored in localStorage → sent as Bearer header via composables → 401 interceptor clears state and redirects. Middleware: `auth.js` (require login), `admin.js` (require admin).

**Key dependencies**: `md-editor-v3` for markdown editing/rendering, `dompurify` for XSS sanitization.

**Design**: Warm-toned glassmorphism with CSS variables. fonts: Poppins + Inter + PingFang SC. Effects: backdrop-blur, 3D card tilt, scroll-reveal, reading progress bar.

## Backend Architecture (`blog-backend/`)

**Flow**: Controller → Service interface → ServiceImpl → Repository (Spring Data JPA) → MySQL. All responses wrapped in `ApiResponse<T>`.

**Security**: Stateless JWT (HS512, 24h expiry). `JwtAuthenticationFilter` extracts Bearer token, validates, sets SecurityContext. Public GET endpoints for articles/music/tags/users; POST `/api/auth/**` also public. All other mutations require auth.

**Entities** (8 tables): User, Article, Tag, ArticleTag (M2M junction), ArticleLike, Comment (self-referencing for replies), Music, Notification.

**File uploads**: Stored in `./uploads/{images,avatars}` with UUID naming. Served via static resource mapping.

**Testing**: JUnit 5 + Mockito (unit), MockMvc + H2 (integration), jqwik (property-based). 11 test files total.

## Key Conventions

- Components don't call `$fetch` — always go through composables
- Backend DTOs are separated into request/response classes under `dto/`
- All API responses use the `ApiResponse<T>` wrapper
- Client-only components (editor, renderer) handle SSR fallback with `<ClientOnly>`
- Drafts and bookmarks are client-side only (localStorage), no backend API
- Backend profile `local` must be active when connecting to remote MySQL

## Reference

Detailed file listings and API endpoint tables → `docs/reference.md`
