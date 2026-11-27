import { useEffect, useMemo, useState } from "react";
import { walletService } from "../../../../services/WalletService";
import { aiInsightsService } from "../../services/aiInsights.service";
import "./styles.css";
import { currentPeriod, formatMoney as money } from "../../../../shared/utils/finance";

// Renderiza apenas a formatação textual usada nas análises, sem interpretar HTML.
function formatInline(text) {
    return text.split(/(\*\*[^*]+\*\*|__[^_]+__)/g).map((part, index) =>
        index % 2 === 1
            ? <strong key={index}>{part.slice(2, -2)}</strong>
            : part
    );
}

function InsightText({ text }) {
    const blocks = [];
    for (const rawLine of (text || "").split(/\r?\n/)) {
        const line = rawLine.trim();
        if (!line) continue;

        // Aceita também os títulos Markdown das respostas anteriores.
        const title = line.replace(/^#{1,6}\s+/, "").replace(/\*\*|__/g, "");
        if (/^(?:[123][.)]\s*)?(Diagnóstico|Pontos de atenção|Ações práticas)\s*:?$/i.test(title)) {
            blocks.push({ type: "heading", text: title.replace(/:$/, "") });
        } else if (/^[-*•]\s+/.test(line)) {
            const item = line.replace(/^[-*•]\s+/, "");
            const last = blocks[blocks.length - 1];
            if (last?.type === "list") last.items.push(item);
            else blocks.push({ type: "list", items: [item] });
        } else {
            blocks.push({ type: "paragraph", text: line });
        }
    }

    return <div className="ai-insight-text">
        {blocks.map((block, index) => {
            if (block.type === "heading") return <h3 key={index}>{block.text}</h3>;
            if (block.type === "list") return (
                <ul key={index}>
                    {block.items.map((item, i) => <li key={i}>{formatInline(item)}</li>)}
                </ul>
            );
            return <p key={index}>{formatInline(block.text)}</p>;
        })}
    </div>;
}

export default function AiInsightsPage() {
    const initialPeriod = useMemo(currentPeriod, []);
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

                        <InsightText text={result.insight} />

                        <p className="ai-disclaimer">
                            A análise é educativa e pode conter imprecisões. Confira os dados antes de tomar decisões financeiras.
                        </p>
                    </article>
                </div>
            )}
        </section>
    );
}
