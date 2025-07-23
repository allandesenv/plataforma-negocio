package com.plataformanegocios.sufix.authservice.controller;

import com.plataformanegocios.sufix.authservice.dto.AuthRequest;
import com.plataformanegocios.sufix.authservice.dto.AuthResponse;
import com.plataformanegocios.sufix.authservice.model.User; // Importar o modelo User
import com.plataformanegocios.sufix.authservice.repository.UserRepository; // Importar o UserRepository
import com.plataformanegocios.sufix.authservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder; // Importar PasswordEncoder
import org.springframework.web.bind.annotation.*; // Adicionado @GetMapping e @RequestBody

import java.util.Collections; // Para Collections.singletonList

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository; // Injetar o UserRepository

    @Autowired
    private PasswordEncoder passwordEncoder; // Injetar o PasswordEncoder

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (Exception e) {
            throw new Exception("Credenciais inválidas", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/register") // NOVO ENDPOINT DE REGISTRO
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest registrationRequest) {
        // Verifica se o username já existe
        if (userRepository.findByUsername(registrationRequest.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nome de usuário já existe!");
        }

        // Cria um novo usuário
        User newUser = new User();
        newUser.setUsername(registrationRequest.getUsername());
        // Codifica a senha antes de salvar!
        newUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        newUser.setRoles(Collections.singletonList("ROLE_USER")); // Atribui um role padrão

        userRepository.save(newUser); // Salva no MongoDB

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário registrado com sucesso!");
    }

    @GetMapping("/hello")
    public String hello() {
        return "Olá! Você está autenticado!";
    }
}