import api from "../configs/axiosConfig";

class UsuarioService {
    async cadastrar(usuario) {
        const { data } = await api.post("/auth/register", {
            name: usuario.nome,
            email: usuario.email,
            password: usuario.senha,
        });
        return data;
    }
}
export default UsuarioService;
