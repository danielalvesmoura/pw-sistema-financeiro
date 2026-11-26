export function registerServiceWorker() {
    if (process.env.NODE_ENV !== "production" || !("serviceWorker" in navigator)) return;
    const register = () => navigator.serviceWorker.register("/service-worker.js")
        .catch(error => console.warn("Não foi possível registrar o service worker.", error));
    if (document.readyState === "complete") register();
    else window.addEventListener("load", register, { once: true });
}
