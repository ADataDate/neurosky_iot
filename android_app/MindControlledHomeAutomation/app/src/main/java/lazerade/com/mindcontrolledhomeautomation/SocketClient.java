package lazerade.com.mindcontrolledhomeautomation;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Class for socket connection
 */
public class SocketClient {
    private static final String TAG = "SocketClient";
    public static final String SWITCH = "switch";
    public static final String CHECK = "cstate";
    private static final String OK = "ok";
    private String mIpAddress = null;

    /**
     * Constructor
     * @param ipAddress used to connect to raspberry pi
     */
    public SocketClient(String ipAddress) {
        mIpAddress = ipAddress;
    }

    /**
     * Create a connect and return socket object
     * @return
     */
    public Socket connect() {
        Socket s = new Socket();
        try {
            InetAddress inetAddress = Inet4Address.getByName(mIpAddress);
            InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, 12345);
            s.connect(socketAddress);
            Log.d(TAG, "Connected to:" + mIpAddress);
        } catch (Exception e) {
            Log.e(TAG, "Could not connect to: " + mIpAddress);
            e.printStackTrace();
            s = null;
        }
        return s;
    }

    /**
     * Send data over socket
     * @param s socket to send data over
     * @param request data to send
     * @return status of sending data
     */
    public boolean sendRequest(Socket s, String request) {
        boolean status = false;
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            bw.write(request);
            bw.flush();
            status = true;
        } catch (Exception e) {
            Log.e(TAG, "Could not send request");
        }
        return status;
    }

    /**
     * Wait for a response
     * @param s socket to wait for a response
     * @return if response was good
     */
    public boolean recvResponse(Socket s) {
        boolean status = false;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String response = br.readLine();
            if (response.equals(OK)) {
                status = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not receive response");
        }
        return status;
    }
}
