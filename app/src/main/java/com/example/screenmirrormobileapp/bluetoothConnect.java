package com.example.screenmirrormobileapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;



public class bluetoothConnect extends AppCompatActivity {

    public static final int REQUEST_ENABLE_BT=1;
    ListView appareils_associe;

    Set<BluetoothDevice> set_pairedDevices;
    ArrayAdapter liste_appareils_associe;
    BluetoothAdapter bluetoothAdapter;
    public static final UUID Bluetooth_UUID = UUID.fromString("52be30ba-5471-420c-b666-c42069fd4578");



    String bluetooth_message="Salut ca va ?";




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);
        init_graph();
        init_bluetooth();
        init_connection();

    }

    public void send_data(byte[] bytes, BluetoothSocket sock) {
        final Button send_data = (Button) findViewById(R.id.send1);

        send_data.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ConnectedThread connectedThread = new ConnectedThread(sock);
                try {
                    connectedThread.write();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    

    public void init_connection()
    {
        appareils_associe.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Object[] objects = set_pairedDevices.toArray();
                BluetoothDevice device = (BluetoothDevice) objects[position];

                ConnectThread connectThread = null;
                connectThread = new ConnectThread(device);
                connectThread.start();

                Toast.makeText(getApplicationContext(),"device choosen "+device.getName(),Toast.LENGTH_SHORT).show();
                
            }
        });
    }
    


    public void init_graph()
    {
        appareils_associe = (ListView)findViewById(R.id.appareils_associe);
        liste_appareils_associe = new ArrayAdapter(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item);
        appareils_associe.setAdapter(liste_appareils_associe);
    }

    public void init_bluetooth()
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {

            Toast.makeText(getApplicationContext(),"Votre appareil n'a pas de Bluetooth",Toast.LENGTH_SHORT).show();

            finish();
        }



        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        else {
            set_pairedDevices = bluetoothAdapter.getBondedDevices();

            if (set_pairedDevices.size() > 0) {

                for (BluetoothDevice device : set_pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress();

                    liste_appareils_associe.add(device.getName() + "\n" + device.getAddress());
                }
            }
        }
    }




    private class ConnectThread extends Thread {
        private final BluetoothSocket btSocket;
        private final BluetoothDevice btDevice;

        public ConnectThread(BluetoothDevice device)  {
            // La Socket utilisée sera finale, on utilise donc une socket intermédiaire tmp
            BluetoothSocket tmp = null;
            btDevice = device;

            // Connection de la socket Bluetooth avec l'appareil
            try {
                tmp = device.createRfcommSocketToServiceRecord(Bluetooth_UUID);
            } catch (IOException e) { }
            btSocket = tmp;

            send_data(bluetooth_message.getBytes(), btSocket);
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
    private class ConnectedThread extends Thread {

        private final BluetoothSocket btSocket;
        private final InputStream btInStream;
        private final OutputStream btOutStream;

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

        // Fonction permettant d'envoyer des données
        public void write() throws InterruptedException {
            //while (1 == 1) {
                if (btOutStream != null) {


                    try {
                        btOutStream.write(Screenshot());
                    } catch (IOException e) {
                    }
                }
                sleep(100);
            //}
        }

        // Fonction permettant de fermer la connexion
        public void cancel() {
            try {
                btSocket.close();
            } catch (IOException e) { }
        }
    }

    private byte[] Screenshot() {
        View v1 = getWindow().getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        return out.toByteArray();
    }
}