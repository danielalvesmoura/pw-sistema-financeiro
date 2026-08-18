import "./PasswordInput.css";
import { useState } from "react";
import { LuEye, LuEyeOff } from "react-icons/lu";

export default function PasswordInput({
    label,
    value,
    onChange,
    placeholder,
    name,
    ...rest
}) {
    const [show, setShow] = useState(false);

    return (
        <div className="password-input-container">
            <label className="input-label">{label}</label>

            <div className="wrapper">
                <input
                    className="password-input-field"
                    type={show ? "text" : "password"}
                    value={value}
                    onChange={onChange}
                    placeholder={placeholder}
                    name={name}
                    {...rest}
                />

                <span
                    className="password-icon"
                    onClick={() => setShow(!show)}
                    role="button"
                    aria-label="Mostrar ou ocultar senha"
                >
                    {show ? <LuEyeOff /> : <LuEye />}
                </span>
            </div>
        </div>
    );
}
