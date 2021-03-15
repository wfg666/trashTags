package news.thu.trashtags;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    TextView textview;
    UsbManager manager;
    List<UsbSerialDriver> availableDrivers;
    UsbSerialDriver driver;
    UsbSerialPort port;

    Timer timer;

    protected int find(){
        byte[] cmd = new byte[]{(byte) 0xBB, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x00, (byte) 0x22, (byte) 0x7E};
        try {
            int sent = port.write(cmd, 20);
            textview.append("Sent " + sent + " bytes.\n");
            byte[] response = new byte[1000];
            int len = port.read(response, 20);
            textview.append("Received " + len + " bytes.\n");
            if(len<24)
                return 0;
            else{
                int rssi = response[5];
                textview.append("rssi = " + rssi + "\n");
                rssi = (rssi+60) * 2;
                if(rssi<0)
                    rssi=0;
                if(rssi>100)
                    rssi=100;
                return rssi;
            }
        } catch (IOException e) {
            textview.append(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textview = findViewById(R.id.textView);

        Button bt_connect = findViewById(R.id.bt_connect);
        Button bt_find = findViewById(R.id.bt_find);

        bt_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Find all available drivers from attached devices.
                manager = (UsbManager) getSystemService(Context.USB_SERVICE);
                availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
                if (availableDrivers.isEmpty()) {
                    return;
                }

                // Open a connection to the first available driver.
                driver = availableDrivers.get(0);
                UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
                if (connection == null) {
                    // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
                    return;
                }

                port = driver.getPorts().get(0); // Most devices have just one port (port 0)

                try {
                    port.open(connection);
                    port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                    textview.append("serial opened\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        TimerTask timerTask = new TimerTask() { //创建定时触发后要执行的逻辑任务
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textview.setText("");
                        int barValue = find();
                        ProgressBar bar =  findViewById(R.id.progressBar);
                        bar.setProgress(barValue);
                        textview.append("bar = " + barValue + "\n");
//                        textview.append("timer\n");
                    }
                });
            }
        };

        bt_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(timerTask, 100, 100); //启动定时任务
            }
        });

    }
}