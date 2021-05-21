package com.example.screenmirrormobileapp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
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



    String bluetooth_message="Salut ca va ?";




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);
        init_graph();
        init_bluetooth();
        init_connection();

    }

  /*  @RequiresApi(api = Build.VERSION_CODES.R)
    public void writing_data(BluetoothSocket sock) {
        ConnectedThread connectedThread = new ConnectedThread(sock);
        //Thread thread = new Thread(connectedThread.write());
        SendData sendData = new SendData();
        sendData.start();
        //connectedThread.write();
    } */

  /*  @SuppressLint("WrongConstant")
    void initImageRead(MediaProjection mediaProjection) {
        if (mediaProjection == null) {

            return;
        }
        int width = 1080;
        int height = 1920;
        int dpi = 400;
        ImageReader mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);

        mediaProjection.createVirtualDisplay("ScreenCapture",
                width, height, dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    } */


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

    public void send_data(byte[] bytes, BluetoothSocket sock) {
        final Button send_data = (Button) findViewById(R.id.send1);

        send_data.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {

                ConnectedThread connectedThread = new ConnectedThread(sock);
                //Thread thread = new Thread(connectedThread.write());
                SendData sendData = new SendData();
                sendData.start();

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
            set_pairedDevices = bluetoothAdapter.getBondedDevices();

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
    private static class ConnectedThread extends Thread {

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
            } catch (IOException e) {
            }

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

    private class SendData extends Thread {

        // Fonction permettant d'envoyer des données
        @RequiresApi(api = Build.VERSION_CODES.R)
        public void run() {
            while (1 == 1) {
                //sleep(10000);
                if (ConnectedThread.btOutStream != null) {


                    try {
                        View v1 = getWindow().getDecorView().getRootView();
                        v1.setDrawingCacheEnabled(true);
                        Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
                        v1.setDrawingCacheEnabled(false);

                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 1, out);
                        byte[] im = out.toByteArray();
                        int fragment = 10000000;
                        byte[] start = "START".getBytes();
                        byte[] stop = "STOP".getBytes();
                        byte[] allByteArray = new byte[start.length + im.length + stop.length];

                        ByteBuffer buff = ByteBuffer.wrap(allByteArray);
                        buff.put(start);
                        buff.put(im);
                        buff.put(stop);

                        byte[] image = buff.array();
                        /*for (int i = 0; i*fragment < image.length; i++) {
                            if (i*fragment + fragment > image.length){

                            }
                        }*/
                        ConnectedThread.btOutStream.write(image);
                        //mMediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                        //mMediaProjection = mMediaProjectionManager.getMediaProjection(Activity.RESULT_OK, (Intent) bundle.getParcelable("data"));
                    } catch (IOException e) {
                    }
                    byte[] buffer = new byte[1000];
                    while (true) {
                        try {
                            if (!(ConnectedThread.btInStream.read(buffer) != 2)) break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //int confirmation = btInStream.read(buffer);
                        //Log.d("ADebugTag", "Value: " + Integer.toString(confirmation));
                        Log.d("ADebugTag", "Value: not yet");

                        //if (confirmation != 0) {
                        //    Toast.makeText(getApplicationContext(), confirmation, Toast.LENGTH_SHORT).show();
                        //}

                    }

                }
                //sleep(1000);
            }
        }

        // Fonction permettant de fermer la connexion
        public void cancel() {
            try {
                ConnectedThread.btSocket.close();
            } catch (IOException e) { }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private byte[] Screenshot() {
        View v1 = getWindow().getDecorView().getRootView();

        v1.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 1, out);


        return out.toByteArray();
    }
}