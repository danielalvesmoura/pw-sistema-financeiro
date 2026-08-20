import { useEffect, useState } from "react";
import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    Tooltip,
    Legend,
    ResponsiveContainer,
} from "recharts";
import { Link } from "react-router-dom";
import { walletService } from "../../../../services/WalletService";
import { transactionService } from "../../../transactions/services/transactions.service";
import { formatDate } from "../../../../shared/utils/date";
import "./styles.css";

const money = (value, currency = "BRL") =>
    Number(value || 0).toLocaleString("pt-BR", {
        style: "currency",
        currency,
    });

export default function DashboardPage() {
    const [wallets, setWallets] = useState([]);
    const [walletId, setWalletId] = useState("");
    const [summary, setSummary] = useState(null);
    const [recent, setRecent] = useState([]);
    const [loading, setLoading] = useState(true);
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
            .finally(() => setLoading(false));
    }, []);

    useEffect(() => {
        if (!walletId) {
            setSummary(null);
            setRecent([]);
            return;
        }

        setLoading(true);

        Promise.all([
            transactionService.summary(walletId),
            transactionService.list(walletId, {
                page: 0,
                size: 5,
                sort: "date,desc",
            }),
        ]).then(([summaryData, page]) => {
                setSummary(summaryData);
                setRecent(page.content || []);
            })
        .catch((err) =>
                setError(
                    err?.response?.data?.message ||
                        "Erro ao carregar dashboard.",
                ),
            )
        .finally(() => setLoading(false));
    }, [walletId]);

    const selectedWallet = wallets.find(
        (wallet) => String(wallet.id) === String(walletId),
    );

    const currency = selectedWallet?.currency || "BRL";

    return (
        <section className="dashboard-page">
            <div className="dashboard-header">
                <div>
                    <h1 className="page-title">Dashboard</h1>
                    <p className="page-subtitle">
                        Resumo simples da sua carteira.
                    </p>
                </div>

                {wallets.length > 0 && (
                    <select
                        className="simple-select wallet-select"
                        value={walletId}
                        onChange={(event) =>
                            setWalletId(event.target.value)
                        }
                    >
                        {wallets.map((wallet) => (
                            <option value={wallet.id} key={wallet.id}>
                                {wallet.name}
                            </option>
                        ))}
                    </select>
                )}
            </div>

            {error && <div className="message error">{error}</div>}
            {loading && <div className="message">Carregando...</div>}

            {!loading && wallets.length === 0 && (
                <div className="simple-card empty-wallet">
                    Você ainda não tem carteira.{" "}
                    <Link to="/app/carteiras">Criar carteira</Link>
                </div>
            )}

            {summary && (
                <>
                    <div className="metrics-grid">
                        <div className="simple-card metric">
                            <span>Saldo atual</span>
                            <strong>
                                {money(summary.balance, currency)}
                            </strong>
                        </div>

                        <div className="simple-card metric income">
                            <span>Receitas</span>
                            <strong>
                                {money(summary.totalIncome, currency)}
                            </strong>
                        </div>

                        <div className="simple-card metric expense">
                            <span>Despesas</span>
                            <strong>
                                {money(summary.totalExpense, currency)}
                            </strong>
                        </div>
                    </div>

                    <div className="dashboard-grid">
                        <div className="simple-card chart-card">
                            <h2>Receitas e despesas por mês</h2>

                            {summary.byMonth?.length ? (
                                <div className="chart-box">
                                    <ResponsiveContainer
                                        width="100%"
                                        height="100%"
                                    >
                                        <BarChart data={summary.byMonth}>
                                            <XAxis dataKey="month" />
                                            <YAxis />
                                            <Tooltip
                                                formatter={(value) =>
                                                    money(value, currency)
                                                }
                                            />
                                            <Legend />
                                            <Bar
                                                dataKey="income"
                                                name="Receitas"
                                                fill="#15803d"
                                            />
                                            <Bar
                                                dataKey="expense"
                                                name="Despesas"
                                                fill="#b91c1c"
                                            />
                                        </BarChart>
                                    </ResponsiveContainer>
                                </div>
                            ) : (
                                <p>Sem dados para o gráfico.</p>
                            )}
                        </div>

                        <div className="simple-card recent-card">
                            <h2>Lançamentos recentes</h2>

                            {recent.length === 0 ? (
                                <p>Nenhum lançamento.</p>
                            ) : (
                                <div className="recent-list">
                                    {recent.map((item) => (
                                        <div
                                            className="recent-item"
                                            key={item.id}
                                        >
                                            <div>
                                                <strong>
                                                    {item.description ||
                                                        item.categoryName ||
                                                        "Sem descrição"}
                                                </strong>
                                                <small>
                                                    {formatDate(item.date)}
                                                </small>
                                            </div>

                                            <span
                                                className={
                                                    item.type === "INCOME"
                                                        ? "value-income"
                                                        : "value-expense"
                                                }
                                            >
                                                {item.type === "INCOME"
                                                    ? "+"
                                                    : "-"}
                                                {money(item.amount, currency)}
                                            </span>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                </>
            )}
        </section>
    );
}
