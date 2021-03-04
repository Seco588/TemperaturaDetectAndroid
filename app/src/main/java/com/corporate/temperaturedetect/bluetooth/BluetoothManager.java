package com.corporate.temperaturedetect.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothManager {
    private final String TAG = "BtManagement";
    private final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private final BluetoothAdapter adapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;


    public BluetoothManager(BluetoothAdapter adapter) {
        this.adapter = adapter;
        bluetoothDevice = null;
        inputStream = null;
        outputStream = null;
        socket = null;
    }

    public boolean createBondState() {
        if (adapter.getBondedDevices().contains(bluetoothDevice)) {
            int bondState = bluetoothDevice.getBondState();
            String tmp = " - ";
            //Bonded already
            if (bondState == BluetoothDevice.BOND_BONDED) {
                tmp = "BOND_BONDED";
            }
            //Creating bond
            if (bondState == BluetoothDevice.BOND_BONDING) {
                tmp = "BOND_BONDING";//never used
            }
            //Breaking bond
            if (bondState == BluetoothDevice.BOND_NONE) {
                tmp = "BOND_NONE";//never used
            }
            Log.d(TAG, "Already paired : " + tmp);
            return BluetoothDevice.BOND_BONDED == bluetoothDevice.getBondState();
        } else {
            boolean bond = bluetoothDevice.createBond();
            Log.d(TAG, "Create bond \nBond state : " + bond);
            return BluetoothDevice.BOND_BONDED == bluetoothDevice.getBondState();
        }
    }


    public boolean createSocket() {
        Log.d(TAG, "Create socket...");
        try {
            socket = bluetoothDevice.createRfcommSocketToServiceRecord(SERIAL_UUID);
            Log.d(TAG, "socket OK ");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "socket KO");
            return false;
        }
    }

    public boolean connectSocket() {
        Log.d(TAG, "Connect socket...");

        adapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            socket.connect();
            return true;
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                socket.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
            return false;
        }
    }

    public void reloadConnection() {
        boolean b3 = disconnectSteam();
        boolean b2 = disconnectSocket();
        boolean socket = createSocket();
        boolean b1 = connectSocket();
        boolean b = connectedStream();
        Log.d(TAG, "Reload connection : "
                + "\ndisconnectSteam : " + b3
                + "\ndisconnectSocket : " + b2
                + "\ncreateSocket : " + socket
                + "\nconnectSocket : " + b1
                + "\nconnectedStream : " + b
        );
    }

    public boolean connectedStream() {
        Log.d(TAG, "Connect stream...");
        int check = 0;

        try {
            inputStream = socket.getInputStream();
            Log.d(TAG, "inputStream OK ");
            check++;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "inputStream KO");
        }

        try {
            outputStream = socket.getOutputStream();
            Log.d(TAG, "outputStream OK ");
            check++;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "outputStream KO");
        }

        return check == 2;
    }

    public boolean disconnectSocket() {
        Log.d(TAG, "Disconnect socket");
        try {
            socket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean disconnectSteam() {
        Log.d(TAG, "Disconnect stream");
        int check = 0;

        try {
            inputStream.close();
            Log.d(TAG, "inputStream CLOSE ");
            check++;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "inputStream ERROR OPEN");
        }

        try {
            outputStream.close();
            Log.d(TAG, "outputStream CLOSE ");
            check++;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "outputStream ERROR OPEN");
        }

        return check == 2;
    }

    public boolean sendData(String data) {
        try {
            outputStream.write(data.getBytes());
            ///  outputStream.flush();
            Log.d(TAG, "Send command : " + data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String receveData() {
        byte b = 0;
        StringBuilder res = new StringBuilder();
        // read until ‘>’ arrives OR end of stream reached char C;
        while (true) {
            try {
                //read
                b = (byte) inputStream.read();
            } catch (IOException e) {
                //  Log.e(TAG, "Bluetooth data reading failed");
                e.printStackTrace();
                res.append(" - ");
                break;
            }
            if (b == -1) {
                // -1 if the end of the stream is reached
                //  Log.d(TAG, "Finished characters...\n finished reading");
                break;
            }
            char c = (char) b;
            if (c == 'C') {
                // read until ‘C’ arrives
                // Log.d(TAG, "Escape charter 'C' found...\n finished reading");
                break;
            }
            res.append(c);
        }
        Log.d(TAG, "Read data from " + bluetoothDevice.getName() + " completed !\n Response : " + res);
        return res.toString();

    }

    public void setRemoteDevice(String mac_address) {
        this.bluetoothDevice = null;
        this.bluetoothDevice = adapter.getRemoteDevice(mac_address);
        Log.d(TAG, "Select : " + bluetoothDevice.getName());
    }

    public void setRemoteDevice(BluetoothDevice remoteDevice) {
        this.bluetoothDevice = remoteDevice;
    }

    public BluetoothDevice getRemoteDevice() {
        return bluetoothDevice;
    }

    public BluetoothAdapter getAdapter() {
        return adapter;
    }

}
