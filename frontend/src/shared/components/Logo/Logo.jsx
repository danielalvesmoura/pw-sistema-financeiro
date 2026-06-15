import { FaWallet } from "react-icons/fa";

import "./Logo.css";
import "../../../styles/global.css";

const Logo = () => {
    return (
        <div className="logo-container">
            <FaWallet className="icon"/>
            <span>Wallet</span>
        </div>
    )
}

export default Logo;