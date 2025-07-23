package com.plataformanegocios.sufix.authservice.repository;

import com.plataformanegocios.sufix.authservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

// UserRepository estende MongoRepository para operações CRUD em documentos User
public interface UserRepository extends MongoRepository<User, String> {
    // Encontra um usuário pelo username
    Optional<User> findByUsername(String username);
}