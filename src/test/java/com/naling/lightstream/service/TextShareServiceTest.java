package com.naling.lightstream.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Rewriting with a mocked Redis in session 5")
class TextShareServiceTest {
    private TextShareService service;

    @BeforeEach
    void setUp(){
        //service = new TextShareService();
    }

    @Test
    void sharedTextCanBeClaimed(){
        // Act
        String code = service.share("hello");
        String result = service.claim(code);
        // Assert
        assertEquals("hello", result);
    }

    @Test
    void unknownCodeReturnNull(){
        // Act
        String result = service.claim("KKKK");
        // Assert
        assertNull(result);
    }

    @Test
    void codeHasFourCharacters(){
        // Act
        String code = service.share("random");
        // Assert
        assertEquals(4, code.length());
    }

    @Test
    void twoSharesDiffCodes(){
        // Act
        String code1 = service.share("first");
        String code2 = service.share("second");
        // Assert
        assertNotEquals(code1, code2);
    }

    @Test
    void sharesNeverCollide(){
        // Set up
        Set<String> set = new HashSet<>();
        // Act
        for (int i = 0; i < 10000; i++){
            String code = service.share("hello");
            set.add(code);
        }
        // Assert
        assertEquals(10000, set.size());
    }


}
