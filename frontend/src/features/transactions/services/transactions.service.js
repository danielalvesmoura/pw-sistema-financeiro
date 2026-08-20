import api from "../../../configs/axiosConfig";

export const transactionService = {
    async list(walletId, params = {}) {
        const { data } = await api.get(
            `/api/v1/wallets/${walletId}/transactions`,
            { params },
        );

        return data;
    }, 

    async create(walletId, payload) {
        const { data } = await api.post(
            `/api/v1/wallets/${walletId}/transactions`,
            payload,
        );

        return data;
    },

    async update(walletId, id, payload) {
        const { data } = await api.put(
            `/api/v1/wallets/${walletId}/transactions/${id}`,
            payload,
        );

        return data;
    },

    async remove(walletId, id) {
        await api.delete(`/api/v1/wallets/${walletId}/transactions/${id}`);
    },

    async summary(walletId, params = {}) {
        const { data } = await api.get(
            `/api/v1/wallets/${walletId}/summary`,
            { params },
        );

        return data;
    },

    async export(walletId, type, format) {
        const response = await api.get(
            `/api/v1/wallets/${walletId}/export`,
            {
                params: {
                    type,
                    format,
                },
                responseType: "blob",
            },
        );

        return response;
    },
};
