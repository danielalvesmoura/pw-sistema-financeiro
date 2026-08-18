import api from "../../../configs/axiosConfig";

export const authService = {
    async login(email, password) {
        const { data } = await api.post("/auth/login", { email, password });
        localStorage.setItem("app-token", data.accessToken);
        const me = await api.get("/api/v1/users/me");
        localStorage.setItem("usuario", JSON.stringify(me.data));
        return data;
    },
    async register(name, email, password) {
        const { data } = await api.post("/auth/register", { name, email, password });
        return data;
    },
    async forgotPassword(email) {
        const { data } = await api.post("/auth/forgot-password", { email });
        return data;
    },
    async resetPassword(token, newPassword) {
        const { data } = await api.post("/auth/reset-password", { token, newPassword });
        return data;
    },
    async changePassword(currentPassword, newPassword) {
        const { data } = await api.patch("/api/v1/users/me/password", { currentPassword, newPassword });
        return data;
    },
    logout() {
        localStorage.removeItem("app-token");
        localStorage.removeItem("usuario");
    }
};
