import { useEffect, useMemo, useState } from "react";
import { walletService } from "../../../../services/WalletService";
import { categoryService } from "../../../../services/CategoryService";
import { transactionService } from "../../services/transactions.service";
import { formatDate } from "../../../../shared/utils/date";
import "./styles.css";

const today = new Date().toISOString().slice(0, 10);

const emptyForm = {
    type: "EXPENSE",
    amount: "",
    description: "",
    date: today,
    categoryId: "",
    notes: "",
    paymentMethod: "",
};

const money = (value, currency = "BRL") =>
    Number(value || 0).toLocaleString("pt-BR", {
        style: "currency",
        currency,
    });

export default function TransactionsPage() {
    const [wallets, setWallets] = useState([]);
    const [walletId, setWalletId] = useState("");
    const [categories, setCategories] = useState([]);
    const [items, setItems] = useState([]);
    const [form, setForm] = useState(emptyForm);
    const [editingId, setEditingId] = useState(null);
    const [error, setError] = useState("");
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);
    const [exportType, setExportType] = useState("ALL");
    const [exportFormat, setExportFormat] = useState("XLSX");
    const [exporting, setExporting] = useState(false);

    const selectedWallet = wallets.find(
        (wallet) => String(wallet.id) === String(walletId),
    );

    const canEdit = selectedWallet && selectedWallet.role !== "VIEWER";

    const availableCategories = useMemo(
        () =>
            categories.filter(
                (category) =>
                    category.type === form.type && category.active !== false,
            ),
        [categories, form.type],
    );

    const loadTransactions = async (id = walletId) => {
        if (!id) {
            setItems([]);
            return;
        }

        setLoading(true);

        try {
            const page = await transactionService.list(id, {
                page: 0,
                size: 50,
                sort: "date,desc",
            });

            setItems(page.content || []);
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    "Erro ao carregar transações.",
            );
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        walletService
            .list()
            .then((walletData) => {
                setWallets(walletData);

                if (walletData.length) {
                    setWalletId(String(walletData[0].id));
                }
            })
            .catch((err) =>
                setError(
                    err?.response?.data?.message ||
                        "Erro ao carregar dados.",
                ),
            );
    }, []);

    useEffect(() => {
        loadTransactions(walletId);

        if (walletId) {
            categoryService
                .list(walletId)
                .then(setCategories)
                .catch((err) =>
                    setError(
                        err?.response?.data?.message ||
                            "Erro ao carregar categorias.",
                    ),
                );
        } else {
            setCategories([]);
        }

        setEditingId(null);
        setForm(emptyForm);
    }, [walletId]);

    const submit = async (event) => {
        event.preventDefault();
        setError("");
        setMessage("");

        if (!walletId) {
            setError("Crie ou selecione uma carteira.");
            return;
        }

        const payload = {
            type: form.type,
            amount: Number(form.amount),
            description: form.description || null,
            date: form.date,
            categoryId: form.categoryId ? Number(form.categoryId) : null,
            notes: form.notes || null,
            paymentMethod: form.paymentMethod || null,
            attachmentUrl: null,
        };

        try {
            if (editingId) {
                await transactionService.update(
                    walletId,
                    editingId,
                    payload,
                );
            } else {
                await transactionService.create(walletId, payload);
            }

            setForm(emptyForm);
            setEditingId(null);
            setMessage("Transação salva.");
            await loadTransactions();
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    "Não foi possível salvar a transação.",
            );
        }
    };

    const edit = (item) => {
        setEditingId(item.id);

        setForm({
            type: item.type,
            amount: String(item.amount),
            description: item.description || "",
            date: item.date,
            categoryId: item.categoryId ? String(item.categoryId) : "",
            notes: item.notes || "",
            paymentMethod: item.paymentMethod || "",
        });

        window.scrollTo({
            top: 0,
            behavior: "smooth",
        });
    };

    const remove = async (id) => {
        if (!window.confirm("Excluir esta transação?")) {
            return;
        }

        try {
            await transactionService.remove(walletId, id);
            setMessage("Transação excluída.");
            await loadTransactions();
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    "Não foi possível excluir.",
            );
        }
    };

    const exportWallet = async () => {
        if (!walletId) {
            return;
        }

        setExporting(true);
        setError("");

        try {
            const response = await transactionService.export(
                walletId,
                exportType,
                exportFormat,
            );

            const disposition = response.headers?.["content-disposition"] || "";
            const match = disposition.match(/filename="?([^";]+)"?/i);
            const fallback = `carteira_${walletId}.${exportFormat.toLowerCase()}`;
            const filename = match?.[1] || fallback;
            const url = window.URL.createObjectURL(response.data);
            const link = document.createElement("a");

            link.href = url;
            link.download = filename;

            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    "Não foi possível exportar a carteira.",
            );
        } finally {
            setExporting(false);
        }
    };

    return (
        <section>
            <div className="transactions-header">
                <div>
                    <h1 className="page-title">Transações</h1>
                    <p className="page-subtitle">
                        Receitas e despesas da carteira selecionada.
                    </p>
                </div>

                {wallets.length > 0 && (
                    <div className="transactions-header-actions">
                        <select
                            className="simple-select transaction-wallet-select"
                            value={walletId}
                            onChange={(event) =>
                                setWalletId(event.target.value)
                            }
                        >
                            {wallets.map((wallet) => (
                                <option key={wallet.id} value={wallet.id}>
                                    {wallet.name} - {wallet.role}
                                </option>
                            ))}
                        </select>

                        <div className="export-controls">
                            <select
                                className="simple-select"
                                value={exportType}
                                onChange={(event) =>
                                    setExportType(event.target.value)
                                }
                            >
                                <option value="ALL">Carteira inteira</option>
                                <option value="INCOME">Apenas receitas</option>
                                <option value="EXPENSE">Apenas despesas</option>
                            </select>

                            <select
                                className="simple-select"
                                value={exportFormat}
                                onChange={(event) =>
                                    setExportFormat(event.target.value)
                                }
                            >
                                <option value="XLSX">XLSX</option>
                                <option value="TXT">TXT</option>
                            </select>

                            <button
                                type="button"
                                className="simple-button export-button"
                                onClick={exportWallet}
                                disabled={exporting}
                            >
                                {exporting ? "Exportando..." : "Exportar"}
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {error && <div className="message error">{error}</div>}
            {message && <div className="message success">{message}</div>}

            {wallets.length === 0 && (
                <div className="simple-card">
                    Você precisa criar uma carteira antes de lançar transações.
                </div>
            )}

            {wallets.length > 0 && (
                <div className="two-columns transactions-grid">
                    <div className="simple-card">
                        <h2>
                            {editingId ? "Editar transação" : "Nova transação"}
                        </h2>

                        {!canEdit && (
                            <div className="message">
                                Seu papel é VIEWER. Você pode apenas visualizar.
                            </div>
                        )}

                        {canEdit && (
                            <form className="simple-form" onSubmit={submit}>
                                <label>
                                    Tipo
                                    <select
                                        value={form.type}
                                        onChange={(event) =>
                                            setForm({
                                                ...form,
                                                type: event.target.value,
                                                categoryId: "",
                                            })
                                        }
                                    >
                                        <option value="EXPENSE">Despesa</option>
                                        <option value="INCOME">Receita</option>
                                    </select>
                                </label>

                                <label>
                                    Valor
                                    <input
                                        type="number"
                                        step="0.01"
                                        min="0.01"
                                        required
                                        value={form.amount}
                                        onChange={(event) =>
                                            setForm({
                                                ...form,
                                                amount: event.target.value,
                                            })
                                        }
                                    />
                                </label>

                                <label>
                                    Descrição
                                    <input
                                        maxLength="255"
                                        value={form.description}
                                        onChange={(event) =>
                                            setForm({
                                                ...form,
                                                description: event.target.value,
                                            })
                                        }
                                    />
                                </label>

                                <label>
                                    Data
                                    <input
                                        type="date"
                                        max={today}
                                        required
                                        value={form.date}
                                        onChange={(event) =>
                                            setForm({
                                                ...form,
                                                date: event.target.value,
                                            })
                                        }
                                    />
                                </label>

                                <label>
                                    Categoria
                                    <select
                                        value={form.categoryId}
                                        onChange={(event) =>
                                            setForm({
                                                ...form,
                                                categoryId: event.target.value,
                                            })
                                        }
                                    >
                                        <option value="">Sem categoria</option>
                                        {availableCategories.map((category) => (
                                            <option
                                                key={category.id}
                                                value={category.id}
                                            >
                                                {category.name}
                                            </option>
                                        ))}
                                    </select>
                                </label>

                                <label>
                                    Forma de pagamento
                                    <select
                                        value={form.paymentMethod}
                                        onChange={(event) =>
                                            setForm({
                                                ...form,
                                                paymentMethod: event.target.value,
                                            })
                                        }
                                    >
                                        <option value="">Não informado</option>
                                        <option value="PIX">PIX</option>
                                        <option value="CARTAO">Cartão</option>
                                        <option value="DINHEIRO">Dinheiro</option>
                                        <option value="TRANSFERENCIA">
                                            Transferência
                                        </option>
                                    </select>
                                </label>

                                <label>
                                    Observações
                                    <textarea
                                        maxLength="1000"
                                        value={form.notes}
                                        onChange={(event) =>
                                            setForm({
                                                ...form,
                                                notes: event.target.value,
                                            })
                                        }
                                    />
                                </label>

                                <div className="inline-actions">
                                    <button className="simple-button">
                                        {editingId ? "Salvar" : "Adicionar"}
                                    </button>

                                    {editingId && (
                                        <button
                                            type="button"
                                            className="simple-button secondary"
                                            onClick={() => {
                                                setEditingId(null);
                                                setForm(emptyForm);
                                            }}
                                        >
                                            Cancelar
                                        </button>
                                    )}
                                </div>
                            </form>
                        )}
                    </div>

                    <div className="simple-card transactions-list-card">
                        <h2>Lançamentos</h2>

                        {loading ? (
                            <p>Carregando...</p>
                        ) : items.length === 0 ? (
                            <p>Nenhuma transação.</p>
                        ) : (
                            <table className="simple-table">
                                <thead>
                                    <tr>
                                        <th>Descrição</th>
                                        <th>Data</th>
                                        <th>Valor</th>
                                        <th>Ações</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {items.map((item) => (
                                        <tr key={item.id}>
                                            <td>
                                                <strong>
                                                    {item.description ||
                                                        "Sem descrição"}
                                                </strong>
                                                <small className="transaction-category">
                                                    {item.categoryName ||
                                                        "Sem categoria"}
                                                </small>
                                            </td>

                                            <td>{formatDate(item.date)}</td>

                                            <td
                                                className={
                                                    item.type === "INCOME"
                                                        ? "value-income"
                                                        : "value-expense"
                                                }
                                            >
                                                {item.type === "INCOME"
                                                    ? "+"
                                                    : "-"}
                                                {money(
                                                    item.amount,
                                                    selectedWallet?.currency ||
                                                        "BRL",
                                                )}
                                            </td>

                                            <td>
                                                {canEdit && (
                                                    <div className="inline-actions">
                                                        <button
                                                            className="small-action"
                                                            onClick={() =>
                                                                edit(item)
                                                            }
                                                        >
                                                            Editar
                                                        </button>

                                                        <button
                                                            className="small-action danger-link"
                                                            onClick={() =>
                                                                remove(item.id)
                                                            }
                                                        >
                                                            Excluir
                                                        </button>
                                                    </div>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )}
                    </div>
                </div>
            )}
        </section>
    );
}
