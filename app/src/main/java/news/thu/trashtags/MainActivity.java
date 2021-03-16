package news.thu.trashtags;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    TextView textview;
    Button btFind;
    Timer timer;

    UsbSerialPort port;
    TagBuffer tagBuffer;

    MainActivity mainActivity;

    protected  boolean openPort() {
        if (port != null) {
            if (port.isOpen()) {
                return true;
            }
        }

        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return false;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            return false;
        }

        port = driver.getPorts().get(0); // Most devices have just one port (port 0)

        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            if(textview!=null)
                textview.append("serial opened\n");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected Tag[] find(){
        if(!openPort())
            return new Tag[0];
        byte[] cmd = new byte[]{(byte) 0xBB, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x00, (byte) 0x22, (byte) 0x7E};
        try {
            int sent = port.write(cmd, 20);
            byte[] response = new byte[5000];
            int len = port.read(response, 20);
            Tag[] tags = R2000.bytesToTags(response, len);
//            textview.append("Sent " + sent + " bytes. ");
//            textview.append("Recv " + len + " bytes.\n");
//            textview.append(tags.length + "Tags.\n");
            return tags;
        } catch (IOException e) {
            textview.append(e.getMessage());
            e.printStackTrace();
        }
        return new Tag[0];
    }

    View.OnClickListener btFindOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(btFind.getText().equals("FIND")){
                TimerTask timerTask = new TimerTask() { //创建定时触发后要执行的逻辑任务
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textview.setText("");

                                Tag[] tags = find();

//                                Tag[] tags = tagBuffer.update(new Tag[0]);
                                for (Tag tag : tags) {
                                    textview.append(tag.toString() + "\n");
                                }

                                LinearLayout barsLo = findViewById(R.id.barsLo);
                                barsLo.removeAllViewsInLayout();

                                ProgressBar[] bars = new ProgressBar[tags.length];
                                TextView[] texts = new TextView[tags.length];

                                for(int i=0; i<tags.length;i++){
                                    texts[i] = new TextView(getApplicationContext());
                                    texts[i].setText(tags[i].toString());
                                    texts[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                    texts[i].setTextColor(0xffffffff);


                                    bars[i] = new ProgressBar(getApplicationContext(),null,android.R.attr.progressBarStyleHorizontal);
                                    bars[i].setProgress(tags[i].power);

                                    barsLo.addView(texts[i]);
                                    barsLo.addView(bars[i]);
                                }


                            }
                        });
                    }
                };
                timer = new Timer();
                timer.scheduleAtFixedRate(timerTask, 100, 100); //启动定时任务
                btFind.setText("STOP");
            }else {
                timer.cancel();
                btFind.setText("FIND");
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textview = findViewById(R.id.textView);
        btFind = findViewById(R.id.btFind);

        ((Button)findViewById(R.id.btManage)).setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(MainActivity.this, ManageActivity.class));}
                }
        );

        btFind.setOnClickListener(btFindOnClickListener);

        tagBuffer = new TagBuffer();


    }
}