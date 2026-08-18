import { Navigate } from "react-router-dom";

export default function ProtectedRoute({ children }) {
    return localStorage.getItem("app-token") ? children : <Navigate to="/login" replace />;
}

export function PublicOnlyRoute({ children }) {
    return localStorage.getItem("app-token") ? <Navigate to="/app" replace /> : children;
}
