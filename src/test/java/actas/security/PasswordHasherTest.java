package actas.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    @Test
    void verificaLaClaveCorrecta() {
        String hash = PasswordHasher.hash("s3creta!".toCharArray());
        assertThat(hash).startsWith("pbkdf2$").doesNotContain("s3creta!");
        assertThat(PasswordHasher.verify("s3creta!".toCharArray(), hash)).isTrue();
        assertThat(PasswordHasher.verify("otra".toCharArray(), hash)).isFalse();
    }

    @Test
    void usaSalAleatoria() {
        assertThat(PasswordHasher.hash("x".toCharArray())).isNotEqualTo(PasswordHasher.hash("x".toCharArray()));
    }

    @Test
    void rechazaFormatosInvalidos() {
        assertThat(PasswordHasher.verify("x".toCharArray(), null)).isFalse();
        assertThat(PasswordHasher.verify("x".toCharArray(), "texto-plano")).isFalse();
        assertThat(PasswordHasher.verify("x".toCharArray(), "pbkdf2$1$no-base64$!!")).isFalse();
    }
}
