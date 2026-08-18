import api from "../configs/axiosConfig";

export const walletService = {
    async list() {
        const { data } = await api.get("/api/v1/wallets");
        return data;
    },

    async get(id) {
        const { data } = await api.get(`/api/v1/wallets/${id}`);
        return data;
    },

    async create(payload) {
        const { data } = await api.post("/api/v1/wallets", payload);
        return data;
    },

    async update(id, payload) {
        const { data } = await api.put(`/api/v1/wallets/${id}`, payload);
        return data;
    },

    async remove(id) {
        await api.delete(`/api/v1/wallets/${id}`);
    },

    async members(id) {
        const { data } = await api.get(`/api/v1/wallets/${id}/members`);
        return data;
    },

    async addMember(id, payload) {
        const { data } = await api.post(
            `/api/v1/wallets/${id}/members`,
            payload,
        );

        return data;
    },

    async updateMember(id, userId, role) {
        const { data } = await api.patch(
            `/api/v1/wallets/${id}/members/${userId}`,
            {
                role,
            },
        );

        return data;
    },

    async removeMember(id, userId) {
        await api.delete(`/api/v1/wallets/${id}/members/${userId}`);
    },
};
