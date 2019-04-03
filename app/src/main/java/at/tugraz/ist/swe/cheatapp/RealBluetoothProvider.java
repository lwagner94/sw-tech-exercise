package at.tugraz.ist.swe.cheatapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class RealBluetoothProvider extends BluetoothProvider {


    private final Queue<String> sentMessageQueue;
    private BluetoothAdapter adapter;
    private final connectThread connectThread;
    private Thread communicationThread;

    public RealBluetoothProvider() throws BluetoothException {
        adapter = BluetoothAdapter.getDefaultAdapter();
        sentMessageQueue = new LinkedList<>();

        if (adapter == null) {
            throw new BluetoothException("No bluetooth adapter available");
        }

        this.connectThread = new connectThread(adapter);
        this.connectThread.start();

        System.out.println("RealBluetoothProvider Start Communication Thread");
        communicationThread = new Thread(new Runnable() {
            BluetoothSocket socket;

            @Override
            public void run() {
                try {
                    synchronized (RealBluetoothProvider.this.connectThread) {
                        RealBluetoothProvider.this.connectThread.wait();
                    }


                    socket = RealBluetoothProvider.this.connectThread.getSocket();
                    RealBluetoothProvider.this.onConnected();

                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    OutputStreamWriter outputWriter = new OutputStreamWriter(socket.getOutputStream());

                    while (true) {
                        if (inputReader.ready()) {
                            String receivedMessage = inputReader.readLine();
                            onMessageReceived(receivedMessage);
                        } else {
                            String sentMessage;

                            synchronized (sentMessageQueue) {
                                sentMessage = sentMessageQueue.poll();
                            }

                            if (sentMessage != null) {
                                outputWriter.write(sentMessage);
                            }
                        }

                    }
                } catch (Exception e) {
                    onDisconnected();
                }
            }

        });
        communicationThread.start();
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
    public void connectToDevice(Device device) {
        System.out.println("Request connection as client;");
        connectThread.requestConnection((RealDevice) device);
    }

    @Override
    protected void onConnected() {

    }

    @Override
    public void sendMessage(String message) {
        synchronized (sentMessageQueue)
        {
            sentMessageQueue.add(message);
        }
    }

    @Override
    public void disconnect() {

    }

    @Override
    protected void onMessageReceived(String message) {

    }

    @Override
    protected void onDisconnected() {

    }
}
