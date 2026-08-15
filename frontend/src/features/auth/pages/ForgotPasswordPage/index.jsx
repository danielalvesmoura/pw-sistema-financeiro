import { useState } from "react";
import { Link } from "react-router-dom";
import { MdOutlineEmail } from "react-icons/md";
import Logo from "../../../../shared/components/Logo/Logo";
import ArrowBack from "../../../../shared/components/ArrowBack/ArrowBack";
import Input from "../../../../shared/components/Input/Input";
import { LoadingOverlay } from "../../../../shared/components/LoadingOverlay/LoadingOverlay";
import { authService } from "../../services/auth.service";
import { isValidEmail } from "../../utils/validators";
import "./styles.css";

export default function ForgotPasswordPage() {
    const [email, setEmail] = useState("");
    const [message, setMessage] = useState("");
    const [token, setToken] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const submit = async (e) => {
        e.preventDefault();
        if (!isValidEmail(email)) return setError("Informe um e-mail válido.");
        setLoading(true); setError(""); setToken("");
        try {
            const data = await authService.forgotPassword(email);
            setMessage(data.message);
            setToken(data.debugToken || "");
        } catch (err) {
            setError(err?.response?.data?.message || "Não foi possível solicitar a recuperação.");
        } finally { setLoading(false); }
    };

    return (
        <>
            {loading && <LoadingOverlay />}
            <div className="forgot-page auth-simple-page">
                <main>
                    <header><div className="auth-first-line"><ArrowBack url="/login" /><Logo /></div><h1>Recuperar senha</h1><p>Informe seu e-mail.</p></header>
                    {error && <div className="message error">{error}</div>}
                    {message && <div className="message success">{message}</div>}
                    {token && <div className="message"><strong>Teste local:</strong> <Link to={`/redefinir-senha/${token}`}>abrir redefinição de senha</Link></div>}
                    <form onSubmit={submit}>
                        <Input label="E-MAIL" icon={<MdOutlineEmail />} placeholder="exemplo@email.com" value={email} onChange={(e) => setEmail(e.target.value)} />
                        <button type="submit" disabled={!email || loading}>Solicitar recuperação</button>
                    </form>
                    <Link className="auth-bottom-link" to="/login">Voltar ao login</Link>
                </main>
            </div>
        </>
    );
}
