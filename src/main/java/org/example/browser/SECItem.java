package org.example.browser;

import java.nio.charset.StandardCharsets;

// Рекорд для общения с нативной либой
public record SECItem(int type, byte[] data, int len) {
    public String decodeData ( ) {
        return new String(data, 0, len, StandardCharsets.UTF_8);
    }
}

