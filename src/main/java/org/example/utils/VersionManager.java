package org.example.utils;

import java.io.IOException;
import java.util.Arrays;

public class VersionManager {
    private static final String[] VERSION_INFO = { "1", "1", "1", "+git" };

    // Проверка версии на GIT
    public static String getVersion ( ) {
        try {
            Process process = new ProcessBuilder("git", "describe", "--tags").redirectError(ProcessBuilder.Redirect.INHERIT).start( );
            int exitCode = process.waitFor( );
            if (exitCode == 0) {
                return process.inputReader( ).readLine( ).trim( );
            } else {
                return getInternalVersion( );
            }
        } catch (IOException | InterruptedException e) {
            return getInternalVersion( );
        }
    }

    private static String getInternalVersion ( ) {
        return String.join(".", Arrays.copyOfRange(VERSION_INFO, 0, 3)) + String.join("", Arrays.copyOfRange(VERSION_INFO, 3, VERSION_INFO.length));
    }
}

