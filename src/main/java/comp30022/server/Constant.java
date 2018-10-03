package comp30022.server;

import java.nio.file.Paths;

public class Constant {
    public static final String GOOGLEMAPAPIKEY = "AIzaSyA6oLxJ0o_uOug2DOHXxUGNdgooxK2jSZc";
    public static final String FIREBASEADMINKEYPATH = Paths
        .get(".", "src", "main", "resources", "comp30022-it-project-firebase-adminsdk-yb5ns-953cb0673d.json")
        .toAbsolutePath()
        .normalize()
        .toString();
}
