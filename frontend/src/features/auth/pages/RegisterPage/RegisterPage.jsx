import UsuarioService from "../../../../services/UsuarioService";

import { useState } from "react";
import { MdOutlineEmail } from "react-icons/md";
import { FaUser } from "react-icons/fa";

import Logo from "../../../../shared/components/Logo/Logo";
import ArrowBack from "../../../../shared/components/ArrowBack/ArrowBack";
import Input from "../../../../shared/components/Input/Input";
import PasswordInput from "../../../../shared/components/PasswordInput/PasswordInput";
import { LoadingOverlay } from "../../../../shared/components/LoadingOverlay/LoadingOverlay";

import { Link, useNavigate } from "react-router-dom";

import "./RegisterPage.css";

const usuarioService = new UsuarioService();

const RegisterPage = () => {
    const navigate = useNavigate();

    const [name, setName] = useState("");
    const [emailInputValue, setEmailInputValue] = useState("");
    const [passwordInputValue, setPasswordInputValue] = useState("");
    const [confirmPasswordInputValue, setConfirmPasswordInputValue] =
        useState("");

    const [nameError, setNameError] = useState(false);
    const [emailFormatError, setEmailFormatError] = useState(false);
    const [passwordFormatError, setPasswordFormatError] = useState(false);
    const [confirmPasswordError, setConfirmPasswordError] = useState(false);

    const [errorMessage, setErrorMessage] = useState("");
    const [successMessage, setSuccessMessage] = useState("");

    const [loading, setLoading] = useState(false);

    const validateEmailFormat = (value) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(value);
    };

    const calculatePasswordStrength = (password) => {
        let score = 0;

        if (password.length >= 8) score++;
        if (/[A-Z]/.test(password)) score++;
        if (/[a-z]/.test(password)) score++;
        if (/[0-9]/.test(password)) score++;
        if (/[^A-Za-z0-9]/.test(password)) score++;

        if (score <= 2) {
            return {
                label: "Fraca",
                className: "weak"
            };
        }

        if (score <= 4) {
            return {
                label: "Média",
                className: "medium"
            };
        }

        return {
            label: "Forte",
            className: "strong"
        };
    };

    const validatePasswordFormat = (password) => {
        return (
            password.length >= 8 &&
            /[A-Z]/.test(password) &&
            /[a-z]/.test(password) &&
            /[0-9]/.test(password)
        );
    };

    const passwordStrength =
        passwordInputValue.length > 0
            ? calculatePasswordStrength(passwordInputValue)
            : null;

    const handleSubmit = async (e) => {
        e.preventDefault();

        setErrorMessage("");
        setSuccessMessage("");

        let hasError = false;

        if (!name.trim()) {
            setNameError(true);
            hasError = true;
        } else {
            setNameError(false);
        }

        if (!validateEmailFormat(emailInputValue)) {
            setEmailFormatError(true);
            hasError = true;
        } else {
            setEmailFormatError(false);
        }

        if (!validatePasswordFormat(passwordInputValue)) {
            setPasswordFormatError(true);
            hasError = true;
        } else {
            setPasswordFormatError(false);
        }

        if (passwordInputValue !== confirmPasswordInputValue) {
            setConfirmPasswordError(true);
            hasError = true;
        } else {
            setConfirmPasswordError(false);
        }

        if (hasError) return;

        setLoading(true);

        try {
            const usuario = {
                nome: name,
                email: emailInputValue,
                senha: passwordInputValue
            };

            await usuarioService.cadastrar(usuario);

            setSuccessMessage(
                "Cadastro realizado com sucesso! Redirecionando..."
            );

            setTimeout(() => {
                navigate("/");
            }, 1500);
        } catch (erroCadastro) {
            const mensagem =
                erroCadastro?.response?.data?.mensagem ||
                "Não foi possível realizar o cadastro.";

            setErrorMessage(mensagem);
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            {loading && <LoadingOverlay />}

            <div className="register-page">
                <main>
                    <header>
                        <div className="first-line">
                            <ArrowBack url="/" />
                            <Logo />
                        </div>

                        <h1>Cadastre-se</h1>
                        <p>
                            Preencha os campos para criar sua conta.
                        </p>
                    </header>

                    {errorMessage && (
                        <div className="alert-message error">
                            {errorMessage}
                        </div>
                    )}

                    {successMessage && (
                        <div className="alert-message success">
                            {successMessage}
                        </div>
                    )}

                    <form onSubmit={handleSubmit}>

                        <Input
                            label="NOME COMPLETO"
                            placeholder="Seu nome"
                            icon={<FaUser />}
                            value={name}
                            onChange={(e) => {
                                setName(e.target.value);
                                setNameError(false);
                            }}
                        />

                        {nameError && (
                            <span className="error-message">
                                Nome é obrigatório.
                            </span>
                        )}

                        <Input
                            label="E-MAIL"
                            placeholder="exemplo@email.com"
                            icon={<MdOutlineEmail />}
                            value={emailInputValue}
                            onChange={(e) => {
                                setEmailInputValue(e.target.value);

                                if (e.target.value) {
                                    setEmailFormatError(
                                        !validateEmailFormat(
                                            e.target.value
                                        )
                                    );
                                }
                            }}
                        />

                        {emailFormatError && (
                            <span className="error-message">
                                Informe um e-mail válido.
                            </span>
                        )}

                        <PasswordInput
                            label="SENHA"
                            placeholder="••••••••"
                            value={passwordInputValue}
                            onChange={(e) => {
                                setPasswordInputValue(
                                    e.target.value
                                );

                                setPasswordFormatError(
                                    !validatePasswordFormat(
                                        e.target.value
                                    )
                                );
                            }}
                        />

                        {passwordStrength && (
                            <div className="password-strength">

                                <div
                                    className={`strength-bar ${passwordStrength.className}`}
                                />

                                <span>
                                    Força: {passwordStrength.label}
                                </span>

                            </div>
                        )}

                        {passwordFormatError && (
                            <span className="error-message">
                                Mínimo 8 caracteres,
                                maiúscula,
                                minúscula
                                e número.
                            </span>
                        )}

                        <PasswordInput
                            label="CONFIRMAR SENHA"
                            placeholder="••••••••"
                            value={confirmPasswordInputValue}
                            onChange={(e) =>
                                setConfirmPasswordInputValue(
                                    e.target.value
                                )
                            }
                            onBlur={() =>
                                setConfirmPasswordError(
                                    passwordInputValue !==
                                    confirmPasswordInputValue
                                )
                            }
                        />

                        {confirmPasswordError && (
                            <span className="error-message">
                                As senhas não conferem.
                            </span>
                        )}

                        <button
                            type="submit"
                            className="form-button"
                            disabled={
                                loading ||
                                !name ||
                                !emailInputValue ||
                                !passwordInputValue ||
                                !confirmPasswordInputValue
                            }
                        >
                            Criar conta
                        </button>

                        <p>
                            Já tem uma conta?

                            <Link
                                to="/"
                                className="link-register"
                            >
                                Entrar
                            </Link>

                        </p>
                    </form>
                </main>
            </div>
        </>
    );
};

export default RegisterPage;