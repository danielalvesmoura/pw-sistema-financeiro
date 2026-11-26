export function formatMoney(value, currency = "BRL") {
    return Number(value ?? 0).toLocaleString("pt-BR", { style: "currency", currency });
}

export function inputDate(date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}

export function currentPeriod(now = new Date()) {
    return {
        startDate: inputDate(new Date(now.getFullYear(), now.getMonth(), 1)),
        endDate: inputDate(new Date(now.getFullYear(), now.getMonth() + 1, 0)),
    };
}

export function currentMonth(now = new Date()) {
    return inputDate(now).slice(0, 7);
}
