package com.dimelthoz.scheerbluetooth;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private MyBluetoothManager myBluetoothManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myBluetoothManager = new MyBluetoothManager(this);
    }

    protected void onDestroy() {
        super.onDestroy();
        myBluetoothManager.close();
    }

}