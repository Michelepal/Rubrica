package it.michelepal.rubrica.config;

import it.michelepal.rubrica.entity.AppUser;
import it.michelepal.rubrica.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DataInitializer {

    @Bean
    CommandLineRunner demoUser(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                AppUser user = new AppUser();
                user.setUsername("admin");
                user.setEmail("admin@example.local");
                user.setPasswordHash(passwordEncoder.encode("admin"));
                user.setFirstName("Admin");
                user.setLastName("Demo");
                userRepository.save(user);
            }
        };
    }
}

