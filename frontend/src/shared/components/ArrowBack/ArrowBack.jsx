import { FaWallet } from "react-icons/fa";

import "./ArrowBack.css";
import "../../../styles/global.css";
import { IoMdArrowRoundBack } from "react-icons/io";

import { Link } from "react-router-dom";

const Logo = (props) => {
    return (
        <Link to={props.url}>
            <IoMdArrowRoundBack className="arrowback-icon" />
        </Link> 
    )
}

export default Logo;