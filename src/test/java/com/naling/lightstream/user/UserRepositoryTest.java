package com.naling.lightstream.user;

import com.naling.lightstream.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void savesAndFindsUserByEmail() {
        User saved = userRepository.save(new User("alice@example.com", "hash"));

        assertNotNull(saved.getId());
        Optional<User> found = userRepository.findByEmail("alice@example.com");
        assertTrue(found.isPresent());
        assertEquals("hash", found.get().getPasswordHash());
    }

    @Test
    void unknownEmailReturnsEmpty() {
        assertTrue(userRepository.findByEmail("nobody@example.com").isEmpty());
    }

    @Test
    void duplicateEmailIsRejected() {
        userRepository.save(new User("alice@example.com", "hash"));

        assertThrows(DataIntegrityViolationException.class,
                () -> userRepository.save(new User("alice@example.com", "other")));
    }
}
