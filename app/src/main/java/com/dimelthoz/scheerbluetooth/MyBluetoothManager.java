package com.dimelthoz.scheerbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MyBluetoothManager {

    private Context context;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String BLUETOOTH_LOG = "BT_LOG";
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private android.bluetooth.BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private ConnectThread connectThread;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public MyBluetoothManager(Context context){
        this.context = context;

        bluetoothManager = context.getSystemService(android.bluetooth.BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();

        verifyBlutooth();
    }

    private void verifyBlutooth(){
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "This devices doesn't support Bluetooth.", Toast.LENGTH_SHORT).show();
            ((Activity)context).finish();
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity)context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            bluetoothEnabled();
        }
    }

    private void bluetoothEnabled() {
        Toast.makeText(context, "Bluetooth is enabled.", Toast.LENGTH_SHORT).show();
        Log.d(BLUETOOTH_LOG, "Bluetooth is enabled.");
        logPairedDevices();
        discoverDevices();
    }

    private void logPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        Log.d(BLUETOOTH_LOG, "[Paired Devices]: ");
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {

                String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                Log.d(BLUETOOTH_LOG, "[Device name]: " + deviceName);
                Log.d(BLUETOOTH_LOG, "[Device address]: " + deviceAddress);

                if (deviceName.equals("ESP32test")) {
                    Log.d(BLUETOOTH_LOG, "Find ESP32");
                    connectThread = new ConnectThread(device);
                    connectThread.start();
                }
            }
        }
    }

    private void discoverDevices() {
        /*
            Caution: Performing device discovery consumes a lot of the Bluetooth adapter's resources.
            After you have found a device to connect to, be certain that you stop
            discovery with cancelDiscovery() before attempting a connection.
            Also, you shouldn't perform discovery while connected to a device because the discovery
            process significantly reduces the bandwidth available for any existing connections.
         */
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(receiver, filter);
        bluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d(BLUETOOTH_LOG, "\n[Bluetooth found a device]: ");

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String deviceName = device.getName();
                String deviceAddress = device.getAddress();

                Log.d(BLUETOOTH_LOG, "[Device name]: " + deviceName);
                Log.d(BLUETOOTH_LOG, "[Device address]: " + deviceAddress);

                if (deviceName.equals("ESP32test")) {
                    Log.d(BLUETOOTH_LOG, "Find ESP32");

                    connectThread = new ConnectThread(device);
                    connectThread.start();
                }

                Log.d(BLUETOOTH_LOG, "");
            }
        }
    };

    private void manageMyConnectedSocket(){

    }

    public class ConnectThread extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final BluetoothDevice bluetoothDevice;

        public ConnectThread(BluetoothDevice bluetoothDevice){
            this.bluetoothDevice = bluetoothDevice;
            BluetoothSocket tmpBluetoothSocket = null;

            try{
                tmpBluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

            bluetoothSocket = tmpBluetoothSocket;
        }

        public void run(){
            bluetoothAdapter.cancelDiscovery();
            try {
                bluetoothSocket.connect();
                Log.d(BLUETOOTH_LOG, "Successfully connected to device!");
                manageMyConnectedSocket();
            } catch (IOException e) {
                Log.d(BLUETOOTH_LOG, "Error connecting to device: ");
                e.printStackTrace();
                try{
                    bluetoothSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close(){
        connectThread.cancel();
        context.unregisterReceiver(receiver);
    }
}
