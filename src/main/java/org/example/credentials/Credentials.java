package org.example.credentials;

import java.io.File;
import java.util.Iterator;

public abstract class Credentials {
    /**
     * Base credentials backend manager
     */
    protected final String db;

    public Credentials (String db) {
        this.db = db;

        System.out.println(STR."Database location: \{this.db}");
        if (!new File(db).isFile( )) {
            throw new RuntimeException(STR."ERROR - \{db} database not found");
        }

        System.out.println(STR."Using \{db} for credentials.");
    }

    public abstract Iterator<Tuple4<String, String, String, Integer>> iterator ( );

    /**
     * Override this method if the credentials subclass needs to do any
     * action after interaction
     */
    public abstract void done ( );

    public record Tuple4<T1, T2, T3, T4>(T1 _1, T2 _2, T3 _3, T4 _4) {

    }
}

