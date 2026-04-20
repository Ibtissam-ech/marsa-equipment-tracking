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
        if (user != null && "password".equals(user.getPassword()) && "ADMIN".equals(user.getRole())) {
            java.util.HashMap<String, Object> userMap = new java.util.HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("fullName", user.getFullName() != null ? user.getFullName() : user.getUsername());
            userMap.put("role", user.getRole());
            java.util.HashMap<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("user", userMap);
            response.put("token", "demo-token");
            return response;
        }
        return java.util.Map.of("success", false, "message", "Invalid credentials");
    }
}