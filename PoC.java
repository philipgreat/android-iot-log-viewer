import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
 
import com.terapico.hacontrol.protocol.HAReturnValue;
 
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
 
public class FindServiceURLActivity extends Activity {
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView t = new TextView(this);
 
        t = (TextView) findViewById(R.id.CommonText);
 
        try {
            System.out.println();
            t.setText(getAddressResponseXML(getAddressResponseXML()));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            t.setText(e.getClass()+":"+e.getMessage());
        }
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
     
     
    public NetworkInterface getFirst() throws Exception{
         
        NetworkInterface.getNetworkInterfaces();
        return null;
         
         
    }
    
    public String getAddressResponseXML() throws IOException {
 
        String address = null;
 
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return null;
        }
         if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLED){
             throw new IllegalStateException("Please enable wifi manually!");
         }
         
        if(!wifi.isWifiEnabled()){
            throw new IllegalStateException("Please enable wifi manually!");
        }
        MulticastLock mcLock = wifi.createMulticastLock("mylock");
        mcLock.acquire();//new InetSocketAddress(InetAddress.getByName(group), port);
        InetAddress group = InetAddress.getByName("224.0.0.7");
        //InetSocketAddress groupInetSocketAddress = new InetSocketAddress(InetAddress.getByName("228.5.6.7"), 6789);
        MulticastSocket socket = new MulticastSocket(6789);
        socket.setSoTimeout(2000);
        socket.joinGroup(group);
        //socket.joinGroup(groupInetSocketAddress, NetworkInterface.getByInetAddress(getWifiInetAddress(wifi)));
 
        byte[] buf = new byte[80];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        socket.receive(recv);
 
        byte recvedBytes[] = new byte[recv.getLength()];
        System.arraycopy(buf, 0, recvedBytes, 0, recv.getLength());
        address = new String(recvedBytes);
 
        //socket.leaveGroup(group);
        socket.close();
        mcLock.release();
        return address;
    }
    InetAddress getWifiInetAddress(WifiManager wifi) throws UnknownHostException {
 
        ByteBuffer wifiRawAddress = ByteBuffer.allocate(4);
        wifiRawAddress.order(ByteOrder.LITTLE_ENDIAN).putInt(wifi.getConnectionInfo().getIpAddress());
        return InetAddress.getByAddress(wifiRawAddress.array());
    }
}
