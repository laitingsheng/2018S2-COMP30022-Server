package com.example.COMP30022ServerEngine.FirebaseDB;

import com.example.COMP30022ServerEngine.Comp30022ServerEngineApplication;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.COMP30022ServerEngine.Constant.FIREBASEADMINKEYPATH;

public class FirebaseDb {

    private static final Logger LOGGER = Logger.getLogger(FirebaseDb.class.getName());

    private FileInputStream serviceAccount;

    public FirebaseDb() {
        try {
            FileInputStream serviceAccount = new FileInputStream(FIREBASEADMINKEYPATH);

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://comp30022-it-project.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        }
    }

    public void updateUser(String userId){
        /*
        1. get old user location from user-location database
        2. delete user from location-user database
        3. update new user location to userlocation database
        4. update new user location to location-user database
         */
        ;
    }
}
