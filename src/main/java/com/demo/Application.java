package com.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;

@RestController
@SpringBootApplication
public class Application {

    @RequestMapping("/")
    String home() {
        return "Hello from EKS DevOps(GANESH PRASAD MAITY,I love devops and cloud   i am from odisha) 🚀";
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
