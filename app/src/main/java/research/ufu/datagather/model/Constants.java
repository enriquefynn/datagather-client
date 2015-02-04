package research.ufu.datagather.model;


public class Constants {
    public static String LONG_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static String SERVER_URL = "http://www.slayer.dlinkddns.com:8000";
    public static String URL_SIGN_UP = SERVER_URL + "/newUser";
    public static String URL_SIGN_IN = SERVER_URL + "/auth";
    public static String URL_LAST_LOCATION = SERVER_URL + "/lastLocation";
    public static String URL_LAST_WIFI= SERVER_URL + "/lastWifi";
    public static String URL_ADD_LOCATION = SERVER_URL + "/addLocation";
    public static String URL_ADD_WIFI = SERVER_URL + "/addWifi";

    //In seconds
    public static int TIME_STEP = 15;
}
