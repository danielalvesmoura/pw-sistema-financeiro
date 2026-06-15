import {useState} from "react";
import { MdOutlineEmail } from "react-icons/md";

import Logo from "../../../../shared/components/Logo/Logo";
import Input from "../../../../shared/components/Input/Input";
import PasswordInput from "../../../../shared/components/PasswordInput/PasswordInput";
import InputError from "../../../../shared/components/InputError/InputError";
import Teste from "../../../../shared/components/Teste/Teste";

import "./LoginPage.css";

const Login = () => {


    return (
        <div className="login-page">
            <header>
                <Logo/>
                <h1>Bem-vindo de volta</h1>
                <p>Gerencie suas finanças colaborativas em um só lugar.</p>
            </header>
            <main>
                <InputError/>

                <form>
                    <Input
                        label="E-MAIL"
                        placeholder="exemplo@email.com"
                        icon={<MdOutlineEmail />}
                    />
                    <PasswordInput
                        label="SENHA"
                        placeholder="••••••••••"
                    />
                </form>
            </main>
        </div>
    )
} 

export default Login;