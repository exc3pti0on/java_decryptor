package org.example.formatters;

import java.util.List;
import java.util.Map;

public class HumanOutputFormat extends OutputFormat {
    public HumanOutputFormat (List<Map<String, String>> pwstore, String cmdargs) {
        super(pwstore, cmdargs);
    }

    @Override
    public void output ( ) {
        for (Map<String, String> output : this.pwstore) {
            String record = String.format(
                    "\nWebsite:   %s\nUsername: '%s'\nPassword: '%s'\n",
                    output.get("url"), output.get("user"), output.get("password")
            );
            System.out.println(record);
        }
    }

    @Override
    public OutputFormat init (List<Map<String, String>> pwstore, String cmdargs) {
        return new HumanOutputFormat(pwstore, cmdargs);
    }
}
