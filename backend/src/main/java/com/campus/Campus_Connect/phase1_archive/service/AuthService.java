package com.campus.Campus_Connect.phase1_archive.service;

import com.campus.Campus_Connect.phase1_archive.model.User;
import com.campus.Campus_Connect.phase1_archive.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

//@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // REGISTER
    public void registerUser(User user) {

        String hashed = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashed);

        userRepository.save(user);
    }

    // LOGIN
    public User loginUser(String identifier, String password) {

        User user;

        if (identifier.contains("@")) {
            user = userRepository.findByEmail(identifier);
        } else {
            user = userRepository.findByUsername(identifier);
        }

        if (user == null) return null;

        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }

        return null;
    }
}