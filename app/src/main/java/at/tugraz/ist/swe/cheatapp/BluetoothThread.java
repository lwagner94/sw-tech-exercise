package at.tugraz.ist.swe.cheatapp;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

import static at.tugraz.ist.swe.cheatapp.Constants.BLUETOOTH_SERVICE_RECORD;
import static at.tugraz.ist.swe.cheatapp.Constants.BLUETOOTH_UUID;

public class BluetoothThread extends Thread {
    private RealBluetoothProvider provider;
    private Queue<BluetoothMessage> messageQueue;
    private boolean running;


    public BluetoothThread(final RealBluetoothProvider provider) {
        this.provider = provider;
        this.messageQueue = new LinkedList<>();
        this.running = true;
    }

    public synchronized void sendBluetoothMessage(final BluetoothMessage bluetoothMessage) {
        messageQueue.add(bluetoothMessage);
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        try {
            BluetoothSocket socket = null;
            final BluetoothServerSocket serverSocket = provider.getAdapter().listenUsingRfcommWithServiceRecord(BLUETOOTH_SERVICE_RECORD, BLUETOOTH_UUID);

            boolean runLoop = true;

            // TODO: Maybe put this part into own method
            ////////////////////////////////////////////////////////////////////////////////////
            while (runLoop && socket == null) {

                try {
                    socket = serverSocket.accept(200);
                    System.out.println("CommunicationThread: Connected as server.");
                } catch (IOException timeout) {
                    // Timeout
                    synchronized (this) {
                        RealDevice dev = provider.getDevice();
                        if (dev != null) {
                            System.out.println("CommunicationThread: Connection as client requested");
                            try {
                                socket = dev.getDevice().createRfcommSocketToServiceRecord(BLUETOOTH_UUID);
                                if (socket != null) {
                                    Log.d("RealBluetoothProvider", "before connect");
                                    socket.connect();
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                throw new RuntimeException(ex.getMessage());
                            }

                            runLoop = running;
                        }


                    }
                }
            }

            serverSocket.close();

            // TODO: Maybe put this part into own method
            ////////////////////////////////////////////////////////////////////////////////////

            BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outputWriter = new PrintWriter(socket.getOutputStream());

            synchronized (this) {
                messageQueue.add(new BluetoothMessage(new ConnectMessage(BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME)));
            }
            boolean disconnected = false;

            while (runLoop) {
                synchronized (this) {
                    if (inputReader.ready()) {
                        String receivedMessage = inputReader.readLine();
                        final BluetoothMessage btMessage = BluetoothMessage.fromJSONString(receivedMessage);

                        switch (btMessage.getMessageType()) {
                            case CHAT:
                                provider.onMessageReceived(btMessage.getMessage());
                                break;
                            case CONNECT:
                                provider.onConnected();
                                break;
                            case DISCONNECT:
                                disconnected = true;
                                break;

                        }
                    } else {
                        BluetoothMessage message;
                        message = messageQueue.poll();


                        if (message != null) {
                            outputWriter.println(message.toJSONString());
                            outputWriter.flush();
                        }
                    }

                    runLoop = !messageQueue.isEmpty() || running && !disconnected;
                }
                Thread.sleep(100);
            }

            inputReader.close();
            outputWriter.close();
            socket.close();

            provider.onDisconnected();

        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } catch (JSONException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
};