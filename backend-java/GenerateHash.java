import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBKDFKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class GenerateHash {
    public static void main(String[] args) throws Exception {
        String password = "senha123456";
        int iterations = 65536;
        int keyLength = 256;
        
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        
        PBKDFKeySpec spec = new PBKDFKeySpec(
            password.toCharArray(),
            salt,
            iterations,
            keyLength
        );
        
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        
        String saltB64 = Base64.getEncoder().encodeToString(salt);
        String hashB64 = Base64.getEncoder().encodeToString(hash);
        String result = String.format("PBKDF2$%d$%s$%s", iterations, saltB64, hashB64);
        
        System.out.println(result);
    }
}
