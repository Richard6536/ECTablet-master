package com.example.richard.ectablet.Services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.MassAirFlowCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

public class OBDService extends Service {

    private BluetoothSocket socket;
    private InputStream is;
    private OutputStreamWriter os;
    private OutputStream ostream;

    public static String btAdress = "D8:3B:BF:E8:6C:81";
    private static final UUID UUID_BT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public BluetoothDevice device;
    BluetoothServerSocket serverSocket;
    public String TAG = "OBDRESULT";

    private static final int DISCOVERABLE_REQUEST_CODE = 0x1;
    private boolean CONTINUE_READ_WRITE = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        new Thread(reader).start();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private Runnable reader = new Runnable() {
        public void run() {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            Log.d(TAG, "BT: " + btAdress);

            try {

                BluetoothDevice device = adapter.getRemoteDevice(btAdress);
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID_BT); //createInsecureRfcommSocketToServiceRecord

                Log.d(TAG, "BT: " + device.getName() + " - " + device.getAddress());

                socket.connect();
                Log.d(TAG, "Pass Socket connect");
                is = socket.getInputStream();
                ostream = socket.getOutputStream();

                new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
                new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
                //new AmbientAirTemperatureCommand().run(socket.getInputStream(), socket.getOutputStream());

                SpeedCommand speedCommand = new SpeedCommand();
                RPMCommand rpmCommand = new RPMCommand();
                MassAirFlowCommand massAirFlowCommand = new MassAirFlowCommand();

                while (!Thread.currentThread().isInterrupted())
                {
                    speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                    rpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                    massAirFlowCommand.run(socket.getInputStream(), socket.getOutputStream());

                    // TODO handle commands result
                    Log.d(TAG, "Speed: " + speedCommand.getFormattedResult());

                    sendMessageToActivity(speedCommand.getFormattedResult(), rpmCommand.getFormattedResult(), massAirFlowCommand.getFormattedResult());
                }

                ostream.flush();
                ostream.close();

                is.close();

                socket.close();
                //Log.d(TAG, "Closed");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG,"Catch Reader 3: " + e.getMessage());
            }
        }
    };


    public void write(String s) throws IOException {
        ostream.write(s.getBytes());
        Log.d(TAG, "write");
    }

    private Runnable runRead = new Runnable() {
        public void run() {
            final int BUFFER_SIZE = 1024;
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes = 0;
            int b = BUFFER_SIZE;

            while (true) {
                try {
                    String result = "";

                    bytes = is.read(buffer, bytes, BUFFER_SIZE - bytes);
                    result = result + new String(buffer, 0, bytes);

                    Log.d(TAG, "read: " + result);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "catch: " + e.getMessage());
                }
            }
        }
    };

    private void sendMessageToActivity(String rpm, String tempMotor, String posAcc) {
        Intent intent = new Intent("intentKey_OBD2");
        // You can also include some extra data.
        intent.putExtra("VEL", rpm);
        intent.putExtra("RPM", tempMotor);
        intent.putExtra("MASS_AIRFLOW", posAcc);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
