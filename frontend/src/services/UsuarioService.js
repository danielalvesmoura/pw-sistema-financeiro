import BaseService from './BaseService';

class UsuarioService extends BaseService {
    constructor() {
        super('/usuario');
    }

    async cadastrar(usuario) {
        const resposta = await this.api.post(this.endPoint, usuario);
        return resposta.data;
    }

    async buscarPorId(id) {
        const resposta = await this.api.get(`${this.endPoint}/${id}`);
        return resposta.data;
    }

    async atualizar(id, usuario) {
        const resposta = await this.api.put(
            `${this.endPoint}/${id}`,
            usuario
        );

        return resposta.data;
    }

    async remover(id) {
        const resposta = await this.api.delete(
            `${this.endPoint}/${id}`
        );

        return resposta.data;
    }
}

export default UsuarioService;