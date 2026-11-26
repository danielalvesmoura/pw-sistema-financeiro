const CACHE = "financeiro-pwa-v2";
const PREFIX = "financeiro-pwa-";
const SHELL = ["/offline.html", "/manifest.json", "/favicon.ico", "/logo192.png", "/logo512.png"];

self.addEventListener("install", event => {
    event.waitUntil(caches.open(CACHE).then(cache => cache.addAll(SHELL)).then(() => self.skipWaiting()));
});
self.addEventListener("activate", event => {
    event.waitUntil(caches.keys().then(keys => Promise.all(keys
        .filter(key => key.startsWith(PREFIX) && key !== CACHE).map(key => caches.delete(key))))
        .then(() => self.clients.claim()));
});
self.addEventListener("fetch", event => {
    const request = event.request;
    const url = new URL(request.url);
    if (request.method !== "GET" || url.origin !== self.location.origin || request.headers.has("Authorization") ||
        /^\/(api|auth|actuator|v3|swagger-ui)(\/|$|\.)/.test(url.pathname)) return;
    if (request.mode === "navigate") {
        event.respondWith(fetch(request).catch(() => caches.match("/offline.html")));
        return;
    }
    // Somente recursos estáticos conhecidos; nunca dados de API nem HTML autenticado.
    if (!url.pathname.startsWith("/static/") && !SHELL.includes(url.pathname)) return;
    event.respondWith(caches.match(request).then(async cached => {
        if (cached) return cached;
        const response = await fetch(request);
        if (response.ok) {
            const cache = await caches.open(CACHE);
            await cache.put(request, response.clone());
        }
        return response;
    }));
});
