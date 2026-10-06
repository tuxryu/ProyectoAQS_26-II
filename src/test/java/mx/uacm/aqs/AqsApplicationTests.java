package mx.uacm.aqs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class AqsApplicationTests {

    @Test
    void mainNoDebeFallar() {
        assertDoesNotThrow(() -> AqsApplication.main(new String[] {}));
    }
}
