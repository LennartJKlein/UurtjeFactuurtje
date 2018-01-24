package nl.lennartklein.uurtjefactuurtje;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Dan Alboteanu on 31-5-2016.
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