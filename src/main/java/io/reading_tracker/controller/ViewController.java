package io.reading_tracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

  @GetMapping({"/", "/app", "/books", "/dashboard"})
  public String index() {
    return "forward:/index.html";
  }
}
