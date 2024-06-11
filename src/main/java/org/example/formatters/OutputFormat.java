package org.example.formatters;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class OutputFormat {
    protected List<Map<String, String>> pwstore;
    protected String cmdargs;

    public OutputFormat (List<Map<String, String>> pwstore, String cmdargs) {
        this.pwstore = pwstore;
        this.cmdargs = cmdargs;
    }

    public abstract void output ( ) throws IOException;

    public abstract OutputFormat init (List<Map<String, String>> pwstore, String cmdargs);
}