package io.reading_tracker.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
@Converter
@RequiredArgsConstructor
public class StringCryptoConverter implements AttributeConverter<String, String> {

  private static AES256Util encrypter;

  public static void setEncrypter(AES256Util aes256Util) {
    if (encrypter != null) {
      log.warn("Encrypter는 이미 설정되어있습니다");
      return;
    }
    encrypter = aes256Util;
    log.info("Encrypter 설정 완료");
  }

  @Override
  public String convertToDatabaseColumn(String attribute) {
    if (!StringUtils.hasText(attribute)) {
      return null;
    }
    if (encrypter == null) {
      throw new IllegalStateException("Encrypter가 설정되어있지 않습니다");
    }
    return encrypter.encrypt(attribute);
  }

  @Override
  public String convertToEntityAttribute(String dbData) {
    if (!StringUtils.hasText(dbData)) {
      return null;
    }
    if (encrypter == null) {
      throw new IllegalStateException("Encrypter가 설정되어있지 않습니다");
    }
    return encrypter.decrypt(dbData);
  }
}
