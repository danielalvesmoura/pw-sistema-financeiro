import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import Logo from "../Logo/Logo";
import api from "../../../configs/axiosConfig";
import "./styles.css";

export default function AppLayout() {
    const navigate = useNavigate();
    const [user, setUser] = useState(() => JSON.parse(localStorage.getItem("usuario") || "null"));

    useEffect(() => {
        api.get("/api/v1/users/me").then(({ data }) => {
            setUser(data);
            localStorage.setItem("usuario", JSON.stringify(data));
        }).catch(() => {});
    }, []);

    const logout = () => {
        localStorage.removeItem("app-token");
        localStorage.removeItem("usuario");
        navigate("/login");
    };

    return (
        <div className="app-layout simple-page">
            <aside className="app-sidebar">
                <Logo />
                <nav>
                    <NavLink to="/app" end>Dashboard</NavLink>
                    <NavLink to="/app/transacoes">Transações</NavLink>
                    <NavLink to="/app/categorias">Categorias</NavLink>
                    <NavLink to="/app/carteiras">Carteiras</NavLink>
                    <NavLink to="/app/perfil/senha">Alterar senha</NavLink>
                </nav>
                <div className="sidebar-user">
                    <strong>{user?.name || "Usuário"}</strong>
                    <button type="button" onClick={logout}>Sair</button>
                </div>
            </aside>
            <main className="app-content"><Outlet /></main>
        </div>
    );
}
