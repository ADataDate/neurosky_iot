package lazerade.com.mindcontrolledhomeautomation;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.AlgoSdk.NskAlgoDataType;
import com.neurosky.AlgoSdk.NskAlgoSdk;
import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "Mindful";
    private NskAlgoSdk nskAlgoSdk;
    private TgStreamReader tgStreamReader;
    private BluetoothAdapter mBluetoothAdapter;
    private String connectionState;
    private int seekState;
    private static final int SEEK_BAR_INITIAL_STATE = 75;
    private long lastSwitch = 0;

    /**
     * Initialize blue tooth, headset button, and the seek bar
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nskAlgoSdk = new NskAlgoSdk();
        setupBlueTooth();
        setupRPiButton();
        setupHeadSetButton();
        setupSeekBar();
    }

    /**
     * Create the options menu for the IP address
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Start the settings activity
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Set up the bluetooth
     */
    private void setupBlueTooth() {
        try {
            // (1) Make sure that the device supports Bluetooth and Bluetooth is on
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Toast.makeText(
                        this,
                        "Please enable your Bluetooth and re-run this program !",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.getMessage());
            return;
        }
    }

    /**
     * Set up the head set button
     */
    private void setupHeadSetButton() {
        Button headSet = findViewById(R.id.headSet);

        headSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Example of constructor public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
                tgStreamReader = new TgStreamReader(mBluetoothAdapter, callback);

                if (tgStreamReader != null && tgStreamReader.isBTConnected()) {
                    tgStreamReader.stop();
                    tgStreamReader.close();
                }
                tgStreamReader.connect();
            }
        });
    }

    private SocketClient createClient() {
        SharedPreferences prefs;
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SocketClient client = new SocketClient(prefs.getString(getString(R.string.IP_ADDRESS), "0.0.0.0"));
        return client;
    }
    /**
     * Set up the rPi button
     */
    private void setupRPiButton() {
        Button rPiButton = findViewById(R.id.rPiButton);

        rPiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendCheck().execute();
            }
        });
    }

    /**
     * Set up the seek bar
     */
    private void setupSeekBar() {
        SeekBar sBar = findViewById(R.id.settingBar);

        // Set up seek bar that is the threshold for Attention
        sBar.setProgress(SEEK_BAR_INITIAL_STATE);
        seekState = SEEK_BAR_INITIAL_STATE;
        sBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "Setting seek state: " + progress);
                seekState = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * Stream handler callback
     */
    private TgStreamHandler callback = new TgStreamHandler() {
        private static final int TIMEOUT = 10000;

        /**
         * Set the connection state string and display to UI
         * @param connectionStates
         */
        @Override
        public void onStatesChanged(int connectionStates) {
            Log.d(TAG, "connectionStates change to: " + connectionStates);
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTING:
                    connectionState = "Connecting...";
                    break;
                case ConnectionStates.STATE_CONNECTED:
                    // Do something when connected
                    tgStreamReader.start();
                    connectionState = "Connected";
                    break;
                case ConnectionStates.STATE_WORKING:
                    connectionState = "Collecting Data";
                    break;
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    connectionState = "Time out waiting for data";
                    if (tgStreamReader != null && tgStreamReader.isBTConnected()) {
                        tgStreamReader.stop();
                        tgStreamReader.close();
                    }
                    break;
                case ConnectionStates.STATE_STOPPED:
                    connectionState = "Stopped";
                    break;
                case ConnectionStates.STATE_DISCONNECTED:
                    connectionState = "Disconnected";
                    break;
                case ConnectionStates.STATE_ERROR:
                    connectionState = "Error";
                    break;
                case ConnectionStates.STATE_FAILED:
                    connectionState = "Failed";
                    break;
            }

            // Update status text field
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView connectionStatus = findViewById(R.id.connectionStatus);
                    connectionStatus.setText(connectionState);
                }
            });
        }

        /**
         * Do nothing on a record fail
         * @param flag
         */
        @Override
        public void onRecordFail(int flag) {

        }

        /**
         * Do nothing on a checksum fail
         * @param payload
         * @param length
         * @param checksum
         */
        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {

        }

        /**
         * Handle received data, send a switch command over the socket if criteria are met
         * @param datatype
         * @param data
         * @param obj
         */
        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            ProgressBar pBar = (ProgressBar) findViewById(R.id.progressBar);
            switch (datatype) {
                case MindDataType.CODE_ATTENTION:
                    short attValue[] = {(short)data};
                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_ATT.value, attValue, 1);
                    Log.d(TAG, "attValue: " + attValue[0]);
                    pBar.setProgress(attValue[0]);
                    long curTime = System.currentTimeMillis();
                    if (attValue[0] >= seekState && curTime - lastSwitch > TIMEOUT) {
                        lastSwitch = curTime;
                        new SendSwitch().execute();
                    }
                    break;
                case MindDataType.CODE_MEDITATION:
                    short medValue[] = {(short)data};
                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_MED.value, medValue, 1);
                    break;
                case MindDataType.CODE_POOR_SIGNAL:
                    short pqValue[] = {(short)data};
                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_PQ.value, pqValue, 1);
                    break;
                case MindDataType.CODE_RAW:
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * Creates a socket connection and sends a command
     * @param command command to send
     * @return status of sending the command
     */
    private boolean sendCommand(String command) {
        boolean status = false;
        SocketClient client = createClient();

        try {
            Socket s = client.connect();
            if (client.sendRequest(s, command)) {
                status = client.recvResponse(s);
            }
        } catch (Exception e) {
            Log.d(TAG, "Could not send data: " + command);
        }
        return status;
    }

    /**
     * AsyncTask to send the switch command to the raspberry pi and show a toast showing result
     */
    private class SendSwitch extends AsyncTask<String, Boolean, Boolean> {
        protected Boolean doInBackground(String... args) {
            return sendCommand(SocketClient.SWITCH);
        }

        protected void onPostExecute(Boolean result) {
            Toast toast;
            String toastString = "Switch command unsuccessful";
            if (result) {
                toastString = "Switch command successful";
            }
            toast = Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /**
     * AsyncTask to check on the raspberry pi and update the TextView
     */
    private class SendCheck extends AsyncTask<String, Boolean, Boolean> {
        protected Boolean doInBackground(String... args) {
            return sendCommand(SocketClient.CHECK);
        }

        protected void onPostExecute(Boolean result) {
            TextView rPiConnect = findViewById(R.id.rPiConnection);
            String status = "Disconnected";
            if (result) {
                status = "Connected";
            }
            rPiConnect.setText(status);
        }
    }
}
