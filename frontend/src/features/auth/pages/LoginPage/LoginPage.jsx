import {useState} from "react";
import { MdOutlineEmail } from "react-icons/md";

import Logo from "../../../../shared/components/Logo/Logo";
import Input from "../../../../shared/components/Input/Input";
import PasswordInput from "../../../../shared/components/PasswordInput/PasswordInput";
import { LoadingOverlay } from "../../../../shared/components/LoadingOverlay/LoadingOverlay";
import InputError from "../../../../shared/components/InputError/InputError";
import Teste from "../../../../shared/components/Teste/Teste";

import { Link } from "react-router-dom";

import { searchAccount } from "../../mockAuth";

import "./LoginPage.css";

const Login = () => {

    const [emailInputValue, setEmailInputValue] = useState("");
    const [passwordInputValue, setPasswordInputValue] = useState("");

    const [emailOrPasswordError, setEmailOrPasswordError] = useState(false);

    const [emailFormatError, setEmailFormatError] = useState(false);
    const [passwordFormatError, setPasswordFormatError] = useState(false);

    const [loading, setLoading] = useState(false);

    const validateEmailFormat = (value) => {
        if (!value) return false;

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        return emailRegex.test(value);
    };

    

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

            if(searchAccount(emailInputValue,passwordInputValue)) {
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

            <div className="login-page">
                <header>
                    <Logo/> 
                    <h1>Bem-vindo de volta</h1>
                    <p>Gerencie suas finanças colaborativas em um só lugar.</p>
                </header>
                <main>
                    

                    {emailOrPasswordError && <InputError/>}

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

                        <Link to={"/recuperar-senha"} className="link-recover">
                            Esqueceu sua senha?
                        </Link>

                        <button type="submit" className="form-button" disabled={emailInputValue === "" || passwordInputValue === ""}>
                            Entrar
                        </button>

                        <p>
                            Ainda não tem uma conta?
                            <Link to={"/cadastrar-conta"} className="link-register">Cadastre-se</Link>
                        </p>
                    </form>
                </main>
            </div>
        </>
        
    )
} 

export default Login;