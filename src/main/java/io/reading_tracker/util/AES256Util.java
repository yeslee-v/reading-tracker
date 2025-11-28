package io.reading_tracker.util;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AES256Util {

  private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
  private SecretKeySpec secretKeySpec;
  private IvParameterSpec ivParameterSpec;

  @Value("${encrypt.secret-key}")
  private String secretKey;

  public String encrypt(String plainText) {
    if (this.secretKeySpec == null) {
      throw new IllegalStateException("[encrypt] AES256Util이 초기화되지 않았습니다");
    }
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
      byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

      return Base64.getEncoder().encodeToString(encrypted);
    } catch (Exception e) {
      throw new RuntimeException("암호화 중 오류가 발생했습니다: ", e);
    }
  }

  public String decrypt(String cipherText) {
    if (secretKeySpec == null) {
      throw new IllegalArgumentException("[decrypt] AES256Util이 초기화되지 않았습니다");
    }
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
      byte[] decoded = Base64.getDecoder().decode(cipherText);

      return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("복호화 중 오류가 발생했습니다: ", e);
    }
  }

  @PostConstruct
  public void init() {
    if (secretKey == null || secretKey.length() < 32) {
      throw new IllegalArgumentException("암호화 키가 존재하지 않거나 32자 미만입니다");
    }

    byte[] keyBytes = new byte[32];
    byte[] b = secretKey.getBytes(StandardCharsets.UTF_8);
    int len = Math.min(b.length, keyBytes.length);
    System.arraycopy(b, 0, keyBytes, 0, len);
    secretKeySpec = new SecretKeySpec(keyBytes, "AES");

    ivParameterSpec = new IvParameterSpec(keyBytes, 0, 16);
  }
}
