import { authService } from "../services/auth.service";
export default function useAuth() {
    return { isAuthenticated: !!localStorage.getItem("app-token"), logout: authService.logout };
}
