package com.xt.interview.controller;

import com.xt.interview.entity.Account;
import com.xt.interview.repository.AccountRepository;
import com.xt.interview.util.JwtUtil;
import com.xt.interview.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Integer NOT_DELETED = 0;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        Map<String, Object> response = new HashMap<>();

        if (username == null || username.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "用户名不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        if (password == null || password.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "密码不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Account account = accountRepository.findByUsernameAndDeleted(username.trim(), NOT_DELETED)
                    .orElse(null);

            if (account == null) {
                response.put("success", false);
                response.put("message", "用户名或密码错误");
                return ResponseEntity.badRequest().body(response);
            }

            if (!account.getPassword().equals(MD5Util.encode(password))) {
                response.put("success", false);
                response.put("message", "用户名或密码错误");
                return ResponseEntity.badRequest().body(response);
            }

            if (account.getStatus() != 1) {
                response.put("success", false);
                response.put("message", "账号已禁用");
                return ResponseEntity.badRequest().body(response);
            }

            String token = jwtUtil.generateToken(account.getId(), account.getUsername(), account.getRoleLevel());

            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", token);

            Map<String, Object> user = new HashMap<>();
            user.put("id", account.getId());
            user.put("username", account.getUsername());
            user.put("name", account.getName());
            user.put("gender", account.getGender());
            user.put("phone", account.getPhone());
            user.put("roleLevel", account.getRoleLevel());
            user.put("status", account.getStatus());
            response.put("user", user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "登录失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "退出登录成功");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> response = new HashMap<>();

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.put("valid", false);
            response.put("message", "未提供token");
            return ResponseEntity.ok(response);
        }

        String token = authorization.substring(7);

        try {
            if (!jwtUtil.isTokenValid(token)) {
                response.put("valid", false);
                response.put("message", "token已过期");
                return ResponseEntity.ok(response);
            }

            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            Integer roleLevel = jwtUtil.getRoleLevelFromToken(token);

            response.put("valid", true);
            response.put("message", "token有效");
            response.put("user", Map.of(
                "id", userId,
                "username", username,
                "roleLevel", roleLevel
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "token无效: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
