import { useState } from "react";
import { MdOutlineEmail } from "react-icons/md";
import { Link, useLocation, useNavigate } from "react-router-dom";
import Logo from "../../../../shared/components/Logo/Logo";
import Input from "../../../../shared/components/Input/Input";
import PasswordInput from "../../../../shared/components/PasswordInput/PasswordInput";
import { LoadingOverlay } from "../../../../shared/components/LoadingOverlay/LoadingOverlay";
import { authService } from "../../services/auth.service";
import { isValidEmail } from "../../utils/validators";
import "./LoginPage.css";

export default function Login() {
    const navigate = useNavigate();
    const location = useLocation();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!isValidEmail(email)) return setError("Informe um e-mail válido.");
        if (password.length < 6) return setError("A senha deve ter pelo menos 6 caracteres.");
        setLoading(true);
        setError("");
        try {
            await authService.login(email, password);
            navigate("/app");
        } catch (err) {
            setError(err?.response?.data?.message || "E-mail ou senha inválidos.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            {loading && <LoadingOverlay />}
            <div className="login-page">
                <main>
                    <header>
                        <Logo />
                        <h1>Bem-vindo de volta</h1>
                        <p>Gerencie suas finanças colaborativas em um só lugar.</p>
                    </header>
                    {location.state?.message && <div className="message success">{location.state.message}</div>}
                    {error && <div className="message error">{error}</div>}
                    <form onSubmit={handleSubmit}>
                        <Input label="E-MAIL" placeholder="exemplo@email.com" icon={<MdOutlineEmail />} value={email} onChange={(e) => setEmail(e.target.value)} />
                        <PasswordInput label="SENHA" placeholder="••••••••" value={password} onChange={(e) => setPassword(e.target.value)} />
                        <Link to="/recuperar-senha" className="link-recover">Esqueceu sua senha?</Link>
                        <button type="submit" disabled={loading || !email || !password}>Entrar</button>
                        <p>Ainda não tem uma conta? <Link to="/cadastrar-conta" className="link-register">Cadastre-se</Link></p>
                    </form>
                </main>
            </div>
        </>
    );
}
