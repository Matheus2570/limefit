package br.com.limefit.autenticacao;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public final class Senhas {
    private static final int ITERACOES = 600_000;
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final String HASH_AUSENTE = criar("conta-inexistente-" + ALEATORIO.nextLong());

    private Senhas() {}

    public static String criar(String senha) {
        byte[] salt = new byte[16];
        ALEATORIO.nextBytes(salt);
        return "pbkdf2-sha256$" + ITERACOES + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(derivar(senha, salt, ITERACOES));
    }

    public static boolean verificar(String senha, String hash) {
        // Também deriva uma senha para conta ausente, mantendo o erro de login genérico.
        if (hash == null) { verificar(senha, HASH_AUSENTE); return false; }
        try {
            String[] partes = hash.split("\\$");
            if (partes.length != 4 || !partes[0].equals("pbkdf2-sha256")) return false;
            int iteracoes = Integer.parseInt(partes[1]);
            if (iteracoes < 100_000 || iteracoes > 2_000_000) return false;
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            return salt.length == 16 && esperado.length == 32
                    && MessageDigest.isEqual(esperado, derivar(senha, salt, iteracoes));
        } catch (IllegalArgumentException erro) {
            return false;
        }
    }

    private static byte[] derivar(String senha, byte[] salt, int iteracoes) {
        char[] caracteres = senha.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(caracteres, salt, iteracoes, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException erro) {
            throw new IllegalStateException("PBKDF2 indisponível no JDK", erro);
        } finally {
            spec.clearPassword();
            Arrays.fill(caracteres, '\0');
        }
    }
}
