package com.example.screenmirrormobileapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Get the default adapter
    public BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public BluetoothHeadset bluetoothHeadset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // adapter is null => bluetooth not supported
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "No Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // request for bluetooth activation
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 2);
        }
        establishConnection();
    }

    // Establish connection to proxy
    public void establishConnection(){
        bluetoothAdapter.getProfileProxy(this, serviceListener, BluetoothProfile.HEADSET);
    }

    public BluetoothProfile.ServiceListener serviceListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = (BluetoothHeadset) proxy;
            }
        }
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = null;
            }
        }
    };

}