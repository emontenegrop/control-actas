package actas.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import actas.model.Usuario;

class UsuarioDaoTest {

    private UsuarioDao dao;

    @BeforeEach
    void setUp() {
        dao = new UsuarioDao(TestDatabase.nueva());
    }

    @Test
    void autenticaAlUsuarioDeDemostracion() {
        assertThat(dao.autenticar("admin", "admin123".toCharArray()))
                .get().extracting(Usuario::tipoUsuario).isEqualTo("Administrador");
    }

    @Test
    void rechazaClaveIncorrectaOUsuarioInexistente() {
        assertThat(dao.autenticar("admin", "otra".toCharArray())).isEmpty();
        assertThat(dao.autenticar("nadie", "admin123".toCharArray())).isEmpty();
        assertThat(dao.autenticar("' OR '1'='1", "' OR '1'='1".toCharArray())).isEmpty();
    }

    @Test
    void creaUsuarioYCambiaSuClave() {
        Usuario u = dao.insert("secretaria", null, 2, "clave-segura".toCharArray());
        assertThat(dao.autenticar("secretaria", "clave-segura".toCharArray())).isPresent();

        dao.update(u.id(), "secretaria", null, 2, "nueva-clave-1".toCharArray());
        assertThat(dao.autenticar("secretaria", "clave-segura".toCharArray())).isEmpty();
        assertThat(dao.autenticar("secretaria", "nueva-clave-1".toCharArray())).isPresent();
    }

    @Test
    void actualizarSinClaveConservaLaActual() {
        dao.update(1, "admin", "1722601810", 1, new char[0]);
        assertThat(dao.autenticar("admin", "admin123".toCharArray())).isPresent();
    }
}
