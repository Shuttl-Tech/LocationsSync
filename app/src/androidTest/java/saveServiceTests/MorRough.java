package saveServiceTests;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import mockLocationUtils.MockLocationProvider;
import testUtils.BaseTestCase;
import testUtils.LogUITest;
import testUtils.UiUtils;


@RunWith(AndroidJUnit4.class)
public class MorRough extends BaseTestCase {


    @Test
    public void tinyMockGPSTest() {

        safeSleep(5);
        MockLocationProvider.setMockLocation(10, 20);
        //mLocationManager.getLastKnownLocation(locationProviderName);


        safeSleep(5);
        MockLocationProvider.setMockLocation(30, 40);

        safeSleep(5);
        MockLocationProvider.setMockLocation(50, 60, 5000);

    }

    @Ignore
    @Test
    public void sequentialChangeTest() {

        LogUITest.info("-----------------------------");
        LogUITest.info("BEGIN sequentialChangeTest()");
        for(int count =1; count<=10; count++) {
            LogUITest.debug("The run count is: "+count);
            double latitude = count*11;
            double longitude = count*11;
            MockLocationProvider.setMockLocation(longitude, latitude);
            safeSleep(5);
        }
        LogUITest.info("END sequentialChangeTest()");
        LogUITest.info("-----------------------------");
    }

    @Ignore
    @Test
    public void bigGPSTest() {

        LogUITest.debug("-------- checkpoint 001 ");

        for (int i=0; i<=10; i++) {

            LogUITest.debug("The run count is: "+i);
            double latitude = UiUtils.randomGenerator(1,90);
            double longitude = UiUtils.randomGenerator(1,90);
            double altitude = UiUtils.randomGenerator(0,5000);

            MockLocationProvider.setMockLocation(longitude, latitude);

            safeSleep(5);

            MockLocationProvider.setMockLocation(longitude, latitude, altitude);
        }


    }


    public static void safeSleep(float seconds) {

        try {
            Thread.sleep((int) seconds * 1000);
        } catch (InterruptedException e) {
            LogUITest.error("Failed to sleep for " + seconds + " seconds !");
            e.printStackTrace();
        }
    }




}
