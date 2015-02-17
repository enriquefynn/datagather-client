package research.ufu.datagather.model;


public class Constants {
    public static String SHARED_DATA = "SHARED_DATA";
    public static String LONG_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static int PORT = 443;
    public static String SERVER_URL = "https://slayer.dyndns-ip.com:" + PORT;
    public static String URL_AUTH = SERVER_URL + "/auth";
    public static String URL_LAST_LOCATION = SERVER_URL + "/lastLocation";
    public static String URL_LAST_WIFI= SERVER_URL + "/lastWifi";
    public static String URL_ADD_LOCATION = SERVER_URL + "/addLocation";
    public static String URL_ADD_WIFI = SERVER_URL + "/addWifi";
    public static String CERTIFICATE_KEY = "CA:8A:92:6F:4F:E5:D5:7E:C6:E7:C5:7C:B3:E0:87:80:99:64:4B:33";
    public static String WIFI_CHANGED_ACTION = "WIFI_CHANGED";

    //In seconds
    public static int TIME_STEP = 15;
    public static int TIMEOUT = 10;

    //Threshold for submission
    public static int THRESHOLD = 500;

    public static int MSG_TICK = 0;
    public static int MSG_LAST_SYNC = 1;
    public static int MSG_SERVER_ERROR = 2;

    public static boolean SERVICE_RUNNING = false;
}
