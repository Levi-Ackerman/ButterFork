package top.lizhengxian.butterfork.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;


import top.lizhengxian.butterfork.ButterFork;
import top.lizhengxian.butterfork.annotation.BindView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn)
    protected Button mBtn;

    @BindView(R.id.text)
    protected TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterFork.bind(this);
        mBtn.setText("changed");
        mTextView.setText("changed too");
    }
}
