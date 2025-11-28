package io.reading_tracker.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class AES256UtilTest {

  private static final String VALID_KEY = "01234567890123456789012345678901";
  private static final String INVALID_KEY = "rTQmdJ";

  @Test
  @DisplayName("정상적인 길이의 키로 초기화에 성공해야 한다")
  public void init_withValidKey_success() {
    // given 32자 암호화 키로
    AES256Util aes256Util = new AES256Util();
    ReflectionTestUtils.setField(aes256Util, "secretKey", VALID_KEY);

    // when AES256Util 초기화를 시도하면
    aes256Util.init();

    // then 성공한다
    assertThat(ReflectionTestUtils.getField(aes256Util, "secretKey")).isNotNull();
  }

  @Test
  @DisplayName("암호화 키가 유효하지 않다면 에러를 반환한다")
  public void init_withInvalidKey_throwsError() {
    // given 유효하지 않은 암호화 키로
    AES256Util aes256Util = new AES256Util();
    ReflectionTestUtils.setField(aes256Util, "secretKey", INVALID_KEY);

    // when AES256Util 초기화를 시도하면

    // then 에러를 반환한다
    assertThatThrownBy(() -> aes256Util.init())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("암호화 키가 존재하지 않거나 32자 미만입니다");
  }

  @Test
  @DisplayName("평문을 암호화/복호화하면 원래의 평문과 일치해야 한다")
  public void encryptAndDecrypt_shouldMatchOriginalText() {
    // given 평문을
    AES256Util aes256Util = new AES256Util();
    ReflectionTestUtils.setField(aes256Util, "secretKey", VALID_KEY);
    aes256Util.init();

    String targetText = "This is a secret refresh token";

    // when 암호화/복호화하면
    String encryptedText = aes256Util.encrypt(targetText);
    String decryptedText = aes256Util.decrypt(encryptedText);

    // then 원래의 평문과 일치해야 한다
    assertThat(decryptedText).isNotEqualTo(encryptedText).isNotBlank();
    assertThat(decryptedText).isEqualTo(targetText);
  }
}
