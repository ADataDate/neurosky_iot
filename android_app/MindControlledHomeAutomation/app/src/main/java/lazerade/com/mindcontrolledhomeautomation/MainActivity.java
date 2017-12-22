package lazerade.com.mindcontrolledhomeautomation;

import android.bluetooth.BluetoothAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "Mindful";
    private NskAlgoSdk nskAlgoSdk;
    private TgStreamReader tgStreamReader;
    private BluetoothAdapter mBluetoothAdapter;
    private String connectionState;
    private int seekState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nskAlgoSdk = new NskAlgoSdk();
        Button headSet = (Button) findViewById(R.id.headSet);
        try {
            // (1) Make sure that the device supports Bluetooth and Bluetooth is on
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Toast.makeText(
                        this,
                        "Please enable your Bluetooth and re-run this program !",
                        Toast.LENGTH_LONG).show();
                //finish();
            } else {
                headSet.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.getMessage());
            return;
        }

        headSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Example of constructor public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
                tgStreamReader = new TgStreamReader(mBluetoothAdapter,callback);

                if(tgStreamReader != null && tgStreamReader.isBTConnected()){

                    // Prepare for connecting
                    tgStreamReader.stop();
                    tgStreamReader.close();
                }

                tgStreamReader.connect();
            }
        });

        SeekBar sBar = findViewById(R.id.settingBar);
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

    private TgStreamHandler callback = new TgStreamHandler() {

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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView connectionStatus = findViewById(R.id.connectionStatus);
                    connectionStatus.setText(connectionState);
                }
            });
        }

        @Override
        public void onRecordFail(int flag) {
            // You can handle the record error message here
            Log.e(TAG,"onRecordFail: " +flag);

        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            // You can handle the bad packets here.
        }

        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            ProgressBar pBar = (ProgressBar) findViewById(R.id.progressBar);
            switch (datatype) {
                case MindDataType.CODE_ATTENTION:
                    short attValue[] = {(short)data};
                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_ATT.value, attValue, 1);
                    Log.d(TAG, "attValue: " + attValue[0]);
                    pBar.setProgress(attValue[0]);
                    if (attValue[0] >= seekState) {
                        Log.d(TAG, "Concentrated hard enough");
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
}
