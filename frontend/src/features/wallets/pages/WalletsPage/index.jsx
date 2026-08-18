import { useEffect, useState } from "react";
import { walletService } from "../../../../services/WalletService";
import "./styles.css";

const emptyWallet = {
    name: "",
    description: "",
    currency: "BRL",
    initialBalance: "0",
    archived: false,
};

const emptyMember = {
    email: "",
    role: "VIEWER",
};

export default function WalletsPage() {
    const [wallets, setWallets] = useState([]);
    const [selectedId, setSelectedId] = useState("");
    const [members, setMembers] = useState([]);
    const [walletForm, setWalletForm] = useState(emptyWallet);
    const [editForm, setEditForm] = useState(emptyWallet);
    const [memberForm, setMemberForm] = useState(emptyMember);
    const [editing, setEditing] = useState(false);
    const [error, setError] = useState("");
    const [message, setMessage] = useState("");

    const selected = wallets.find(
        (wallet) => String(wallet.id) === String(selectedId),
    );

    const isOwner = selected?.role === "OWNER";

    const payload = (form) => ({
        ...form,
        initialBalance: Number(form.initialBalance || 0),
    });

    const loadWallets = async (keepSelection = true) => {
        const data = await walletService.list();
        setWallets(data);

        const currentSelectionStillExists = data.some(
            (wallet) => String(wallet.id) === String(selectedId),
        );

        if (!keepSelection || !currentSelectionStillExists) {
            setSelectedId(data[0]?.id ? String(data[0].id) : "");
        }
    };

    const loadMembers = async (id) => {
        if (!id) {
            setMembers([]);
            return;
        }

        try {
            setMembers(await walletService.members(id));
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    "Erro ao carregar membros.",
            );
        }
    };

    useEffect(() => {
        loadWallets(false).catch(() =>
            setError("Erro ao carregar carteiras."),
        );
    }, []);

    useEffect(() => {
        loadMembers(selectedId);
        setEditing(false);
    }, [selectedId]);

    const createWallet = async (event) => {
        event.preventDefault();
        setError("");
        setMessage("");

        try {
            const created = await walletService.create(payload(walletForm));

            setWalletForm(emptyWallet);
            setMessage("Carteira criada.");
            await loadWallets(false);
            setSelectedId(String(created.id));
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    "Não foi possível criar a carteira.",
            );
        }
    };

    const saveWallet = async (event) => {
        event.preventDefault();

        try {
            await walletService.update(selectedId, payload(editForm));
            setMessage("Carteira atualizada.");
            setEditing(false);
            await loadWallets(true);
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    "Não foi possível atualizar.",
            );
        }
    };

    const startEdit = () => {
        setEditForm({
            name: selected.name,
            description: selected.description || "",
            currency: selected.currency || "BRL",
            initialBalance: String(selected.initialBalance || 0),
            archived: Boolean(selected.archived),
        });

        setEditing(true);
    };

    const removeWallet = async () => {
        if (!window.confirm("Excluir a carteira e todas as transações?")) {
            return;
        }

        try {
            await walletService.remove(selectedId);
            setMessage("Carteira excluída.");
            setMembers([]);
            await loadWallets(false);
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    "Não foi possível excluir.",
            );
        }
    };

    const addMember = async (event) => {
        event.preventDefault();
        setError("");

        try {
            await walletService.addMember(selectedId, memberForm);
            setMemberForm(emptyMember);
            setMessage("Membro adicionado.");
            await loadMembers(selectedId);
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    "Não foi possível adicionar o membro.",
            );
        }
    };

    const changeRole = async (userId, role) => {
        try {
            await walletService.updateMember(selectedId, userId, role);
            await loadMembers(selectedId);
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    "Não foi possível alterar o papel.",
            );
        }
    };

    const removeMember = async (userId) => {
        if (!window.confirm("Remover este membro?")) {
            return;
        }

        try {
            await walletService.removeMember(selectedId, userId);
            await loadMembers(selectedId);
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    "Não foi possível remover o membro.",
            );
        }
    };

    const walletFields = (form, setForm) => (
        <>
            <label>
                Nome
                <input
                    required
                    value={form.name}
                    onChange={(event) =>
                        setForm({
                            ...form,
                            name: event.target.value,
                        })
                    }
                />
            </label>

            <label>
                Descrição
                <textarea
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
                Moeda
                <select
                    value={form.currency}
                    onChange={(event) =>
                        setForm({
                            ...form,
                            currency: event.target.value,
                        })
                    }
                >
                    <option value="BRL">BRL</option>
                    <option value="USD">USD</option>
                    <option value="EUR">EUR</option>
                </select>
            </label>

            <label>
                Saldo inicial
                <input
                    type="number"
                    step="0.01"
                    min="0"
                    value={form.initialBalance}
                    onChange={(event) =>
                        setForm({
                            ...form,
                            initialBalance: event.target.value,
                        })
                    }
                />
            </label>
        </>
    );

    return (
        <section>
            <h1 className="page-title">Carteiras</h1>
            <p className="page-subtitle">
                Crie carteiras e compartilhe com outras contas.
            </p>

            {error && <div className="message error">{error}</div>}
            {message && <div className="message success">{message}</div>}

            <div className="wallets-top-grid">
                <div className="simple-card">
                    <h2>Nova carteira</h2>

                    <form className="simple-form" onSubmit={createWallet}>
                        {walletFields(walletForm, setWalletForm)}
                        <button className="simple-button">
                            Criar carteira
                        </button>
                    </form>
                </div>

                <div className="simple-card">
                    <h2>Minhas carteiras</h2>

                    {wallets.length === 0 ? (
                        <p>Nenhuma carteira.</p>
                    ) : (
                        <div className="wallet-list">
                            {wallets.map((wallet) => (
                                <button
                                    className={
                                        String(wallet.id) ===
                                        String(selectedId)
                                            ? "wallet-item selected"
                                            : "wallet-item"
                                    }
                                    onClick={() =>
                                        setSelectedId(String(wallet.id))
                                    }
                                    key={wallet.id}
                                >
                                    <strong>{wallet.name}</strong>
                                    <span>
                                        {wallet.currency || "BRL"} · {wallet.role}
                                    </span>
                                </button>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            {selected && (
                <div className="simple-card wallet-details">
                    <div className="wallet-detail-header">
                        <div>
                            <h2>{selected.name}</h2>
                            <p>{selected.description || "Sem descrição"}</p>
                            <small>
                                Moeda: {selected.currency || "BRL"} · Saldo
                                inicial: {selected.initialBalance || 0} · Seu
                                papel: {selected.role}
                            </small>
                        </div>

                        {isOwner && !editing && (
                            <div className="inline-actions">
                                <button
                                    className="small-action"
                                    onClick={startEdit}
                                >
                                    Editar
                                </button>

                                <button
                                    className="small-action danger-link"
                                    onClick={removeWallet}
                                >
                                    Excluir
                                </button>
                            </div>
                        )}
                    </div>

                    {editing && (
                        <form
                            className="simple-form wallet-edit-form"
                            onSubmit={saveWallet}
                        >
                            {walletFields(editForm, setEditForm)}

                            <label className="checkbox-line">
                                <input
                                    type="checkbox"
                                    checked={editForm.archived}
                                    onChange={(event) =>
                                        setEditForm({
                                            ...editForm,
                                            archived: event.target.checked,
                                        })
                                    }
                                />
                                Arquivada
                            </label>

                            <div className="inline-actions">
                                <button className="simple-button">
                                    Salvar
                                </button>

                                <button
                                    className="simple-button secondary"
                                    type="button"
                                    onClick={() => setEditing(false)}
                                >
                                    Cancelar
                                </button>
                            </div>
                        </form>
                    )}

                    <h3>Membros</h3>

                    {isOwner && (
                        <form className="member-form" onSubmit={addMember}>
                            <input
                                type="email"
                                placeholder="email@exemplo.com"
                                required
                                value={memberForm.email}
                                onChange={(event) =>
                                    setMemberForm({
                                        ...memberForm,
                                        email: event.target.value,
                                    })
                                }
                            />

                            <select
                                value={memberForm.role}
                                onChange={(event) =>
                                    setMemberForm({
                                        ...memberForm,
                                        role: event.target.value,
                                    })
                                }
                            >
                                <option value="VIEWER">Visualizador</option>
                                <option value="EDITOR">Editor</option>
                            </select>

                            <button className="simple-button">
                                Adicionar
                            </button>
                        </form>
                    )}

                    <div className="members-table-wrap">
                        <table className="simple-table">
                            <thead>
                                <tr>
                                    <th>Nome</th>
                                    <th>E-mail</th>
                                    <th>Papel</th>
                                    <th>Ações</th>
                                </tr>
                            </thead>

                            <tbody>
                                {members.map((member) => (
                                    <tr key={member.userId}>
                                        <td>{member.name}</td>
                                        <td>{member.email}</td>

                                        <td>
                                            {isOwner &&
                                            member.role !== "OWNER" ? (
                                                <select
                                                    value={member.role}
                                                    onChange={(event) =>
                                                        changeRole(
                                                            member.userId,
                                                            event.target.value,
                                                        )
                                                    }
                                                >
                                                    <option value="VIEWER">
                                                        VIEWER
                                                    </option>
                                                    <option value="EDITOR">
                                                        EDITOR
                                                    </option>
                                                </select>
                                            ) : (
                                                member.role
                                            )}
                                        </td>

                                        <td>
                                            {isOwner &&
                                                member.role !== "OWNER" && (
                                                    <button
                                                        className="small-action danger-link"
                                                        onClick={() =>
                                                            removeMember(
                                                                member.userId,
                                                            )
                                                        }
                                                    >
                                                        Remover
                                                    </button>
                                                )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </section>
    );
}
