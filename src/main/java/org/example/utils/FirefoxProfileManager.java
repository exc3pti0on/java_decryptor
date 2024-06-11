package org.example.utils;

import org.example.Exit;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FirefoxProfileManager {
    public static Map<String, String> getProfileSections (Properties profiles) {
        Map<String, String> sections = new HashMap<>( );
        int i = 1;
        for (String section : profiles.stringPropertyNames( )) {
            if (section.startsWith("Profile")) {
                sections.put(String.valueOf(i), profiles.getProperty(section, "Path"));
                i++;
            }
        }
        return sections;
    }

    public static void printSections (Map<String, String> sections, PrintStream textIOWrapper) {
        for (Map.Entry<String, String> entry : sections.entrySet( )) {
            textIOWrapper.println(STR."\{entry.getKey( )} -> \{entry.getValue( )}");
        }
        textIOWrapper.flush( );
    }

    public static String askSection (Properties sections) {
        String choice = "ASK";
        Scanner scanner = new Scanner(System.in);
        while (!sections.containsKey(choice)) {
            System.err.println("Select the Mozilla profile you wish to decrypt");
            printSections(getProfileSections(sections), System.err);
            try {
                choice = scanner.nextLine( );
            } catch (Exception e) {
                System.err.println("Could not read Choice, got EOF");
                throw new RuntimeException("Could not read Choice, got EOF");
            }
        }

        String finalChoice = sections.getProperty(choice);
        if (finalChoice == null) {
            System.err.println(STR."Profile No. \{choice} does not exist!");
            throw new RuntimeException(STR."Profile No. \{choice} does not exist!");
        }

        System.out.println(STR."Profile selection matched \{finalChoice}");
        return finalChoice;
    }

    public static String askPassword (String profile, boolean interactive) {
        String passwd;
        String passmsg = STR."""

Primary Password for profile \{profile}:\s""";

        if (System.console( ) != null && interactive) {
            passwd = new String(System.console( ).readPassword(passmsg));
        } else {
            System.err.println("Reading Primary password from standard input:");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                passwd = reader.readLine( ).trim( );
            } catch (IOException e) {
                throw new RuntimeException("Could not read password from standard input", e);
            }
        }

        return passwd;
    }

    public static Properties readProfiles (String basepath) {
        String profileini = STR."\{basepath}/profiles.ini";

        System.out.println(STR."Reading profiles from \{profileini}");

        if (!new java.io.File(profileini).isFile( )) {
            System.err.println(STR."profile.ini not found in \{basepath}");
            throw new RuntimeException("profile.ini not found");
        }

        // Read profiles from Firefox profile folder
        Properties profiles = new Properties( );
        try {
            profiles.load(new java.io.FileInputStream(profileini));
        } catch (IOException e) {
            throw new RuntimeException("Error reading profiles.ini", e);
        }

        System.out.println(STR."Read profiles \{profiles.stringPropertyNames( )}");
        return profiles;
    }

    public static String getProfile (
            String basepath,
            boolean interactive,
            String choice,
            boolean listProfiles) {
        try {
            Properties profiles = readProfiles(basepath);
            if (listProfiles) {
                System.out.println("Listing available profiles...");
                printSections(profiles.entrySet( ).stream( ).collect(Collectors.toMap(e -> (String) e.getKey( ), e -> (String) e.getValue( ))), System.out);
                throw new Exit(Exit.CLEAN);
            }

            Set<String> sections = profiles.keySet( ).stream( ).map(Object::toString).collect(Collectors.toSet( ));

            if (sections.size( ) == 1) {
                return (String) profiles.get("1");
            } else if (choice != null) {
                if (!profiles.containsKey(choice)) {
                    System.err.println(STR."Profile No. \{choice} does not exist!");
                    throw new Exit(Exit.NO_SUCH_PROFILE);
                }
                return (String) profiles.get(choice);
            } else if (!interactive) {
                System.err.println("Don't know which profile to decrypt. We are in non-interactive mode and -c/--choice wasn't specified.");
                throw new Exit(Exit.MISSING_CHOICE);
            } else {
                // Ask user which profile to open
                return askSection(profiles);
            }
        } catch (Exit e) {
            if (e.exitcode == Exit.MISSING_PROFILEINI) {
                System.out.println(STR."Continuing and assuming '\{basepath}' is a profile location");
                File profileDir = new File(basepath);
                if (!profileDir.isDirectory( )) {
                    System.err.println(STR."Profile location '\{basepath}' is not a directory");
                    throw e;
                }
                return basepath;
            } else {
                throw e;
            }
        }
    }
}
