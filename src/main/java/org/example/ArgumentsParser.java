package org.example;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.SystemUtils;
import org.example.formatters.*;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class ArgumentsParser {
    // Функция для разбора аргументов командной строки
    public static CommandLine parseArguments (String[] args) {

        Options options = new Options( );

        // Ищем путь до ФаерФокса
        String profilePath;
        if (SystemUtils.IS_OS_WINDOWS) {
            profilePath = Paths.get(System.getenv("APPDATA"), "Mozilla", "Firefox").toString( );
        } else if (SystemUtils.IS_OS_MAC) {
            profilePath = "~/Library/Application Support/Firefox";
        } else {
            profilePath = "~/.mozilla/firefox";
        }

        // Инициализация ключей аргументов

        Option profileOption = Option.builder("p")
                .longOpt("profile")
                .hasArg( )
                .argName("profile")
                .desc(String.format("Path to profile folder (default: %s)", profilePath))
                .build( );
        options.addOption(profileOption);

        Map<String, String> formatChoices = new HashMap<>( );
        formatChoices.put("human", "HumanOutputFormat");
        formatChoices.put("json", "JSONOutputFormat");
        formatChoices.put("csv", "CSVOutputFormat");
        formatChoices.put("tabular", "TabularOutputFormat");
        formatChoices.put("pass", "PassOutputFormat");

        Option formatOption = Option.builder("f")
                .longOpt("format")
                .hasArg( )
                .argName("format")
                .desc("Format for the output.")
                .build( );
        options.addOption(formatOption);

        Option csvDelimiterOption = Option.builder("d")
                .longOpt("csv-delimiter")
                .hasArg( )
                .argName("delimiter")
                .desc("The delimiter for csv output")
                .build( );
        options.addOption(csvDelimiterOption);

        Option csvQuotecharOption = Option.builder("q")
                .longOpt("csv-quotechar")
                .hasArg( )
                .argName("quotechar")
                .desc("The quote char for csv output")
                .build( );
        options.addOption(csvQuotecharOption);

        Option noCSVHeaderOption = Option.builder( )
                .longOpt("no-csv-header")
                .desc("Do not include a header in CSV output.")
                .build( );
        options.addOption(noCSVHeaderOption);

        Option passUsernamePrefixOption = Option.builder( )
                .longOpt("pass-username-prefix")
                .hasArg( )
                .argName("prefix")
                .desc("Export username as is (default), or with the provided format prefix. For instance 'login: ' for browserpass.")
                .build( );
        options.addOption(passUsernamePrefixOption);

        Option passPrefixOption = Option.builder("p")
                .longOpt("pass-prefix")
                .hasArg( )
                .argName("prefix")
                .desc("Folder prefix for export to pass from passwordstore.org (default: web)")
                .build( );
        options.addOption(passPrefixOption);

        Option passCommandOption = Option.builder("m")
                .longOpt("pass-cmd")
                .hasArg( )
                .argName("command")
                .desc("Command/path to use when exporting to pass (default: pass)")
                .build( );
        options.addOption(passCommandOption);

        Option passAlwaysWithLoginOption = Option.builder( )
                .longOpt("pass-always-with-login")
                .desc("Always save as /<login> (default: only when multiple accounts per domain)")
                .build( );
        options.addOption(passAlwaysWithLoginOption);

        Option noInteractiveOption = Option.builder("n")
                .longOpt("no-interactive")
                .desc("Disable interactivity.")
                .build( );
        options.addOption(noInteractiveOption);

        Option nonFatalDecryptionOption = Option.builder( )
                .longOpt("non-fatal-decryption")
                .desc("If set, corrupted entries will be skipped instead of aborting the process.")
                .build( );
        options.addOption(nonFatalDecryptionOption);

        Option choiceOption = Option.builder("c")
                .longOpt("choice")
                .hasArg( )
                .argName("profile")
                .desc("The profile to use (starts with 1). If only one profile, defaults to that.")
                .build( );
        options.addOption(choiceOption);

        Option listOption = Option.builder("l")
                .longOpt("list")
                .desc("List profiles and exit.")
                .build( );
        options.addOption(listOption);

        Option encodingOption = Option.builder("e")
                .longOpt("encoding")
                .hasArg( )
                .argName("encoding")
                .desc("Override default encoding (UTF-8).")
                .build( );
        options.addOption(encodingOption);

        Option verboseOption = Option.builder("v")
                .longOpt("verbose")
                .desc("Verbosity level. Warning on -vv (highest level) user input will be printed on screen")
                .build( );
        options.addOption(verboseOption);

        Option versionOption = Option.builder( )
                .longOpt("version")
                .desc("Display version of tool and exit")
                .build( );
        options.addOption(versionOption);

        // Инициализация парсера
        CommandLineParser parser = new DefaultParser( );
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter( );
            formatter.printHelp("tool", options);
            return null;
        }

        return cmd;
    }

    // Форматтеры вывода
    private static final Map<String, Class<? extends OutputFormat>> formatters =
            new HashMap<>( ) {

            };

    static {
        formatters.put("human", HumanOutputFormat.class);
        formatters.put("json", JSONOutputFormat.class);
        formatters.put("csv", CSVOutputFormat.class);
        formatters.put("tabular", TabularOutputFormat.class);
        formatters.put("pass", PassOutputFormat.class);
    }

    public static Class<? extends OutputFormat> getOutputFormatter (CommandLine args) {
        return formatters.get(args.getOptionValue("format"));
    }
}

