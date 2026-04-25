// Polyfill localStorage for jsdom environment
// jsdom's localStorage can be broken when using certain vitest configs
const store = {}
const localStorageMock = {
  getItem(key) { return store[key] ?? null },
  setItem(key, value) { store[key] = String(value) },
  removeItem(key) { delete store[key] },
  clear() { Object.keys(store).forEach(k => delete store[k]) },
  get length() { return Object.keys(store).length },
  key(index) { return Object.keys(store)[index] ?? null }
}

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
  writable: true,
  configurable: true
})
