package top.lizhengxian.apt_sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.example.BindActivity;
import com.example.BindView;

@BindActivity
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn)
    private Button mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}
