import { useEffect, useState } from "react";
import { walletService } from "../../../../services/WalletService";
import { reportService } from "../../services/reports.service";
import { currentPeriod, formatMoney } from "../../../../shared/utils/finance";
import { formatDate } from "../../../../shared/utils/date";
import "./styles.css";

export default function ReportsPage() {
    const [wallets, setWallets] = useState([]);
    const [walletId, setWalletId] = useState("");
    const [period, setPeriod] = useState(currentPeriod);
    const [report, setReport] = useState(null);
    const [error, setError] = useState("");
    const [busy, setBusy] = useState(false);
    useEffect(() => {
        let active = true;
        walletService.list().then(items => {
            if (active) { setWallets(items); setWalletId(String(items[0]?.id || "")); }
        }).catch(() => { if (active) setError("Não foi possível carregar as carteiras."); });
        return () => { active = false; };
    }, []);
    const change = (setter, value) => { setter(value); setReport(null); setError(""); };
    const generate = async (event, download = false) => {
        event.preventDefault(); setError("");
        if (!walletId || !period.startDate || !period.endDate || period.startDate > period.endDate) {
            setError("Selecione uma carteira e um período válido."); return;
        }
        setBusy(true);
        try {
            if (download) await reportService.downloadTransactions(walletId, period);
            else setReport(await reportService.transactions(walletId, period));
        } catch (err) { setError(err.response?.data?.message || "Não foi possível gerar o extrato."); }
        finally { setBusy(false); }
    };
    const money = value => formatMoney(value, report?.currency || "BRL");
    return <section className="reports-page">
        <h1 className="page-title">Relatórios</h1><p className="page-subtitle">Extrato de transações por período.</p>
        {error && <p role="alert" className="message error">{error}</p>}
        <form className="simple-card simple-form" onSubmit={generate}>
            <label>Carteira<select required value={walletId} disabled={busy} onChange={e => change(setWalletId, e.target.value)}>
                <option value="">Selecione</option>{wallets.map(w => <option key={w.id} value={w.id}>{w.name}</option>)}
            </select></label>
            <div className="report-dates">{[["startDate", "Data inicial"], ["endDate", "Data final"]].map(([key, label]) =>
                <label key={key}>{label}<input type="date" required disabled={busy} value={period[key]} onChange={e => change(setPeriod, { ...period, [key]: e.target.value })} /></label>)}</div>
            <div className="inline-actions"><button className="simple-button" disabled={busy || !walletId}>Gerar extrato</button>
                <button type="button" className="simple-button secondary" disabled={busy || !report} onClick={e => generate(e, true)}>Baixar CSV</button></div>
        </form>
        {report && <div className="simple-card report-result">
            <div className="report-totals"><p>Receitas <strong>{money(report.totalIncome)}</strong></p><p>Despesas <strong>{money(report.totalExpense)}</strong></p><p>Resultado <strong>{money(report.netResult)}</strong></p></div>
            <p>{report.totalTransactions} transações</p>
            <div className="report-table"><table className="simple-table"><thead><tr><th>Data</th><th>Tipo</th><th>Categoria</th><th>Descrição</th><th>Valor</th></tr></thead>
                <tbody>{report.transactions.map(item => <tr key={item.id}><td>{formatDate(item.date)}</td><td>{item.type === "INCOME" ? "Receita" : "Despesa"}</td><td>{item.categoryName}</td><td>{item.description || "—"}</td><td>{money(item.amount)}</td></tr>)}</tbody></table></div>
        </div>}
    </section>;
}
