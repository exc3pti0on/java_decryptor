package org.example.formatters;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class CSVOutputFormat extends OutputFormat {
    private char delimiter;
    private char quotechar;
    private boolean header;

    public CSVOutputFormat (List<Map<String, String>> pwstore, String cmdargs, char delimiter, char quotechar, boolean header) {
        super(pwstore, cmdargs);
        this.delimiter = delimiter;
        this.quotechar = quotechar;
        this.header = header;
    }

    @Override
    public void output ( ) throws IOException {
        try (Writer writer = new StringWriter( ); CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withDelimiter(this.delimiter)
                .withQuote(this.quotechar)
                .withRecordSeparator("\n")
                .withHeader("url", "user", "password"))) {
            if (this.header) {
                csvPrinter.printRecord("url", "user", "password");
            }
            for (Map<String, String> output : this.pwstore) {
                csvPrinter.printRecord(output.get("url"), output.get("user"), output.get("password"));
            }
            System.out.println(writer);
        }
    }

    @Override
    public OutputFormat init (List<Map<String, String>> pwstore, String cmdargs) {
        return new TabularOutputFormat(pwstore, cmdargs);
    }
}
