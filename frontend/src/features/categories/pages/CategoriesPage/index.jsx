import { useEffect, useState } from "react";
import { categoryService } from "../../../../services/CategoryService";
import { walletService } from "../../../../services/WalletService";
import "./styles.css";

const emptyForm = {
    name: "",
    type: "EXPENSE",
    icon: "",
    description: "",
    displayOrder: 0,
    active: true,
};

export default function CategoriesPage() {
    const [wallets, setWallets] = useState([]);
    const [walletId, setWalletId] = useState("");
    const [items, setItems] = useState([]);
    const [form, setForm] = useState(emptyForm);
    const [editingId, setEditingId] = useState(null);
    const [error, setError] = useState("");
    const [message, setMessage] = useState("");

    const selectedWallet = wallets.find(
        (wallet) => String(wallet.id) === String(walletId),
    );

    const canEdit = selectedWallet && selectedWallet.role !== "VIEWER";

    const load = async (id = walletId) => {
        if (!id) {
            setItems([]);
            return;
        }

        try {
            setItems(await categoryService.list(id));
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    "Erro ao carregar categorias.",
            );
        }
    };

    useEffect(() => {
        walletService
            .list()
            .then((data) => {
                setWallets(data);

                if (data.length) {
                    setWalletId(String(data[0].id));
                }
            })
            .catch((err) =>
                setError(
                    err?.response?.data?.message ||
                        "Erro ao carregar carteiras.",
                ),
            );
    }, []);

    useEffect(() => {
        load(walletId);
        setEditingId(null);
        setForm(emptyForm);
    }, [walletId]);

    const submit = async (event) => {
        event.preventDefault();
        setError("");
        setMessage("");

        if (!walletId) {
            setError("Selecione uma carteira.");
            return;
        }

        try {
            const payload = {
                ...form,
                displayOrder: Number(form.displayOrder || 0),
                icon: form.icon || null,
                description: form.description || null,
            };

            if (editingId) {
                await categoryService.update(walletId, editingId, payload);
            } else {
                await categoryService.create(walletId, payload);
            }

            setForm(emptyForm);
            setEditingId(null);
            setMessage("Categoria salva.");
            await load();
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    "Não foi possível salvar.",
            );
        }
    };

    const edit = (item) => {
        setEditingId(item.id);

        setForm({
            name: item.name,
            type: item.type,
            icon: item.icon || "",
            description: item.description || "",
            displayOrder: item.displayOrder || 0,
            active: item.active !== false,
        });

        setMessage("");
        setError("");
    };

    const remove = async (id) => {
        if (!window.confirm("Excluir esta categoria?")) {
            return;
        }

        try {
            await categoryService.remove(walletId, id);
            setMessage("Categoria excluída.");
            await load();
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    "Não foi possível excluir.",
            );
        }
    };

    return (
        <section>
            <div className="categories-header">
                <div>
                    <h1 className="page-title">Categorias</h1>
                    <p className="page-subtitle">
                        Suas categorias na carteira selecionada.
                    </p>
                </div>

                {wallets.length > 0 && (
                    <select
                        className="simple-select category-wallet-select"
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
                )}
            </div>

            {error && <div className="message error">{error}</div>}
            {message && <div className="message success">{message}</div>}

            {wallets.length === 0 && (
                <div className="simple-card">
                    Você precisa criar ou participar de uma carteira.
                </div>
            )}

            {wallets.length > 0 && (
                <div className="two-columns">
                    <div className="simple-card">
                        <h2>
                            {editingId ? "Editar categoria" : "Nova categoria"}
                        </h2>

                        {!canEdit && (
                            <div className="message">
                                Seu papel é VIEWER. Você pode apenas visualizar
                                suas categorias.
                            </div>
                        )}

                        {canEdit && (
                            <form className="simple-form" onSubmit={submit}>
                                <label>
                                    Nome
                                    <input
                                        value={form.name}
                                        maxLength={80}
                                        required
                                        onChange={(event) =>
                                            setForm({
                                                ...form,
                                                name: event.target.value,
                                            })
                                        }
                                    />
                                </label>

                                <label>
                                    Tipo
                                    <select
                                        value={form.type}
                                        onChange={(event) =>
                                            setForm({
                                                ...form,
                                                type: event.target.value,
                                            })
                                        }
                                    >
                                        <option value="EXPENSE">Despesa</option>
                                        <option value="INCOME">Receita</option>
                                    </select>
                                </label>

                                <label>
                                    Descrição
                                    <input
                                        maxLength={255}
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
                                    Ícone
                                    <input
                                        maxLength={80}
                                        value={form.icon}
                                        onChange={(event) =>
                                            setForm({
                                                ...form,
                                                icon: event.target.value,
                                            })
                                        }
                                        placeholder="Opcional"
                                    />
                                </label>

                                <label>
                                    Ordem de exibição
                                    <input
                                        type="number"
                                        min="0"
                                        value={form.displayOrder}
                                        onChange={(event) =>
                                            setForm({
                                                ...form,
                                                displayOrder: event.target.value,
                                            })
                                        }
                                    />
                                </label>

                                <label className="checkbox-line">
                                    <input
                                        type="checkbox"
                                        checked={form.active}
                                        onChange={(event) =>
                                            setForm({
                                                ...form,
                                                active: event.target.checked,
                                            })
                                        }
                                    />
                                    Categoria ativa
                                </label>

                                <div className="inline-actions">
                                    <button
                                        className="simple-button"
                                        type="submit"
                                    >
                                        {editingId ? "Salvar" : "Adicionar"}
                                    </button>

                                    {editingId && (
                                        <button
                                            className="simple-button secondary"
                                            type="button"
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

                    <div className="simple-card category-list-card">
                        <h2>Minhas categorias</h2>

                        {items.length === 0 ? (
                            <p>Nenhuma categoria cadastrada.</p>
                        ) : (
                            <table className="simple-table">
                                <thead>
                                    <tr>
                                        <th>Nome</th>
                                        <th>Tipo</th>
                                        <th>Status</th>
                                        <th>Ações</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {items.map((item) => (
                                        <tr key={item.id}>
                                            <td>{item.name}</td>
                                            <td>
                                                {item.type === "INCOME"
                                                    ? "Receita"
                                                    : "Despesa"}
                                            </td>
                                            <td>
                                                {item.active
                                                    ? "Ativa"
                                                    : "Inativa"}
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
