package research.ufu.datagather.model;


public class Constants {
    public static String LONG_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static int PORT = 443;
    public static String SERVER_URL = "https://slayer.dlinkddns.com:" + PORT;
    public static String URL_AUTH = SERVER_URL + "/auth";
    public static String URL_LAST_LOCATION = SERVER_URL + "/lastLocation";
    public static String URL_LAST_WIFI= SERVER_URL + "/lastWifi";
    public static String URL_ADD_LOCATION = SERVER_URL + "/addLocation";
    public static String URL_ADD_WIFI = SERVER_URL + "/addWifi";
    public static String CERTIFICATE_KEY = "CA:8A:92:6F:4F:E5:D5:7E:C6:E7:C5:7C:B3:E0:87:80:99:64:4B:33";
    //In seconds
    public static int TIME_STEP = 15;

    //Threshold for submission
    public static int THRESHOLD = 500;
}
