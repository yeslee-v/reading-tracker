package io.reading_tracker.config;

import io.reading_tracker.util.AES256Util;
import io.reading_tracker.util.StringCryptoConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EncryptInitListener implements ApplicationListener<ContextRefreshedEvent> {

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    if (event.getApplicationContext().getParent() == null) {
      try {
        AES256Util aes256Util = event.getApplicationContext().getBean(AES256Util.class);

        StringCryptoConverter.setEncrypter(aes256Util);
        log.info("Encryption 초기화 완료");
      } catch (Exception e) {
        log.error("Encryption 동작 에러", e);

        throw new RuntimeException("Encryption 초기화 실패: ", e);
      }
    }
  }
}
