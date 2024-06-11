package org.example.formatters;

import java.util.List;
import java.util.Map;

public class TabularOutputFormat extends CSVOutputFormat {
    public TabularOutputFormat (List<Map<String, String>> pwstore, String cmdargs) {
        super(pwstore, cmdargs, '\t', '\'', true);
    }
}
