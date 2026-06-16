import {useState} from "react";
import { MdOutlineEmail } from "react-icons/md";

import Logo from "../../../../shared/components/Logo/Logo";
import Input from "../../../../shared/components/Input/Input";
import PasswordInput from "../../../../shared/components/PasswordInput/PasswordInput";
import InputError from "../../../../shared/components/InputError/InputError";
import Teste from "../../../../shared/components/Teste/Teste";

import "./LoginPage.css";

const Login = () => {

    const [emailInputValue, setEmailInputValue] = useState("");
    const [passwordInputValue, setPasswordInputValue] = useState("");

    const [emailOrPasswordError, setEmailOrPasswordError] = useState(false);

    const validateEmail = (value) => {
        if (!value) return false;

        // Validação para e-mail (contém '@')
        const isEmailFormat = value.includes("@");
        
        if (isEmailFormat) {
            // Validação de e-mail um pouco mais robusta
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            return emailRegex.test(value);
        } else {
            // Nome de usuário não pode ter espaços
            return !value.includes(" ");
        }
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        console.log("asdf");

        if (validateEmail(emailInputValue)) {

        } else {
            setEmailOrPasswordError(true);
        }
    }

    return (
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
                    <PasswordInput
                        label="SENHA"
                        placeholder="••••••••••"
                        onChange={(e) => setPasswordInputValue(e.target.value)}
                    />

                    <button type="submit" className="form-button" disabled={emailInputValue === "" || passwordInputValue === ""}>
                        Entrar
                    </button>
                </form>
            </main>
        </div>
    )
} 

export default Login;