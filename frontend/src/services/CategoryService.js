import api from "../configs/axiosConfig";

export const categoryService = {
    async list(walletId, type) {
        const params = {
            walletId,
        };

        if (type) {
            params.type = type;
        }

        const { data } = await api.get("/api/v1/categories", {
            params,
        });

        return data;
    },

    async create(walletId, payload) {
        const { data } = await api.post(
            "/api/v1/categories",
            payload,
            {
                params: {
                    walletId,
                },
            },
        );

        return data;
    },

    async update(walletId, id, payload) {
        const { data } = await api.put(
            `/api/v1/categories/${id}`,
            payload,
            {
                params: {
                    walletId,
                },
            },
        );

        return data;
    },

    async remove(walletId, id) {
        await api.delete(`/api/v1/categories/${id}`, {
            params: {
                walletId,
            },
        });
    },
};
