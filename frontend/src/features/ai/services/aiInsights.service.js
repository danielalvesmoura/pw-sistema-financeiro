import api from "../../../configs/axiosConfig";

export const aiInsightsService = {
    async generate(walletId, startDate, endDate) {
        const { data } = await api.post(
            `/api/v1/wallets/${walletId}/ai/insights`,
            { startDate, endDate },
        );

        return data;
    },
};
