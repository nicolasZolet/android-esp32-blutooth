package com.dimelthoz.scheerbluetooth;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private MyBluetoothManager myBluetoothManager;
    private EditText etxtMessageToSend;
    private Button btnSendMessage;
    private TextView txvMessageReceived;
    private Handler handler;

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myBluetoothManager = new MyBluetoothManager(this, "ESP32test");


        initializeVariables();
        buttons();

        handler = new Handler();
        handler.post(updateScreen);
    }

    private final Runnable updateScreen = new Runnable() {
        public void run() {
            if (hasWindowFocus()) {
                txvMessageReceived.setText(myBluetoothManager.communicationThread.getCompleteData());
            }
            handler.postDelayed(updateScreen, 500);
        }
    };

    private void initializeVariables(){
        etxtMessageToSend = findViewById(R.id.edit_text_message_send);
        btnSendMessage = findViewById(R.id.btn_send_message);
        txvMessageReceived = findViewById(R.id.txv_message_received);
    }

    private void buttons(){
        btnSendMessage.setOnClickListener(v -> {
            String messageToSend = etxtMessageToSend.getText().toString();
            myBluetoothManager.communicationThread.write(messageToSend.getBytes());
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        myBluetoothManager.close();
    }

}