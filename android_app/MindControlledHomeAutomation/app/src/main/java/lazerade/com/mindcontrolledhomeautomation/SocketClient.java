package lazerade.com.mindcontrolledhomeautomation;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketClient {
    private String mAddress;

    public SocketClient(String address) {
        mAddress = address;
    }

    public boolean sendString(String data) {
        boolean status = false;
        Thread send = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(MainActivity.TAG, "Attempting connection");
                    InetAddress inetAddress = Inet4Address.getByName(mAddress);
                    InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, 12345);

                    Socket s = new Socket();
                    s.connect(socketAddress);
                    Log.d(MainActivity.TAG, "Connected");
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                    bw.write("test\n");
                    bw.flush();
                    s.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(MainActivity.TAG, "Could not connect to address: " + mAddress);
                }
            }
        });
        send.start();
        return status;
    }
}
