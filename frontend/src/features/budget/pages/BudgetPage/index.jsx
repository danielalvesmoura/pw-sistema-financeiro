import { useEffect, useState } from "react";
import { walletService } from "../../../../services/WalletService";
import { realtimeService } from "../../../../services/RealtimeService";
import { currentMonth, formatMoney } from "../../../../shared/utils/finance";
import { budgetService } from "../../services/budget.service";
import "./styles.css";

export default function BudgetPage() {
    const [wallets, setWallets] = useState([]);
    const [walletId, setWalletId] = useState("");
    const [month, setMonth] = useState(currentMonth);
    const [budget, setBudget] = useState(null);
    const [limit, setLimit] = useState("");
    const [error, setError] = useState("");
    const [busy, setBusy] = useState(false);
    const [revision, setRevision] = useState(0);

    useEffect(() => {
        let active = true;
        walletService.list().then(items => {
            if (active) { setWallets(items); setWalletId(String(items[0]?.id || "")); }
        }).catch(() => { if (active) setError("Não foi possível carregar as carteiras."); });
        return () => { active = false; };
    }, []);

    useEffect(() => {
        if (!walletId || !month) return;
        let active = true;
        budgetService.get(walletId, month).then(data => {
            if (active) { setBudget(data); setLimit(data.limit ?? ""); }
        }).catch(err => { if (active) setError(err.response?.data?.message || "Falha ao carregar orçamento."); });
        return () => { active = false; };
    }, [walletId, month, revision]);

    useEffect(() => {
        if (!walletId || !month) return;
        let active = true;
        const unsubscribe = realtimeService.subscribeToWallet(walletId, event => {
            if (event.type === "CONNECTED" || event.type?.startsWith("TRANSACTION_")) {
                budgetService.get(walletId, month).then(data => { if (active) setBudget(data); }).catch(() => {});
            }
        });
        return () => { active = false; unsubscribe(); };
    }, [walletId, month]);

    const changeSelection = (setter, value) => {
        setBudget(null); setError(""); setLimit(""); setter(value);
    };
    const save = async (event, remove = false) => {
        event.preventDefault();
        if (!budget?.canEdit || busy) return;
        setBusy(true); setError("");
        try {
            await budgetService.update(walletId, month, remove ? null : Number(limit));
            setRevision(value => value + 1);
        } catch (err) {
            setError(err.response?.data?.message || "Não foi possível salvar o limite.");
        } finally { setBusy(false); }
    };
    const money = value => formatMoney(value, budget?.currency || "BRL");
    const exceeded = budget?.remaining != null && budget.remaining < 0;

    return <section className="budget-page">
        <h1 className="page-title">Orçamento mensal</h1>
        <p className="page-subtitle">Defina um limite recorrente e acompanhe as despesas de cada mês.</p>
        {error && <p role="alert" className="message error">{error}</p>}
        <div className="simple-card simple-form budget-filters">
            <label>Carteira<select value={walletId} disabled={busy} onChange={e => changeSelection(setWalletId, e.target.value)}>
                <option value="">Selecione uma carteira</option>
                {wallets.map(w => <option key={w.id} value={w.id}>{w.name}</option>)}
            </select></label>
            <label>Mês<input type="month" value={month} disabled={busy} onChange={e => changeSelection(setMonth, e.target.value)} required /></label>
        </div>
        {budget && <div className="simple-card budget-summary">
            <p>Despesas do mês</p><strong>{money(budget.spent)}</strong>
            {budget.limit == null ? <p>Esta carteira ainda não possui limite.</p> : <>
                <p>Limite: {money(budget.limit)}</p>
                <progress aria-label="Percentual utilizado do orçamento" max="100" value={Math.min(100, Number(budget.percentage))} />
                <p>{budget.percentage}% utilizado</p>
                <p className={exceeded ? "budget-exceeded" : ""}>{exceeded ? "Limite excedido em " : "Disponível: "}{money(Math.abs(budget.remaining))}</p>
            </>}
            {budget.canEdit ? <form className="simple-form" onSubmit={save}>
                <label>Limite mensal recorrente<input type="number" min="0.01" max="9999999999999.99" step="0.01" required value={limit} onChange={e => setLimit(e.target.value)} /></label>
                <div className="inline-actions"><button className="simple-button" disabled={busy}>Salvar limite</button>
                    {budget.limit != null && <button type="button" className="simple-button secondary" disabled={busy} onClick={e => save(e, true)}>Remover limite</button>}
                </div>
            </form> : <p>Somente o dono da carteira pode alterar o limite.</p>}
        </div>}
    </section>;
}
