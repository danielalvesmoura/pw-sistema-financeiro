import { MdErrorOutline } from "react-icons/md";

import "./InputError.css";

const InputError = () => {
    return (
        <div className="error-conteiner">
            <MdErrorOutline className="icon"/>
            <p>E-mail ou senha incorretos</p>
        </div>
    )
}

export default InputError;