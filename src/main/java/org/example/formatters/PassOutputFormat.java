package org.example.formatters;

import org.example.Main;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class PassOutputFormat extends OutputFormat {
    private final Logger LOG;

    private String prefix;
    private String cmd;
    private String usernamePrefix;
    private boolean alwaysWithLogin;
    private Map<String, Map<String, String>> toExport;

    public PassOutputFormat (List<Map<String, String>> pwstore, String cmdargs, Logger LOG) {
        super(pwstore, cmdargs);
        String[] cmdargsList = cmdargs.split(" ");
        this.prefix = cmdargsList[0];
        this.cmd = cmdargsList[1];
        this.usernamePrefix = cmdargsList[2];
        this.alwaysWithLogin = Boolean.parseBoolean(cmdargsList[3]);

        this.LOG = LOG;
    }

    public void output ( ) throws IOException {
        testPassCmd( );
        preprocessOutputs( );
        export( );
    }

    private void testPassCmd ( ) {
        System.out.println("Testing if password store is installed and configured");

        ProcessBuilder pb = new ProcessBuilder(this.cmd, "ls");
        Process p = null;

        try {
            p = pb.start( );
            int returnCode = p.waitFor( );
            if (returnCode != 0) {
                if (p.getErrorStream( ).toString( ).contains("Try \"pass init\"")) {
                    System.out.println("Password store was not initialized.");
                    System.out.println("Initialize the password store manually by using 'pass init'");
                    throw new RuntimeException("PASSSTORE_NOT_INIT");
                } else {
                    System.out.println("Unknown error happened when running 'pass'.");
                    System.out.println(STR."Stdout: \{p.getInputStream( ).toString( )}");
                    System.out.println(STR."Stderr: \{p.getErrorStream( ).toString( )}");
                    throw new RuntimeException("UNKNOWN_ERROR");
                }
            }
        } catch (IOException e) {
            if (e.getMessage( ).contains("No such file or directory")) {
                System.out.println("Password store is not installed and exporting was requested");
                throw new RuntimeException("PASSSTORE_MISSING", e);
            } else {
                System.out.println("Unknown error happened.");
                System.out.println(STR."Error was '\{e.getMessage( )}'");
                throw new RuntimeException("UNKNOWN_ERROR", e);
            }
        } catch (InterruptedException e) {
            System.out.println("Unknown error happened when running 'pass'.");
            System.out.println(STR."Stdout: \{p.getInputStream( ).toString( )}");
            System.out.println(STR."Stderr: \{p.getErrorStream( ).toString( )}");
            throw new RuntimeException("UNKNOWN_ERROR", e);
        }

        System.out.println(STR."""
pass returned:
Stdout: \{p.getInputStream( ).toString( )}
Stderr: \{p.getErrorStream( ).toString( )}""");
    }

    private void preprocessOutputs ( ) throws IOException {
        Map<String, Map<String, String>> toExport = new HashMap<>( );

        for (Map<String, String> record : this.pwstore) {
            String url = record.get("url");
            String user = record.get("user");
            String passw = record.get("password");

            URL address = new URL(url);

            if (!toExport.containsKey(address.getHost( ))) {
                toExport.put(address.getHost( ), new HashMap<>( ));
            }

            toExport.get(address.getHost( )).put(user, passw);
        }

        this.toExport = toExport;
    }

    public void export ( ) {
        LOG.info("Exporting credentials to password store");
        String prefixStr = (prefix != null) ? STR."\{prefix}/" : null;
        LOG.info(STR."Using pass prefix '\{prefixStr}'");

        for (Map.Entry<String, Map<String, String>> entry : toExport.entrySet( )) {
            String address = entry.getKey( );
            Map<String, String> accounts = entry.getValue( );
            for (Map.Entry<String, String> account : accounts.entrySet( )) {
                String user = account.getKey( );
                String password = account.getValue( );
                String passname;
                if (alwaysWithLogin || accounts.size( ) > 1) {
                    passname = STR."\{prefixStr}\{address}/\{user}";
                } else {
                    passname = prefixStr + address;
                }
                LOG.info(STR."Exporting credentials for '\{passname}'");

                String data = STR."""
\{password}
\{usernamePrefix}\{user}
""";
                LOG.warning(STR."Inserting pass '\{passname}' '\{data}'");

                List<String> cmd = new ArrayList<>( );
                cmd.add(this.cmd);
                cmd.add("insert");
                cmd.add("--force");
                cmd.add("--multiline");
                cmd.add(passname);

                LOG.warning(STR."Running command '\{cmd}' with stdin '\{data}'");
                ProcessBuilder pb = new ProcessBuilder(cmd);
                try {
                    Process p = pb.start( );
                    p.getOutputStream( ).write(data.getBytes( ));
                    p.getOutputStream( ).flush( );
                    p.getOutputStream( ).close( );
                    int exitCode = p.waitFor( );
                    if (exitCode != 0) {
                        LOG.warning(STR."ERROR: passwordstore exited with non-zero: \{exitCode}");
                        LOG.warning(STR."Stdout: \{p.getInputStream( )}\nStderr: \{p.getErrorStream( )}");
                        throw new RuntimeException("Passwordstore error");
                    }
                    LOG.info(STR."Successfully exported '\{passname}'");
                } catch (IOException | InterruptedException e) {
                    LOG.warning(STR."Error exporting credentials: \{e.getMessage( )}");
                    throw new RuntimeException("Error exporting credentials", e);
                }
            }
        }
    }

    @Override
    public OutputFormat init (List<Map<String, String>> pwstore, String cmdargs) {
        return new PassOutputFormat(pwstore, cmdargs, Main.LOG);
    }
}
