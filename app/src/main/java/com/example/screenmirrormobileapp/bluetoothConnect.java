package com.example.screenmirrormobileapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;



public class bluetoothConnect extends AppCompatActivity {

    public static final int REQUEST_ENABLE_BT=1;
    ListView appareils_associe;

    Set<BluetoothDevice> set_pairedDevices;
    ArrayAdapter liste_appareils_associe;
    BluetoothAdapter bluetoothAdapter;
    public static final UUID Bluetooth_UUID = UUID.fromString("52be30ba-5471-420c-b666-c42069fd4578");

    public static MediaProjection mediaProjection;
    public MediaProjectionManager mediaProjectionManager;
    public static WindowManager windowManager;
    public Intent intent;
    public int REQUEST_CODE = 999;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaProjectionManager = (MediaProjectionManager) getApplicationContext().getSystemService(MEDIA_PROJECTION_SERVICE);
        intent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, REQUEST_CODE);
        windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);


        setContentView(R.layout.bluetooth);
        init_graph();
        init_bluetooth();
        init_connection();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, intent);
                System.out.println("Printing mediaprojection:  " + mediaProjection);    // toujours null
            }
        }
    }
    public void stop_data(BluetoothSocket sock) {
        final Button stop_data = (Button) findViewById(R.id.cancel1);

        stop_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    sock.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void send_data(BluetoothSocket sock) {
        final Button send_data = (Button) findViewById(R.id.send1);

        send_data.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                ConnectedThread connectedThread = new ConnectedThread(sock);
                SendData sendData = new SendData();
                sendData.start();
            }
        });

    }
    

    public void init_connection(){
        appareils_associe.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object[] objects = set_pairedDevices.toArray();
                BluetoothDevice device = (BluetoothDevice) objects[position];

                ConnectThread connectThread = null;
                connectThread = new ConnectThread(device);
                connectThread.start();

                Toast.makeText(getApplicationContext(),"device choosen "+device.getName(),Toast.LENGTH_SHORT).show();
                
            }
        });
    }
    


    public void init_graph() {
        appareils_associe = (ListView)findViewById(R.id.appareils_associe);
        liste_appareils_associe = new ArrayAdapter(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item);
        appareils_associe.setAdapter(liste_appareils_associe);
    }

    public void init_bluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Votre appareil n'a pas de Bluetooth",Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            set_pairedDevices = bluetoothAdapter.getBondedDevices();
        } else {
            set_pairedDevices = bluetoothAdapter.getBondedDevices();
            if (set_pairedDevices.size() > 0) {
                for (BluetoothDevice device : set_pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress();

                    liste_appareils_associe.add(deviceName + "\n" + deviceHardwareAddress);
                }
            }
        }
    }




    public class ConnectThread extends Thread {
        private final BluetoothSocket btSocket;
        private final BluetoothDevice btDevice;

        public ConnectThread(BluetoothDevice device)  {
            // La Socket utilisée sera finale, on utilise donc une socket intermédiaire tmp
            BluetoothSocket tmp = null;
            btDevice = device;

            // Connexion de la socket Bluetooth avec l'appareil
            try {
                tmp = device.createRfcommSocketToServiceRecord(Bluetooth_UUID);
            } catch (IOException e) { }
            btSocket = tmp;
            send_data(btSocket);
            stop_data(btSocket);
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {

                btSocket.connect();
            } catch (IOException connectException) {
                // Si on ne peut pas se connecter, on ferme la socket
                try {
                    btSocket.close();
                } catch (IOException closeException) { }
                return;
            }
        }


        public void cancel() {
            try {
                btSocket.close();
            } catch (IOException e) { }
        }
    }
    public static class ConnectedThread extends Thread {

        private static BluetoothSocket btSocket;
        private static InputStream btInStream;
        private static OutputStream btOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            btSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            btInStream = tmpIn;
            btOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[2];
            int bytes;

            while (true) {
                try {
                    bytes = btInStream.read(buffer);
                } catch (IOException e) {
                    break;
                }
            }
        }

    }

    public class SendData extends Thread {

        // Fonction permettant d'envoyer des données
        @RequiresApi(api = Build.VERSION_CODES.R)
        public void run() {
            while (true) {
                //sleep(10000);
                if (ConnectedThread.btOutStream != null) {


                    try {
                        PhoneScreen phoneScreen = new PhoneScreen();
                        phoneScreen.createImage();
                        File file = new File(getFilesDir(),"/screen.jpg");
                        byte[] im = convertFileIntoByteArray(file);
                        int fragment = 10000000;
                        byte[] start = "START".getBytes();
                        byte[] stop = "STOP".getBytes();
                        byte[] allByteArray = new byte[start.length + im.length + stop.length];

                        ByteBuffer buff = ByteBuffer.wrap(allByteArray);
                        buff.put(start);
                        buff.put(im);
                        buff.put(stop);

                        byte[] image = buff.array();

                        ConnectedThread.btOutStream.write(image);

                    } catch (IOException e) {
                    }
                    byte[] buffer = new byte[1000];
                    while (true) {
                        try {
                            if (!(ConnectedThread.btInStream.read(buffer) != 2)) break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Log.d("ADebugTag", "Value: not yet");

                    }
                }
            }
        }

        private byte[] convertFileIntoByteArray(File file){
            FileInputStream inputStream;
            byte[] byteArray = new byte[(int) file.length()];
            try{
                inputStream = new FileInputStream(file);
                inputStream.read(byteArray);
                inputStream.close();
            }catch(IOException e){
                e.printStackTrace();
            }
            return byteArray;
        }

    }


}