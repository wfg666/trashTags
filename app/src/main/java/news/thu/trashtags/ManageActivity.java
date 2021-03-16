package news.thu.trashtags;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ManageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

//        File file = new File("TagData.json");
        File file = new File(getApplicationContext().getExternalFilesDir("data"), "TagData.json");
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write("6666666666666666".getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}