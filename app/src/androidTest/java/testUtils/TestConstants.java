package testUtils;

import com.shuttl.packagetest.R;

import testUtils.mockWebServer.MockWebUtils;

public class TestConstants {


    /*
    When we call startForgroundSerivce() method, Service do not get started immediately.
    When we try to stop the service immediately starting it, It may fire the exception.
    Because Service is not started due to processing speed and low memory in RAM.
    This was causing Instrumentation Crash in tests.
    Usually waiting for 500 ms helps ... But since we are running on simulators .
    Therefore, adding more wait for safer side.
     */
    public static int WAIT_FOR_SERVICE_TO_GET_STARTED = 2;

    public static class DelayInSeconds {

        public static long THREE_SEC = 3000;
        public static long FIVE_SEC = 5000;
        public static long TEN_SEC = 10000;
        public static long TWENTY_SEC = 20000;

    }

    public static double BASE_LAT = 13.001;
    public static double BASE_LNG = 77.010;
    public static double DIFFERENCE_IN_LATITUDE_BETWEEN_TWO_LOCATIONS = 0.001;

    public static boolean WAKE_LOCK_ENABLED = true;
    public static boolean WAKE_LOCK_DISABLED = false;
    public static int ALARM_TRIGGER_INTERVAL = 15000;
    public static int INACTIVITY_INTERVAL = 5 * 60 * 1000;

    public static boolean REUSE_LAST_LOCATION = false;
    public static boolean ENABLE_ALARM_MANAGER = true;

    public static String GPS_PIPELINE_URL_END_POINT = "sendGps/";
    public static String GPS_PIPELINE_URL = MockWebUtils.getMockWebServerUrl() + GPS_PIPELINE_URL_END_POINT;
    public static int NUMBER_OF_RETRIES_FOR_STOPPING_SERVICES = 3;
    public static int NUMBER_OF_RETRIES_FOR_STARTING_SERVICES = 3;
    public static final int NOTIFICATION_ICON_ID = R.drawable.ic_loc;


    public enum RESPONSE_TYPE {
        SUCCESS,
        FAILURE,
        DELAYED
    }


    // ---------------------------------    LOCATION CONFIG FOR PING SERVICE TESTS  -------------------------------------

    public static final int MIN_TIME_INTERVAL_FOR_LOCATION_FETCHING_PS = 100;  // in millis
    public static final int MIN_DISTANCE_INTERVAL_BETWEEN_TWO_LOCATIONS_PS = 20;
    public static final int MIN_PING_SERVICE_SYNC_INTERVAL_PS = 7000;  // in millis
    public static final int ACCURACY_PS = 3;
    public static final int BUFFER_SIZE_PS = 3;
    public static final int BATCH_SIZE_FOR_PING_SERVICE_PS = 5;


    // ---------------------------------    LOCATION CONFIG FOR SAVE SERVICE TESTS  -------------------------------------

    public static final int MIN_TIME_INTERVAL_FOR_LOCATION_FETCHING_SS = 2000;  // in millis
    public static final int MIN_DISTANCE_INTERVAL_BETWEEN_TWO_LOCATIONS_SS = 20;
    public static final int MIN_PING_SERVICE_SYNC_INTERVAL_SS = 90000;  // in millis
    public static final int ACCURACY_SS = 3;
    public static final int BUFFER_SIZE_SS = 3;
    public static final int BATCH_SIZE_FOR_PING_SERVICE_SS = 1;


    // ---------------------------------    LOCATION CONFIG FOR END TO END FLOW  -------------------------------------

    public static final int MIN_TIME_INTERVAL_FOR_LOCATION_FETCHING = 100;  // in millis
    public static final int MIN_DISTANCE_INTERVAL_BETWEEN_TWO_LOCATIONS = 20;
    public static final int MIN_PING_SERVICE_SYNC_INTERVAL = 7000;  // in millis
    public static final int ACCURACY = 3;
    public static final int BUFFER_SIZE = 5;
    public static final int BATCH_SIZE_FOR_PING_SERVICE = 3;


    // ---------------------------------    GLOBAL LOCATION CONFIG   -------------------------------------

    public static final int MIN_TIME_INTERVAL_FOR_LOCATION_FETCHING_GLOBAL = 1000;  // in millis
    public static final int MIN_DISTANCE_INTERVAL_BETWEEN_TWO_LOCATIONS_GLOBAL = 20;
    public static final int MIN_PING_SERVICE_SYNC_INTERVAL_GLOBAL = 10000;  // in millis
    public static final int ACCURACY_GLOBAL = 3;
    public static final int BUFFER_SIZE_GLOBAL = 50;
    public static final int BATCH_SIZE_FOR_PING_SERVICE_GLOBAL = 10;
    public static final String XAPI_KEY_GLOBAL = "";
    public static final int SERVICE_TIMEOUT_GLOBAL = 0;  // in millis


}
