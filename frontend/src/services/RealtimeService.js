const API_BASE_URL = (process.env.REACT_APP_API_BASE_URL || "http://localhost:8080").replace(/\/$/, "");
const RECONNECT_DELAY_MS = 1500;

const wait = (milliseconds, signal) =>
    new Promise((resolve) => {
        if (signal.aborted) {
            resolve();
            return;
        }

        const timeout = setTimeout(resolve, milliseconds);
        signal.addEventListener(
            "abort",
            () => {
                clearTimeout(timeout);
                resolve();
            },
            { once: true },
        );
    });

const dispatchEventBlock = (block, onEvent) => {
    const data = block
        .split("\n")
        .filter((line) => line.startsWith("data:"))
        .map((line) => line.slice(5).trimStart())
        .join("\n");

    if (!data) {
        return;
    }

    try {
        onEvent(JSON.parse(data));
    } catch (error) {
        console.error("Evento em tempo real inválido.", error);
    }
};

export const realtimeService = {
    subscribeToWallet(walletId, onEvent, onError = () => {}) {
        const controller = new AbortController();

        const connect = async () => {
            while (!controller.signal.aborted) {
                const token = localStorage.getItem("app-token");

                if (!token) {
                    return;
                }

                try {
                    const response = await fetch(
                        `${API_BASE_URL}/api/v1/wallets/${walletId}/events`,
                        {
                            headers: {
                                Accept: "text/event-stream",
                                Authorization: `Bearer ${token}`,
                            },
                            signal: controller.signal,
                        },
                    );

                    if (!response.ok || !response.body) {
                        throw new Error(`Falha ao conectar ao tempo real (${response.status}).`);
                    }

                    const reader = response.body.getReader();
                    const decoder = new TextDecoder();
                    let buffer = "";

                    while (!controller.signal.aborted) {
                        const { value, done } = await reader.read();

                        if (done) {
                            break;
                        }

                        buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, "\n");

                        let separatorIndex = buffer.indexOf("\n\n");
                        while (separatorIndex >= 0) {
                            const block = buffer.slice(0, separatorIndex);
                            buffer = buffer.slice(separatorIndex + 2);
                            dispatchEventBlock(block, onEvent);
                            separatorIndex = buffer.indexOf("\n\n");
                        }
                    }
                } catch (error) {
                    if (!controller.signal.aborted) {
                        onError(error);
                    }
                }

                if (!controller.signal.aborted) {
                    await wait(RECONNECT_DELAY_MS, controller.signal);
                }
            }
        };

        connect();

        return () => controller.abort();
    },
};
