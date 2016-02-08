package com.example.bltcamera.utils.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.bltcamera.commons.ThreadHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by hmspl on 6/2/16.
 */
public class BluetoothHandler {

    public static final String ACK_INIT = "acin";
    public static final String ACK_DATA_RECEIVED = "acdr";

    public static final String DATA_END = "dten";
    public static final String DATA_START = "dtst";

    public static final String CAMERA = "camr";
    public static final String RECORD = "recrr";

    private static UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private final boolean mIsWatchView;
    private int mState;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private BluetoothListener mBluetoothListener;
    private final String NAME = "BluetoothHandler";

    private final BluetoothAdapter mBluetoothAdapter;
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection

    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    private static final String TAG = "BluetoothHandler";
    private static final int ARRAY_BUFFER = 1024 * 3;

    public BluetoothHandler(BluetoothListener bluetoothListener, boolean isWatch) {
        mIsWatchView = isWatch;
        mBluetoothListener = bluetoothListener;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothHandler newInstance(BluetoothListener bluetoothListener, boolean isWatch) {
        return new BluetoothHandler(bluetoothListener, isWatch);
    }

    public synchronized void start() {
        setState(STATE_LISTEN);
        resetThreads(true, true, false);
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    public synchronized void connect(BluetoothDevice bluetoothDevice) {
        resetThreads(mState == STATE_CONNECTING, true, false);
        mConnectThread = new ConnectThread(bluetoothDevice);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    private void connected(BluetoothSocket bluetoothSocket, final BluetoothDevice remoteDevice) {
        ThreadHandler.getInstance().doInForground(new Runnable() {
            @Override
            public void run() {
                mBluetoothListener.onConnected(remoteDevice);
            }
        });

        setState(STATE_CONNECTED);
        resetThreads(true, true, true);
        if (mConnectedThread == null) {
            mConnectedThread = new ConnectedThread(bluetoothSocket);
            mConnectedThread.start();
        }
    }

    private void connectionFailed() {
        ThreadHandler.getInstance().doInForground(new Runnable() {
            @Override
            public void run() {
                mBluetoothListener.connectionFailed();
            }
        });
        start();
    }

    public synchronized void stop() {
        setState(STATE_NONE);
        resetThreads(true, true, true);
    }

    public synchronized void write(byte[] data) {
        synchronized (this) {
            if (mState != STATE_CONNECTED) {
                return;
            }
            mConnectedThread.write(data);
        }
    }


    private void resetThreads(boolean connect, boolean connected, boolean accept) {
        if (connect && mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (connected && mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (accept && mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
    }


    public void setState(int state) {
        mState = state;
    }

    public int getState() {
        return mState;
    }

    /**
     * This thread is to connect to a device
     */
    public class ConnectThread extends Thread {

        private BluetoothDevice mBluetoothDevice;
        private BluetoothSocket mBluetoothSocket;

        public ConnectThread(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;
            try {
//                mBluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
                /*UUID uuid = bluetoothDevice.getUuids()[0].getUuid();
                MY_UUID = uuid;*/
                mBluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mBluetoothSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mBluetoothSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                connectionFailed();
                return;
            }

            synchronized (BluetoothHandler.this) {
                mConnectThread = null;
            }

            connected(mBluetoothSocket, mBluetoothDevice);
        }

        public void cancel() {
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * This thread works after establishing bluetooth connection
     */
    public class ConnectedThread extends Thread {

        private InputStream mInputStream;
        private OutputStream mOutputStream;
        private BluetoothSocket mBluetoothSocket;

        public ConnectedThread(BluetoothSocket socket) {
            mBluetoothSocket = socket;
            try {
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] data;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (true) {
                try {
                    int available = mInputStream.available();
                    if (available > 0) {
                        data = new byte[available];
                        mInputStream.read(data);
                        if (mIsWatchView) {
                            int dataLength = data.length;
                            if (dataLength == 4) {
                                String key = new String(data, "UTF-8").trim();
                                if (key.equals(DATA_START)) {
                                    write(ACK_INIT.getBytes());
                                    byteArrayOutputStream = new ByteArrayOutputStream();
                                } else if (key.equals(DATA_END)) {
                                    final ByteArrayOutputStream finalByteArrayOutputStream = byteArrayOutputStream;

                                    mBluetoothListener.onReceivedData(finalByteArrayOutputStream.toByteArray());

                                    write(ACK_DATA_RECEIVED.getBytes()); //Sending the data received ack
                                }
                            } else {
                                byteArrayOutputStream.write(data);
                                final int completeDataSize = byteArrayOutputStream.size();
                                if (completeDataSize > 4) {
                                    final byte[] completeData = byteArrayOutputStream.toByteArray();
                                    byte[] keyData = Arrays.copyOfRange(completeData, completeDataSize - 4, completeDataSize);
                                    String key = new String(keyData, "UTF-8").trim();
                                    if (key.equals(DATA_END)) {

                                        mBluetoothListener.onReceivedData(Arrays.copyOfRange(completeData, 0, completeDataSize - 4));

                                        write(ACK_DATA_RECEIVED.getBytes()); //Sending the data received ack
                                    }
                                }
                            }
                        } else {
                            final String receivedKey = new String(data, "UTF-8").trim();
                            ThreadHandler.getInstance().doInForground(new Runnable() {
                                @Override
                                public void run() {
                                    mBluetoothListener.onGotAck(receivedKey);
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                    BluetoothHandler.this.start();
                    ThreadHandler.getInstance().doInForground(new Runnable() {
                        @Override
                        public void run() {
                            mBluetoothListener.onLostConnection();
                        }
                    });
                    break;
                }
            }
        }

        public void write(byte[] data) {
            try {
               /* for (int i = 0; i < data.length; i += ARRAY_BUFFER) {
                    int len;
                    if (data.length - i >= ARRAY_BUFFER) {
                        len = ARRAY_BUFFER;
                        mOutputStream.write(data, i, len);
                    } else {
                        len = data.length - i;
                        mOutputStream.write(data, i, len);
                    }
                }*/
                mOutputStream.write(data);
                mOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                mBluetoothListener.onLostConnection();
            }
        }

        public void cancel() {
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This thread will be listening to new connections
     */
    public class AcceptThread extends Thread {

        private BluetoothServerSocket mBluetoothServerSocket;

        public AcceptThread() {
            try {
                mBluetoothServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            BluetoothSocket socket = null;
            while (mState != STATE_CONNECTED) {
                try {
                    socket = mBluetoothServerSocket.accept();
                    Log.i(TAG, "Socket accepted");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (socket != null) {
                    switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            Log.i(TAG, "Socket close failed");
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                    }
                }
            }
        }

        public void cancel() {
            try {
                mBluetoothServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
