package com.api.codenest;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class HomeController {
    
    @GetMapping("/test")
    public String index() {
        System.out.println("Hello World!");
        return "index";
    }
    


}
