////////////////////////////////////////////////////////////////////////////////
// Title        PersistentDatabase
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

package nl.lennartklein.uurtjefactuurtje;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A singleton for a FireBase database
 */

public class PersistentDatabase {

    private static FirebaseDatabase db;
    private static DatabaseReference dbReference;

    public static DatabaseReference getReference() {
        if (db == null || dbReference == null) {
            db = FirebaseDatabase.getInstance();
            db.setPersistenceEnabled(true);
            dbReference = db.getReference();
        }
        return dbReference;
    }

}