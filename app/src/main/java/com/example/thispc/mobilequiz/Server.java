package com.example.thispc.mobilequiz;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class Server extends AppCompatActivity {


    int c;

    boolean refreshEnabled = false;

    private BluetoothAdapter bluetoothAdapter;
    public static String MyName = "";
    Button btn;
    int array[];
    EditText name;
    private ListView listview;
    DataBaseHandler dbh;
    private ArrayAdapter adapter;
    private static final int ENABLE_BT_REQUEST_CODE = 1;
    private static final int DISCOVERABLE_BT_REQUEST_CODE = 2;
    private ArrayList<UUID> mUuids;
    private static final int Finished_Activity = 3;
    private static final int DISCOVERABLE_DURATION = 300;
    public static BluetoothDevice mBluetoothDevice = null;
    boolean check = true;
    String playname = "";
    public static BluetoothSocket mBluetoothSocket = null;
    public static int finalscore[];
    public static int arraylength=0;
    ListeningThread t = null;
    ConnectedThread ct = null;
    public static BluetoothSocket a[];
    int a1 = 0;
    int b;
    TextView playerhead;
    TextView scorehead;
    TextView p1;
    TextView p2;
    TextView p3;
    TextView p4;
    TextView p5;
    TextView s1;
    TextView s2;
    TextView s3;
    TextView s4;
    TextView s5;
   public static ConnectedThread carray[];
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                adapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        playerhead = (TextView) findViewById(R.id.Playerhead);
        scorehead = (TextView) findViewById(R.id.Scorehead);
        Typeface m=Typeface.createFromAsset(getAssets(),"MING____.ttf");
        playerhead.setTypeface(m);
        Typeface m1=Typeface.createFromAsset(getAssets(),"MING____.ttf");
        scorehead.setTypeface(m1);
        carray=new ConnectedThread[5];
        p1 = (TextView) findViewById(R.id.player1);
        p2 = (TextView) findViewById(R.id.player2);
        p3 = (TextView) findViewById(R.id.player3);
        p4 = (TextView) findViewById(R.id.player4);
        p5 = (TextView) findViewById(R.id.player5);
        s1 = (TextView) findViewById(R.id.score1);
        s2 = (TextView) findViewById(R.id.score2);
        s3 = (TextView) findViewById(R.id.score3);
        s4 = (TextView) findViewById(R.id.score4);
        s5 = (TextView) findViewById(R.id.score5);
        btn = (Button) findViewById(R.id.btn_find);
        Typeface m2=Typeface.createFromAsset(getAssets(),"MING____.ttf");
        btn.setTypeface(m2);
        dbh = new DataBaseHandler(this);
        mUuids = new ArrayList<UUID>();
        a = new BluetoothSocket[2];
        mUuids.add(UUID.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"));
        mUuids.add(UUID.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"));
        mUuids.add(UUID.fromString("e442e09a-51f3-4a7b-91cb-f638491d1412"));
        mUuids.add(UUID.fromString("a81d6504-4536-49ee-a475-7d96d09439e4"));
      btn.setOnClickListener(
              new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {


                          if (bluetoothAdapter == null) {
                              Toast.makeText(getApplicationContext(), " Your device does not support Bluetooth",
                                      Toast.LENGTH_SHORT).show();
                          } else if (!refreshEnabled) {
                              refreshEnabled = true;
                              btn.setText("STOP");
                              Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                              startActivityForResult(enableBluetoothIntent, ENABLE_BT_REQUEST_CODE);
                              Toast.makeText(Server.this, "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
                          } else if (refreshEnabled) {
                              btn.setText("Find Client");
                              refreshEnabled = false;
                              adapter.clear();
                              bluetoothAdapter.disable();
                          }
                    //  }
                  }
              }
      );
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      listview = (ListView) findViewById(R.id.listView);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemValue = (String) listview.getItemAtPosition(position);
                String MAC = itemValue.substring(itemValue.length() - 17);
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(MAC);


            }
        });
        if (bluetoothAdapter.isEnabled())
        {
            bluetoothAdapter.disable();
            adapter.clear();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_BT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth enabled." + "\n" + "Scanning for peers", Toast.LENGTH_SHORT).show();

                makeDiscoverable();
                discoverDevices();

                t = new ListeningThread();
                t.start();


            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth is not enabled.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == DISCOVERABLE_BT_REQUEST_CODE) {
            if (resultCode == DISCOVERABLE_DURATION) {
                Toast.makeText(getApplicationContext(), "Your device is now discoverable for Clients", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Fail to enable discoverable mode.", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == Finished_Activity) {
            bluetoothAdapter.disable();
            adapter.clear();
            refreshEnabled = false;
        }
    }

    public synchronized void connected(BluetoothSocket socket, int c) {

        mBluetoothSocket = socket;
        ct = new ConnectedThread(socket, c);
        carray[c-1]=ct;
        ct.start();

    }

    protected void discoverDevices() {
        if (bluetoothAdapter.startDiscovery()) {
            Toast.makeText(getApplicationContext(), "Discovering peers", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Discovery failed to start.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void makeDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        startActivityForResult(discoverableIntent, DISCOVERABLE_BT_REQUEST_CODE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(broadcastReceiver);
    }
    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private int cnt = 0;
        int playnum;

        public ConnectedThread(BluetoothSocket socket, int p) {
          playnum = p;
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {

            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            String s="";
           for (int i = 1; i < Main2Activity.c; i++) {
                RandomQuestionsType rqt = dbh.getRandomQuestionsType(i);
                final int finalI = i;
                s+=rqt.getId1() + "[" + rqt.getId2() + "]" + rqt.getType()+";";
            }
            s=s+"+"+CategoryForServer.Duration+"/"+playnum;
            ct.write((s).getBytes());
            // Keep listening to the InputStream while connected
            while (true) {
                try {

                    String readMessage = "";
                    bytes = mmInStream.read(buffer);
                    readMessage = new String(buffer, 0, bytes);
                    if (readMessage.contains(".")) {
                        final String finalReadMessage = readMessage;

                        if(readMessage.charAt(1)=='1')
                        {
                            p1.setText(readMessage.substring(2));
                        }
                        if(readMessage.charAt(1)=='2')
                        {
                            p2.setText(readMessage.substring(2));
                        }
                        if(readMessage.charAt(1)=='3')
                        {
                            p3.setText(readMessage.substring(2));
                        }
                        if(readMessage.charAt(1)=='4')
                        {
                            p4.setText(readMessage.substring(2));
                        }
                        if(readMessage.charAt(1)=='5')
                        {
                            p5.setText(readMessage.substring(2));
                        }
                    }
              if (readMessage.contains("?")) {
                    final String finalReadMessage1 = readMessage;
                  if (readMessage.charAt(1) == '1') {
                      final String finalReadMessage = readMessage;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                s1.setText(finalReadMessage.substring(2, finalReadMessage.indexOf(">")));
                                p1.setText(finalReadMessage.substring(finalReadMessage.indexOf(">") + 1));
                            }
                        });

                    }
                    if (readMessage.charAt(1) == '2') {

                        final String finalReadMessage = readMessage;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                s2.setText(finalReadMessage.substring(2, finalReadMessage.indexOf(">")));
                                p2.setText(finalReadMessage.substring(finalReadMessage.indexOf(">") + 1));
                            }
                        });

                    }
                    if (readMessage.charAt(1) == '3') {
                        final String finalReadMessage = readMessage;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                s3.setText(finalReadMessage.substring(2, finalReadMessage.indexOf(">")));
                                p3.setText(finalReadMessage.substring(finalReadMessage.indexOf(">") + 1));
                            }
                        });

                    }
                    if (readMessage.charAt(1) == '4') {
                        final String finalReadMessage = readMessage;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                s4.setText(finalReadMessage.substring(2, finalReadMessage.indexOf(">")));
                                p4.setText(finalReadMessage.substring(finalReadMessage.indexOf(">") + 1));
                            }
                        });

                    }
                    if (readMessage.charAt(1) == '5') {
                        final String finalReadMessage = readMessage;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                s5.setText(finalReadMessage.substring(2, finalReadMessage.indexOf(">")));
                                p5.setText(finalReadMessage.substring(finalReadMessage.indexOf(">") + 1));
                            }
                        });
                    }
                }
                    if (readMessage.contains("=")) {
                        final String finalReadMessage1 = readMessage;
                         arraylength++;
                        if (readMessage.charAt(1) == '1') {
                            final String finalReadMessage = readMessage;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    s1.setText(finalReadMessage.substring(2, finalReadMessage.indexOf(">")));
                                    p1.setText(finalReadMessage.substring(finalReadMessage.indexOf(">") + 1));
                                }
                            });
                            finalscore[0]=Integer.parseInt(readMessage.substring(2,readMessage.indexOf(">")));
                        }
                        if (readMessage.charAt(1) == '2') {
                            final String finalReadMessage = readMessage;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    s2.setText(finalReadMessage.substring(2, finalReadMessage.indexOf(">")));
                                    p2.setText(finalReadMessage.substring(finalReadMessage.indexOf(">") + 1));
                                }
                            });
                            finalscore[1]=Integer.parseInt(readMessage.substring(2,readMessage.indexOf(">")));

                        }
                        if (readMessage.charAt(1) == '3') {
                            final String finalReadMessage = readMessage;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    s3.setText(finalReadMessage.substring(2, finalReadMessage.indexOf(">")));
                                    p3.setText(finalReadMessage.substring(finalReadMessage.indexOf(">") + 1));
                                }
                            });
                            finalscore[2]=Integer.parseInt(readMessage.substring(2,readMessage.indexOf(">")));

                        }
                        if (readMessage.charAt(1) == '4') {
                            final String finalReadMessage = readMessage;

                            runOnUiThread(new Runnable() {
                                public void run() {

                                    s4.setText(finalReadMessage.substring(2, finalReadMessage.indexOf(">")));
                                    p4.setText(finalReadMessage.substring(finalReadMessage.indexOf(">") + 1));
                                }
                            });
                            finalscore[3]=Integer.parseInt(readMessage.substring(2,readMessage.indexOf(">")));

                        }
                        if (readMessage.charAt(1) == '5') {
                            final String finalReadMessage = readMessage;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    s5.setText(finalReadMessage.substring(2, finalReadMessage.indexOf(">")));
                                    p5.setText(finalReadMessage.substring(finalReadMessage.indexOf(">") + 1));
                                }
                            });
                            finalscore[4]=Integer.parseInt(readMessage.substring(2,readMessage.indexOf(">")));

                        }
                        waitingforresult();
                    }
                } catch (Exception e) {

                    break;
                }
            }
        }
      public void waitingforresult()
        {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "in waiting for results", Toast.LENGTH_SHORT).show();

                }
            });

            array=new int[a1];
            int d;
            boolean c=true;
            while(c)
            {
                if(arraylength==a1)
                {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "in if", Toast.LENGTH_SHORT).show();

                        }
                    });

                    array=finalscore;
                    c=false;
                    for (int i=0;i<a1;i++)
                    {
                        for (int j=0;j<a1-1;j++)
                        {
                            if(array[j]>array[j+1])
                            {
                                d=array[j];
                                array[j]=array[j+1];
                                array[j+1]=d;
                            }
                        }
                    }
                    for (int i=0;i<a1;i++)
                    {

                        if(finalscore[playnum-1]==array[i])
                        {
                            final int finalI = i;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "in arranging ranks", Toast.LENGTH_SHORT).show();
                                }
                            });
                            carray[playnum-1].write(("()" + (finalI + 1)).getBytes());
                            break;
                        }
                    }
                }
            }
        }
        public void write(byte[] buffer) {

            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {

            }
        }
    }
    private class ListeningThread extends Thread {
        BluetoothServerSocket bluetoothServerSocket;
        BluetoothServerSocket temp = null;

        public ListeningThread() {

        }

        public void run() {
            BluetoothSocket bluetoothSocket = null;
            for (int i = 0; i < mUuids.size(); i++) {
                try {


                    temp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(getString(R.string.app_name), mUuids.get(i));

                } catch (IOException e) {
                    e.printStackTrace();
                }
                check = true;
                bluetoothServerSocket = temp;
                b = 0;
                while (check) {
                    try {
                        bluetoothSocket = bluetoothServerSocket.accept();

                    } catch (IOException e) {
                        break;
                    }
                    for (i = 0; i < a1; i++) {
                        if (bluetoothSocket.equals(a[i]))
                            b++;
                    }
                    if (b == 0) {

                        a[a1] = bluetoothSocket;
                        check = false;
                        a1++;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                       c++;
                                Toast.makeText(getApplicationContext(), "Total "+c+" connection accepted.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        finalscore=new int[a1];
                        connected(bluetoothSocket,a1);
                    }

                }

         /*  try {
                    bluetoothServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }


        }
    }


}