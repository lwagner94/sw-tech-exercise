package at.tugraz.ist.swe.cheatapp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.Menu;
import android.view.MenuInflater;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static org.hamcrest.Matchers.not;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ConnectFragmentEspressoTest {
    @Rule
    public ActivityTestRule<MainActivity> mainActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        mainActivityTestRule.getActivity().showConnectFragment();
    }

    @Test
    public void testButtonsVisible() {
        onView(withId(R.id.btn_connect_disconnect)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_connect_disconnect)).check(matches(withText(R.string.connect)));
    }

    @Test
    public void testListVisible() {
        onView(withId(R.id.lv_con_devices)).check(matches(isDisplayed()));
    }

    @Test
    public void testMenuVisible() {
        onData(is(instanceOf(MenuInflater.class))).check(matches(isDisplayed()));
    }

    @Test
    public void testMenuDropdownItems() {
        Menu appMenu;
        MenuInflater inflater = mainActivityTestRule.getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu, appMenu);
        assertTrue(appMenu.hasVisibleItems());
        onData(is(instanceOf(MenuInflater.class))).perform(click());
        onView(withId(R.id.menu_set_nickname)).check(matches(isDisplayed()));
    }

    @Test
    public void testSelectListElementAndConnect() throws InterruptedException {
        DummyBluetoothProvider provider = new DummyBluetoothProvider();
        provider.enableDummyDevices(1);

        mainActivityTestRule.getActivity().setBluetoothProvider(provider);

        onData(allOf(is(instanceOf(String.class)), is("1")))
                .perform(click());

        onView(withId(R.id.btn_connect_disconnect)).perform(click());
        provider.getThread().join();

        assertTrue(provider.isConnected());
        assertEquals(provider.getConnectedDevice().getDeviceId(), 1);
        assertEquals(provider.getConnectedDevice().getDeviceName(), "1");
    }

    @Test
    public void testConnectWithoutSelection() throws InterruptedException {
        DummyBluetoothProvider provider = new DummyBluetoothProvider();
        provider.enableDummyDevices(1);

        mainActivityTestRule.getActivity().setBluetoothProvider(provider);

        onView(withId(R.id.btn_connect_disconnect)).perform(click());

        assertFalse(provider.isConnected());
        assertNull(provider.getConnectedDevice());
    }

    @Test
    public void testConnectWithSelection() throws InterruptedException {
        DummyBluetoothProvider provider = new DummyBluetoothProvider();
        provider.enableDummyDevices(1);

        mainActivityTestRule.getActivity().setBluetoothProvider(provider);

        onData(allOf(is(instanceOf(String.class)), is("1"))).perform(click());

        onView(withId(R.id.btn_connect_disconnect)).perform(click());
        provider.getThread().join();

        assertTrue(provider.isConnected());
        assertEquals(provider.getConnectedDevice().getDeviceId(), 1);
        assertEquals(provider.getConnectedDevice().getDeviceName(), "1");
    }

    @Test
    public void testChangeViewOnConnect() throws InterruptedException {
        DummyBluetoothProvider provider = new DummyBluetoothProvider();
        provider.enableDummyDevices(1);

        mainActivityTestRule.getActivity().setBluetoothProvider(provider);

        onData(allOf(is(instanceOf(String.class)), is("1")))
                .perform(click());
        onView(withId(R.id.btn_connect_disconnect)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_connect_disconnect)).perform(click());
        provider.getThread().join();

        onView(withId(R.id.btn_chat_send)).check(matches(isDisplayed()));
    }

    @Test
    public void testChangeViewOnDisconnect() throws InterruptedException {
        DummyBluetoothProvider provider = new DummyBluetoothProvider();
        provider.enableDummyDevices(1);

        mainActivityTestRule.getActivity().setBluetoothProvider(provider);

        onData(allOf(is(instanceOf(String.class)), is("1")))
                .perform(click());
        onView(withId(R.id.btn_connect_disconnect)).perform(click());
        provider.getThread().join();

        onView(withId(R.id.btn_connect_disconnect)).perform(click());
        provider.getThread().join();

        onView(withId(R.id.btn_connect_disconnect)).check(matches(isDisplayed()));
    }

    @Test
    public void testDisconnect() throws InterruptedException {
        DummyBluetoothProvider provider = new DummyBluetoothProvider();
        provider.enableDummyDevices(1);

        mainActivityTestRule.getActivity().setBluetoothProvider(provider);

        onData(allOf(is(instanceOf(String.class)), is("1")))
                .perform(click());

        onView(withId(R.id.btn_connect_disconnect)).perform(click());
        provider.getThread().join();

        onView(withId(R.id.btn_connect_disconnect)).perform(click());
        provider.getThread().join();

        assertFalse(provider.isConnected());
        assertNull(provider.getConnectedDevice());
    }



    @Test
    public void testHandshakeMessageAfterConnect() throws InterruptedException {
        DummyBluetoothProvider provider = new DummyBluetoothProvider();
        provider.enableDummyDevices(1);

        mainActivityTestRule.getActivity().setBluetoothProvider(provider);

        onData(allOf(is(instanceOf(String.class)), is("1")))
                .perform(click());
        onView(withId(R.id.btn_connect_disconnect)).perform(click());
        provider.getThread().join();

        onView(withText(R.string.connected))
                .inRoot(withDecorView(not(is(mainActivityTestRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));
    }
}
