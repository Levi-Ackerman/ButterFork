package top.lizhengxian.butterfork.sample;

import android.widget.Button;
import android.widget.TextView;

import top.lizhengxian.butterfork.annotation.BindLayout;
import top.lizhengxian.butterfork.annotation.BindView;

@BindLayout(R.layout.activity_main)
public class MainActivity extends BaseActivity {

    @BindView(R.id.btn)
    protected Button mBtn;

    @BindView(R.id.text)
    protected TextView mTextView;

    //试着change一下text，看看views是不是真的实例化成功了
    @Override
    protected void onStart() {
        super.onStart();
        mBtn.setText("changed");
        mTextView.setText("changed too");
    }
}
