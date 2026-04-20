package com.marsamaroc.equipment.controller;

import com.marsamaroc.equipment.model.entity.User;
import com.marsamaroc.equipment.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Controller
public class PageController {
    private final UserRepository userRepo;
    
    public PageController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @PostMapping("/api/auth/login")
    @ResponseBody
    public Map<String, Object> doLogin(@RequestParam String username, @RequestParam String password) {
        User user = userRepo.findByUsername(username).orElse(null);
        if (user != null && user.getPassword().equals(password) && "ADMIN".equals(user.getRole())) {
            return Map.of("success", true, "user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "fullName", user.getFullName(),
                "role", user.getRole()
            ), "token", "demo-token");
        }
        return Map.of("success", false, "message", "Invalid credentials");
    }
}