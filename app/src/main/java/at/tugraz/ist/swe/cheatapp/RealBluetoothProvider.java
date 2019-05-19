package at.tugraz.ist.swe.cheatapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RealBluetoothProvider extends BluetoothProvider {
    private BluetoothAdapter adapter;
    private BluetoothThread bluetoothThread;

    private RealDevice device;

    public RealBluetoothProvider() throws BluetoothException {
        initialize();
    }

    public void initialize() throws BluetoothException {
        // TODO: Should the bluetooth adapter be a member of BluetoothThread?
        adapter = BluetoothAdapter.getDefaultAdapter();
        device = null;


        if (adapter == null) {
            throw new BluetoothException("No bluetooth adapter available");
        }

        Thread.UncaughtExceptionHandler exceptionHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable ex) {
                ex.printStackTrace();
                onError(ex.getMessage());
            }
        };

        Log.d("RealBluetoothProvider", "Starting bluetooth thread");

        bluetoothThread = new BluetoothThread(this);
        bluetoothThread.setUncaughtExceptionHandler(exceptionHandler);
        bluetoothThread.start();
    }

    @Override
    public List<Device> getPairedDevices() {
        Set<BluetoothDevice> btDevices = adapter.getBondedDevices();

        List<Device> devices = new ArrayList<>();

        for (BluetoothDevice btDevice : btDevices) {
            devices.add(new RealDevice(btDevice));
        }

        return devices;
    }

    @Override
    public synchronized void connectToDevice(Device device) {
        // TODO: Should this be a member of bluetooth thread?
        Log.d("RealBluetoothProvider", "Requesting connection as client");
        this.device = (RealDevice) device;

    }

    @Override
    public void sendMessage(final Message message) {
        final BluetoothMessage btMessage = new BluetoothMessage(message);
        bluetoothThread.sendBluetoothMessage(btMessage);
    }

    @Override
    public void disconnect() {
        final BluetoothMessage btMessage = new BluetoothMessage(new DisconnectMessage());
        bluetoothThread.sendBluetoothMessage(btMessage);
        bluetoothThread.setRunning(false);
    }

    @Override
    protected void onMessageReceived(final Message message) {
        super.onMessageReceived(message);
    }

    @Override
    protected void onDisconnected() {
        try {
            initialize();
        } catch (BluetoothException ex) {
            ex.printStackTrace();
            onError(ex.getMessage());
        }

        super.onDisconnected();
    }

    public void handleBluetoothMessage(final BluetoothMessage bluetoothMessage) {
        switch (bluetoothMessage.getMessageType()) {
            case CHAT:
                onMessageReceived(bluetoothMessage.getMessage());
                break;
            case CONNECT:
                onConnected();
                break;
            case DISCONNECT:
                bluetoothThread.setRunning(false);
                break;
        }
    }

    public synchronized RealDevice getDevice() {
        return device;
    }

    public BluetoothAdapter getAdapter() {
        return adapter;
    }
}
