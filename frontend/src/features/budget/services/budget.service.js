import api from "../../../configs/axiosConfig";

export const budgetService = {
    async get(walletId, month) {
        const { data } = await api.get(`/api/v1/wallets/${walletId}/budget`, { params: { month } });
        return data;
    },
    async update(walletId, month, limit) {
        const { data } = await api.put(`/api/v1/wallets/${walletId}/budget`, { limit }, { params: { month } });
        return data;
    },
};
