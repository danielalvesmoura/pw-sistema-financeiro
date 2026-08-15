import "./App.css";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import Login from "./features/auth/pages/LoginPage/LoginPage";
import Register from "./features/auth/pages/RegisterPage/RegisterPage";
import ForgotPasswordPage from "./features/auth/pages/ForgotPasswordPage";
import ResetPasswordPage from "./features/auth/pages/ResetPasswordPage";
import ChangePasswordPage from "./features/auth/pages/ChangePasswordPage";
import DashboardPage from "./features/dashboard/pages/DashboardPage";
import TransactionsPage from "./features/transactions/pages/TransactionsPage";
import CategoriesPage from "./features/categories/pages/CategoriesPage";
import WalletsPage from "./features/wallets/pages/WalletsPage";
import AppLayout from "./shared/components/AppLayout";
import ProtectedRoute, { PublicOnlyRoute } from "./shared/components/ProtectedRoute";

function App() {
  return (
    <div className="App">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<PublicOnlyRoute><Login /></PublicOnlyRoute>} />
          <Route path="/cadastrar-conta" element={<PublicOnlyRoute><Register /></PublicOnlyRoute>} />
          <Route path="/recuperar-senha" element={<PublicOnlyRoute><ForgotPasswordPage /></PublicOnlyRoute>} />
          <Route path="/redefinir-senha" element={<PublicOnlyRoute><ResetPasswordPage /></PublicOnlyRoute>} />
          <Route path="/redefinir-senha/:token" element={<PublicOnlyRoute><ResetPasswordPage /></PublicOnlyRoute>} />

          <Route path="/app" element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
            <Route index element={<DashboardPage />} />
            <Route path="transacoes" element={<TransactionsPage />} />
            <Route path="categorias" element={<CategoriesPage />} />
            <Route path="carteiras" element={<WalletsPage />} />
            <Route path="perfil/senha" element={<ChangePasswordPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
