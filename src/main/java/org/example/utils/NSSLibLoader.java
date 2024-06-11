package org.example.utils;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class NSSLibLoader {
    // Поиск и загрузка нативной библиотеки
    public static void loadLib ( ) {
        List<String> locations = new ArrayList<>( );
        String nssName;

        String nssLibPath = System.getenv("NSS_LIB_PATH");
        if (nssLibPath != null && !nssLibPath.isEmpty( )) {
            locations.add(nssLibPath);
        }

        String osName = System.getProperty("os.name");
        boolean is64Bit = System.getProperty("os.arch").contains("64");

        if (osName.startsWith("Windows")) {
            nssName = "nss3.dll";
            if (!is64Bit) {
                locations.add("");
                locations.add("C:\\Program Files (x86)\\Mozilla Firefox");
                locations.add("C:\\Program Files (x86)\\Firefox Developer Edition");
                locations.add("C:\\Program Files (x86)\\Mozilla Thunderbird");
                locations.add("C:\\Program Files (x86)\\Nightly");
                locations.add("C:\\Program Files (x86)\\SeaMonkey");
                locations.add("C:\\Program Files (x86)\\Waterfox");
            }
            locations.add("");
            locations.add(Paths.get(System.getProperty("user.home"), "AppData", "Local", "Mozilla Firefox").toString( ));
            locations.add(Paths.get(System.getProperty("user.home"), "AppData", "Local", "Firefox Developer Edition").toString( ));
            locations.add(Paths.get(System.getProperty("user.home"), "AppData", "Local", "Mozilla Thunderbird").toString( ));
            locations.add(Paths.get(System.getProperty("user.home"), "AppData", "Local", "Nightly").toString( ));
            locations.add(Paths.get(System.getProperty("user.home"), "AppData", "Local", "SeaMonkey").toString( ));
            locations.add(Paths.get(System.getProperty("user.home"), "AppData", "Local", "Waterfox").toString( ));
            locations.add("C:\\Program Files\\Mozilla Firefox");
            locations.add("C:\\Program Files\\Firefox Developer Edition");
            locations.add("C:\\Program Files\\Mozilla Thunderbird");
            locations.add("C:\\Program Files\\Nightly");
            locations.add("C:\\Program Files\\SeaMonkey");
            locations.add("C:\\Program Files\\Waterfox");

            String[] software = { "firefox", "thunderbird", "waterfox", "seamonkey" };
            for (String binary : software) {
                String location = findExecutableLocation(binary);
                if (location != null) {
                    locations.add(Paths.get(location, nssName).toString( ));
                }
            }
        } else if (osName.startsWith("Mac")) {
            nssName = "libnss3.dylib";
            locations.add("");
            locations.add("/usr/local/lib/nss");
            locations.add("/usr/local/lib");
            locations.add("/opt/local/lib/nss");
            locations.add("/sw/lib/firefox");
            locations.add("/sw/lib/mozilla");
            locations.add("/usr/local/opt/nss/lib");
            locations.add("/opt/pkg/lib/nss");
            locations.add("/Applications/Firefox.app/Contents/MacOS");
            locations.add("/Applications/Thunderbird.app/Contents/MacOS");
            locations.add("/Applications/SeaMonkey.app/Contents/MacOS");
            locations.add("/Applications/Waterfox.app/Contents/MacOS");
        } else {
            nssName = "libnss3.so";
            if (is64Bit) {
                locations.add("");
                locations.add("/usr/lib64");
                locations.add("/usr/lib64/nss");
                locations.add("/usr/lib");
                locations.add("/usr/lib/nss");
                locations.add("/usr/local/lib");
                locations.add("/usr/local/lib/nss");
                locations.add("/opt/local/lib");
                locations.add("/opt/local/lib/nss");
                locations.add(Paths.get(System.getProperty("user.home"), ".nix-profile", "lib").toString( ));
            } else {
                locations.add("");
                locations.add("/usr/lib");
                locations.add("/usr/lib/nss");
                locations.add("/usr/lib32");
                locations.add("/usr/lib32/nss");
                locations.add("/usr/lib64");
                locations.add("/usr/lib64/nss");
                locations.add("/usr/local/lib");
                locations.add("/usr/local/lib/nss");
                locations.add("/opt/local/lib");
                locations.add("/opt/local/lib/nss");
                locations.add(Paths.get(System.getProperty("user.home"), ".nix-profile", "lib").toString( ));
            }
        }

        // Load the library
        for (String location : locations) {
            File libFile = new File(location, nssName);
            if (libFile.exists( )) {
                System.load(libFile.getAbsolutePath( ));
                return;
            }
        }

        throw new RuntimeException("Unable to find the libnss library.");
    }

    private static String findExecutableLocation (String binaryName) {
        String location = System.getenv("PATH");
        if (location != null) {
            for (String dir : location.split(File.pathSeparator)) {
                File file = new File(dir, binaryName);
                if (file.exists( ) && file.canExecute( )) {
                    return dir;
                }
            }
        }
        return null;
    }
}
