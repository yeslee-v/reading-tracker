package io.reading_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ReadingTrackerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReadingTrackerApplication.class, args);
  }
}
