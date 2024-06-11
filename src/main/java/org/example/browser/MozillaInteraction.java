package org.example.browser;

import org.example.Exit;
import org.example.credentials.Credentials;
import org.example.credentials.JsonCredentials;
import org.example.credentials.SqliteCredentials;
import org.example.exceptions.NotFoundError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class MozillaInteraction {
    private final NSSProxy proxy;
    private String profile;

    public MozillaInteraction (boolean nonFatalDecryption, Logger LOG) {
        this.proxy = new NSSProxy(nonFatalDecryption, LOG);
    }

    public void loadProfile (String profile) {
        this.profile = profile;
        this.proxy.init(this.profile);
    }

    public void authenticate (boolean interactive) {
        this.proxy.authenticate(this.profile, interactive);
    }

    public void unloadProfile ( ) {
        this.proxy.shutdown( );
    }

    public ArrayList<Map<String, String>> decryptPasswords ( ) {
        Credentials credentials = this.obtainCredentials( );

        ArrayList<Map<String, String>> outputs = new ArrayList<>( );

        for (Iterator<Credentials.Tuple4<String, String, String, Integer>> it = credentials.iterator( ); it.hasNext( ); ) {
            Credentials.Tuple4<String, String, String, Integer> credential = it.next( );
            String url = credential._1( );
            String user = credential._2( );
            String password = credential._3( );
            int enctype = credential._4( );

            if (enctype != 0) {
                try {
                    user = this.proxy.decrypt(user.getBytes( ));
                    password = this.proxy.decrypt(password.getBytes( ));
                } catch (Exception e) {
                    user = "*** decryption failed ***";
                    password = "*** decryption failed ***";
                }
            }

            Map<String, String> output = new HashMap<>( );
            output.put("url", url);
            output.put("user", user);
            output.put("password", password);
            outputs.add(output);
        }

        if (outputs.isEmpty( )) {
            System.out.println("No passwords found in selected profile");
        }

        credentials.done( );
        return outputs;
    }

    private Credentials obtainCredentials ( ) {
        try {
            return new JsonCredentials(this.profile);
        } catch (NotFoundError e) {
            try {
                return new SqliteCredentials(this.profile);
            } catch (NotFoundError e2) {
                System.out.println("Couldn't find credentials file (logins.json or signons.sqlite).");
                throw new Exit(Exit.MISSING_SECRETS);
            }
        }
    }
}

