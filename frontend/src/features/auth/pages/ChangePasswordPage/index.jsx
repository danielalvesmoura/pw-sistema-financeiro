import { useState } from "react";
import PasswordInput from "../../../../shared/components/PasswordInput/PasswordInput";
import { authService } from "../../services/auth.service";
import { isStrongPassword } from "../../utils/validators";
import "./styles.css";

export default function ChangePasswordPage() {
    const [current, setCurrent] = useState("");
    const [password, setPassword] = useState("");
    const [confirm, setConfirm] = useState("");
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const submit = async (e) => {
        e.preventDefault(); setMessage(""); setError("");
        if (!isStrongPassword(password)) return setError("A nova senha deve ter 8 caracteres, maiúscula, minúscula e número.");
        if (password !== confirm) return setError("As senhas não conferem.");
        setLoading(true);
        try {
            const data = await authService.changePassword(current, password);
            setMessage(data.message); setCurrent(""); setPassword(""); setConfirm("");
        } catch (err) { setError(err?.response?.data?.message || "Não foi possível alterar a senha."); }
        finally { setLoading(false); }
    };

    return <section className="change-password-page">
        <h1 className="page-title">Alterar senha</h1>
        <p className="page-subtitle">Confirme sua senha atual antes de trocar.</p>
        <div className="simple-card change-password-card">
            {error && <div className="message error">{error}</div>}
            {message && <div className="message success">{message}</div>}
            <form onSubmit={submit}>
                <PasswordInput label="SENHA ATUAL" value={current} onChange={(e) => setCurrent(e.target.value)} />
                <PasswordInput label="NOVA SENHA" value={password} onChange={(e) => setPassword(e.target.value)} />
                <PasswordInput label="CONFIRMAR NOVA SENHA" value={confirm} onChange={(e) => setConfirm(e.target.value)} />
                <button className="simple-button change-password-button" disabled={loading || !current || !password || !confirm}>Alterar senha</button>
            </form>
        </div>
    </section>;
}
