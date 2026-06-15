import "./PasswordInput.css";
import {useState} from "react";

import { LuEye } from "react-icons/lu";
import { LuEyeOff } from "react-icons/lu";

export default function PasswordInput({
    label,
    type = "text",
    value,
    onChange,
    placeholder,
    name,
}) {
    const [show, setShow] = useState(false);

    return (
        <div className="password-input-container">
            <label className="input-label">{label}</label>

            <div className="wrapper">
                <input
                    className={`password-input-field`}
                    type={show ? "text" : "password"}
                    value={value}
                    onChange={onChange}
                    placeholder={placeholder}
                    name={name}
                />
                
                <span className="password-icon" onClick={() => setShow(!show)}>
                    {show ? <LuEyeOff /> : <LuEye/>}
                </span>
            </div>
            
        </div>
    );
}