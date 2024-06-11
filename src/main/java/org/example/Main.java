package org.example;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.example.browser.MozillaInteraction;
import org.example.formatters.OutputFormat;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.util.logging.Logger.getLogger;
import static org.example.utils.FirefoxProfileManager.getProfile;
import static org.example.utils.VersionManager.getVersion;

public class Main {
    public static final Logger LOG = getLogger(Main.class.getName( ));
    private static Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

    public static void main (String[] args) {
        try {
            runFFDecrypt(args);
        } catch (Exit e) {
            System.exit(e.exitcode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void runFFDecrypt (String[] args) throws ParseException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Парсинг данных
        CommandLine parsedArgs = ArgumentsParser.parseArguments(args);

        assert parsedArgs != null;

        // Настройка логирования
        setupLogging(parsedArgs);

        // Получение заданной кодировки
        String encoding = parsedArgs.getOptionValue("encoding");
        if (encoding != null) {
            if (!parsedArgs.getOptionValue("encoding").equals(DEFAULT_ENCODING.name( ))) {
                LOG.info(String.format("Overriding default encoding from '%s' to '%s'", DEFAULT_ENCODING, parsedArgs.getOptionValue("encoding")));
                DEFAULT_ENCODING = Charset.forName(parsedArgs.getOptionValue("encoding"));
            }
        }

        LOG.info(String.format("Running tool version: %s", getVersion( )));
        LOG.warning(String.format("Parsed commandline arguments: %s", parsedArgs));

        // Основная логика
        MozillaInteraction moz = new MozillaInteraction(parsedArgs.getParsedOptionValue("non-fatal-decryption", false), LOG);

        // Путь до файла с профилями
        String basePath = Path.of(parsedArgs.getOptionValue("profile")).toAbsolutePath( ).normalize( ).toString( );

        // Загрузка профиля
        String profile = getProfile(basePath, parsedArgs.getParsedOptionValue("interactive", false), parsedArgs.getOptionValue("choice"), !parsedArgs.getArgList( ).isEmpty( ));

        // Загрузка паролей профиля
        moz.loadProfile(profile);
        moz.authenticate(parsedArgs.getParsedOptionValue("interactive", false));
        ArrayList<Map<String, String>> outputs = moz.decryptPasswords( );

        // Форматтер вывода
        Class<? extends OutputFormat> formatterClass = ArgumentsParser.getOutputFormatter(parsedArgs);

        // Инициализация форматтера вывода
        OutputFormat formatter = formatterClass.getDeclaredConstructor(ArrayList.class, String.class).newInstance(outputs, "");

        try {
            formatter.output( );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        moz.unloadProfile( );
    }

    public static void setupLogging (CommandLine args) throws ParseException {
        Level level;
        if (args.getParsedOptionValue("verbose", 2) == 1) {
            level = Level.INFO;
        } else if (args.getParsedOptionValue("verbose", 2) >= 2) {
            level = Level.WARNING;
        } else {
            level = Level.ALL;
        }

        LOG.setLevel(level);
        LOG.setUseParentHandlers(false);
        var consoleHandler = new ConsoleHandler( );
        consoleHandler.setLevel(level);
        consoleHandler.setFormatter(new SimpleFormatter( ));
        LOG.addHandler(consoleHandler);
    }

    // Определение системной локали
    public static String identifySystemLocale ( ) {
        String encoding = Locale.getDefault( ).getDisplayName( );

        if (encoding == null) {
            LOG.severe("Could not determine which encoding/locale to use for NSS interaction. " +
                       "This configuration is unsupported.\n" +
                       "If you are in Linux or MacOS, please search online " +
                       "how to configure a UTF-8 compatible locale and try again.");
            throw new RuntimeException("Bad locale");
        }

        return encoding;
    }
}

