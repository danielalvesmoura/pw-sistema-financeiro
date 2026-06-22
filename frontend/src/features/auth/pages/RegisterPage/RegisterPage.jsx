import UsuarioService from "../../../../services/UsuarioService";

import { useState } from "react";
import { MdOutlineEmail } from "react-icons/md";


import Logo from "../../../../shared/components/Logo/Logo";
import ArrowBack from "../../../../shared/components/ArrowBack/ArrowBack";
import Input from "../../../../shared/components/Input/Input";
import PasswordInput from "../../../../shared/components/PasswordInput/PasswordInput";
import { LoadingOverlay } from "../../../../shared/components/LoadingOverlay/LoadingOverlay";
import InputError from "../../../../shared/components/InputError/InputError";
import Teste from "../../../../shared/components/Teste/Teste";

import { Link } from "react-router-dom";

import { searchAccount } from "../../mockAuth";

import "./RegisterPage.css";

const usuarioService = new UsuarioService();

const RegisterPage = () => {

    const [emailInputValue, setEmailInputValue] = useState("");
    const [passwordInputValue, setPasswordInputValue] = useState("");
    const [confirmPasswordInputValue, setConfirmPasswordInputValue] = useState("");

    const [emailOrPasswordError, setEmailOrPasswordError] = useState(false);

    const [emailFormatError, setEmailFormatError] = useState(false);
    const [passwordFormatError, setPasswordFormatError] = useState(false);

    const [loading, setLoading] = useState(false);

    const validateEmailFormat = (value) => {
        if (!value) return false;

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        return emailRegex.test(value);
    };

    // const register = () => {
    //     try {
    //         UsuarioService.inserir(dados);
    //     } catch (erroCadstro) {
    //         const mensagem = erroCadstro?.response?.data?.mensagem || "Não foi possível realizar o cadastro.";
    //     }
    // }



    const handleSubmit = (e) => {
        e.preventDefault();

        setLoading(true);

        setTimeout(() => {
            if (validateEmailFormat(emailInputValue)) {
                setEmailFormatError(false);
            } else {
                setEmailFormatError(true);
                return;
            }

            if (searchAccount(emailInputValue, passwordInputValue)) {
                setEmailOrPasswordError(false);
            } else {
                setEmailOrPasswordError(true);
            }

            setLoading(false);

        }, 1000)

    }

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
                        <p>Preencha os campos para criar uma nova conta.</p>
                    </header>


                    {emailOrPasswordError && <InputError />}

                    <form onSubmit={handleSubmit}>
                        <Input
                            label="E-MAIL"
                            placeholder="exemplo@email.com"
                            icon={<MdOutlineEmail />}
                            onChange={(e) => setEmailInputValue(e.target.value)}
                        />
                        {emailFormatError && <span className="error-message">Email inválido</span>}

                        <PasswordInput
                            label="SENHA"
                            placeholder="••••••••••"
                            onChange={(e) => setPasswordInputValue(e.target.value)}
                        />
                        {passwordFormatError && <span className="error-message">Senha inválida</span>}

                        <PasswordInput
                            label="CONFIRMAR SENHA"
                            placeholder="••••••••••"
                            onChange={(e) => setPasswordInputValue(e.target.value)}
                        />
                        {passwordFormatError && <span className="error-message">Senha inválida</span>}

                        <button type="submit" className="form-button" disabled={emailInputValue === "" || passwordInputValue === ""}>
                            Criar conta
                        </button>

                        <p>
                            Já tem uma conta?
                            <Link to={"/cadastrar-conta"} className="link-register">Entrar</Link>
                        </p>
                    </form>
                </main>
            </div>
        </>

    )
}

export default RegisterPage;