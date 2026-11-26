import { currentPeriod, currentMonth, inputDate } from "./finance";
import { formatDate } from "./date";

test("calcula o período de fevereiro em ano bissexto", () => {
    expect(currentPeriod(new Date(2024, 1, 15))).toEqual({ startDate: "2024-02-01", endDate: "2024-02-29" });
});
test("mantém a data local sem conversão de fuso horário", () => {
    expect(inputDate(new Date(2026, 0, 1))).toBe("2026-01-01");
    expect(currentMonth(new Date(2026, 10, 26))).toBe("2026-11");
    expect(formatDate("2026-11-26T00:00:00")).toBe("26/11/2026");
});
