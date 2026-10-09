import api from "../../../configs/axiosConfig";

export const reportService = {
    async transactions(walletId, params) {
        const { data } = await api.get(
            `/api/v1/wallets/${walletId}/reports/transactions`,
            { params },
        );

        return data;
    },

    async downloadTransactions(walletId, params) {
        const response = await api.get(
            `/api/v1/wallets/${walletId}/reports/transactions.csv`,
            {
                params,
                responseType: "blob",
            },
        );

        const disposition = response.headers["content-disposition"] || "";
        const filenameMatch = disposition.match(/filename="?([^";]+)"?/i);
        const filename = filenameMatch?.[1] || "extrato-transacoes.csv";
        const url = window.URL.createObjectURL(response.data);
        const link = document.createElement("a");

        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);
    },
};
