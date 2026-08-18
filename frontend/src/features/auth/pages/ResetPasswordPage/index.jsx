import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Logo from "../../../../shared/components/Logo/Logo";
import ArrowBack from "../../../../shared/components/ArrowBack/ArrowBack";
import PasswordInput from "../../../../shared/components/PasswordInput/PasswordInput";
import { LoadingOverlay } from "../../../../shared/components/LoadingOverlay/LoadingOverlay";
import { authService } from "../../services/auth.service";
import { isStrongPassword } from "../../utils/validators";
import "../ForgotPasswordPage/styles.css";

export default function ResetPasswordPage() {
    const { token } = useParams();
    const navigate = useNavigate();
    const [password, setPassword] = useState("");
    const [confirm, setConfirm] = useState("");
    const [error, setError] = useState(
        token ? "" : "Token ausente ou inválido.",
    );
    const [loading, setLoading] = useState(false);

    const submit = async (event) => {
        event.preventDefault();

        if (!token) {
            setError("Token ausente ou inválido.");
            return;
        }

        if (!isStrongPassword(password)) {
            setError(
                "Use no mínimo 8 caracteres, com maiúscula, minúscula e número.",
            );
            return;
        }

        if (password !== confirm) {
            setError("As senhas não conferem.");
            return;
        }

        setLoading(true);
        setError("");

        try {
            await authService.resetPassword(token, password);

            navigate("/login", {
                state: {
                    message: "Senha redefinida com sucesso.",
                },
            });
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    "Não foi possível redefinir a senha.",
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            {loading && <LoadingOverlay />}

            <div className="auth-simple-page">
                <main>
                    <header>
                        <div className="auth-first-line">
                            <ArrowBack url="/login" />
                            <Logo />
                        </div>

                        <h1>Redefinir senha</h1>
                        <p>Defina sua nova senha.</p>
                    </header>

                    {error && <div className="message error">{error}</div>}

                    <form onSubmit={submit}>
                        <PasswordInput
                            label="NOVA SENHA"
                            value={password}
                            onChange={(event) =>
                                setPassword(event.target.value)
                            }
                        />

                        <PasswordInput
                            label="CONFIRMAR SENHA"
                            value={confirm}
                            onChange={(event) => setConfirm(event.target.value)}
                        />

                        <button
                            type="submit"
                            disabled={
                                !token || !password || !confirm || loading
                            }
                        >
                            Redefinir senha
                        </button>
                    </form>

                    <Link className="auth-bottom-link" to="/login">
                        Voltar ao login
                    </Link>
                </main>
            </div>
        </>
    );
}
