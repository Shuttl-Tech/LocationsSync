package testUtils;

import android.Manifest;
import android.content.Context;
import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;

import com.shuttl.location_pings.callbacks.LocationPingServiceCallback;
import com.shuttl.location_pings.config.components.LocationConfigs;
import com.shuttl.packagetest.MainActivity;
import com.shuttl.packagetest.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockLocationUtils.MockLocationProvider;
import testUtils.mockWebServer.CustomDispatcher;
import testUtils.mockWebServer.DispatcherUtils;
import testUtils.mockWebServer.MockWebUtils;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class BaseTestCase {

    public static Map<String, String> edgeCaseResponses = new HashMap<>();
    public static Context appContext = InstrumentationRegistry.getInstrumentation().getContext();
    public static Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    public static UiDevice mDevice = UiDevice.getInstance(getInstrumentation());
    public LocationConfigs locationConfigs;

    public LocationPingServiceCallback locationPingServiceCallback = new LocationPingServiceCallback() {
        @Override
        public void afterSyncLocations(@Nullable List list) {

        }

        @NotNull
        @Override
        public List beforeSyncLocations(@Nullable List list) {
            return null;
        }

        @Override
        public void errorWhileSyncLocations(@Nullable Exception error) {

        }

        @Override
        public void serviceStarted() {

        }

        @Override
        public void serviceStopped() {

        }

        @Override
        public void serviceStoppedManually() {

        }
    };


    @Rule
    public GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    );


    @Rule
    public TestName testName = new TestName();

    @Rule
    public ActivityTestRule activityTestRule = new ActivityTestRule(MainActivity.class);

    @Before
    public void mainSetUp() throws IOException {

        LogUITest.debug("\n***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****");
        LogUITest.info("\n***** \t\tBEGIN Test: " + testName.getMethodName());
        LogUITest.debug("***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****\n");

        MockWebUtils.startServer();
        //DispatcherUtils.setDispacher(new CustomDispatcher());

        LogUITest.debug("Current URL : " + TestConstants.GPS_PIPELINE_URL);

        // Set config
        locationConfigs =
                new LocationConfigs(100, 100
                        , 1000, 3, 100, 10, 1800000
                        , "", TestConstants.GPS_PIPELINE_URL, R.drawable.ic_loc);

    }


    protected static void setMockLocationInDeveloperOption() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mDevice.executeShellCommand("appops set " + "com.shuttl.packagetest" + " android:mock_location allow");
                //mDevice.executeShellCommand("appops set " + getInstrumentation().getTargetContext().getPackageName() + " android:mock_location allow");
            } catch (IOException e) {
                LogUITest.error("Failed to set Mock Location App in developer options");
                LogUITest.debug(e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                LogUITest.error("Failed to set Mock Location App in developer options");
                LogUITest.debug(e.getMessage());
                e.printStackTrace();

            }
        }
    }

    @After
    public void tearDown() throws IOException {
        MockLocationProvider.unregister();

        UiUtils.stopSaveLocationServiceIfRunning(activityTestRule.getActivity().getApplication());
        UiUtils.stopPingLocationServiceIfRunning(activityTestRule.getActivity().getApplication());

        MockWebUtils.stopServer();

        LogUITest.debug("\n***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****");
        LogUITest.info("\n***** \t\tEND Test: " + testName.getMethodName());
        LogUITest.debug("***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****\n");

    }
}