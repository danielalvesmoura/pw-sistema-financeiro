import { useEffect, useMemo, useState } from "react";
import { walletService } from "../../../../services/WalletService";
import { aiInsightsService } from "../../services/aiInsights.service";
import "./styles.css";

const toInputDate = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
};

const defaultPeriod = () => {
    const now = new Date();
    return {
        startDate: toInputDate(new Date(now.getFullYear(), now.getMonth(), 1)),
        endDate: toInputDate(new Date(now.getFullYear(), now.getMonth() + 1, 0)),
    };
};

const money = (value, currency = "BRL") =>
    Number(value || 0).toLocaleString("pt-BR", {
        style: "currency",
        currency,
    });

export default function AiInsightsPage() {
    const initialPeriod = useMemo(defaultPeriod, []);
    const [wallets, setWallets] = useState([]);
    const [walletId, setWalletId] = useState("");
    const [startDate, setStartDate] = useState(initialPeriod.startDate);
    const [endDate, setEndDate] = useState(initialPeriod.endDate);
    const [result, setResult] = useState(null);
    const [loadingWallets, setLoadingWallets] = useState(true);
    const [generating, setGenerating] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        walletService
            .list()
            .then((items) => {
                setWallets(items);
                if (items.length) {
                    setWalletId(String(items[0].id));
                }
            })
            .catch((err) =>
                setError(
                    err?.response?.data?.message ||
                        "Erro ao carregar carteiras.",
                ),
            )
            .finally(() => setLoadingWallets(false));
    }, []);

    useEffect(() => {
        setResult(null);
        setError("");
    }, [walletId, startDate, endDate]);

    const generate = async (event) => {
        event.preventDefault();
        setError("");
        setResult(null);

        if (!walletId) {
            setError("Selecione uma carteira.");
            return;
        }

        if (!startDate || !endDate) {
            setError("Informe a data inicial e a data final.");
            return;
        }

        if (startDate > endDate) {
            setError("A data inicial não pode ser posterior à data final.");
            return;
        }

        setGenerating(true);
        try {
            const data = await aiInsightsService.generate(
                walletId,
                startDate,
                endDate,
            );
            setResult(data);
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    "Não foi possível gerar a análise com IA.",
            );
        } finally {
            setGenerating(false);
        }
    };

    const currency = result?.context?.currency || "BRL";

    return (
        <section className="ai-page">
            <div className="ai-header">
                <div>
                    <h1 className="page-title">Análise com IA</h1>
                    <p className="page-subtitle">
                        Gere um resumo financeiro a partir dos dados reais da carteira.
                    </p>
                </div>
            </div>

            {error && <div className="message error">{error}</div>}
            {loadingWallets && <div className="message">Carregando...</div>}

            {!loadingWallets && wallets.length === 0 && (
                <div className="simple-card">
                    Crie uma carteira e registre transações antes de usar a análise com IA.
                </div>
            )}

            {wallets.length > 0 && (
                <form className="simple-card ai-filter-card" onSubmit={generate}>
                    <label>
                        Carteira
                        <select
                            className="simple-select"
                            value={walletId}
                            onChange={(event) => setWalletId(event.target.value)}
                            disabled={generating}
                        >
                            {wallets.map((wallet) => (
                                <option value={wallet.id} key={wallet.id}>
                                    {wallet.name}
                                </option>
                            ))}
                        </select>
                    </label>

                    <label>
                        Data inicial
                        <input
                            type="date"
                            value={startDate}
                            onChange={(event) => setStartDate(event.target.value)}
                            disabled={generating}
                        />
                    </label>

                    <label>
                        Data final
                        <input
                            type="date"
                            value={endDate}
                            onChange={(event) => setEndDate(event.target.value)}
                            disabled={generating}
                        />
                    </label>

                    <button type="submit" disabled={generating}>
                        {generating ? "Analisando..." : "Gerar análise com IA"}
                    </button>
                </form>
            )}

            {result && (
                <div className="ai-results">
                    <div className="ai-summary-grid">
                        <div className="simple-card ai-metric">
                            <span>Receitas</span>
                            <strong>{money(result.context.totalIncome, currency)}</strong>
                        </div>
                        <div className="simple-card ai-metric">
                            <span>Despesas</span>
                            <strong>{money(result.context.totalExpense, currency)}</strong>
                        </div>
                        <div className="simple-card ai-metric">
                            <span>Resultado</span>
                            <strong>{money(result.context.netResult, currency)}</strong>
                        </div>
                        <div className="simple-card ai-metric">
                            <span>Transações</span>
                            <strong>{result.context.totalTransactions}</strong>
                        </div>
                    </div>

                    <article className="simple-card ai-insight-card">
                        <div className="ai-insight-title">
                            <div>
                                <h2>Insight financeiro</h2>
                                <small>
                                    Período de {result.context.startDate} a {result.context.endDate}
                                </small>
                            </div>
                            <span className="ai-badge">IA</span>
                        </div>

                        <div className="ai-insight-text">{result.insight}</div>

                        <p className="ai-disclaimer">
                            A análise é educativa e pode conter imprecisões. Confira os dados antes de tomar decisões financeiras.
                        </p>
                    </article>
                </div>
            )}
        </section>
    );
}
