package vip.mtmy.helloworld;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.widget.Toast;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements EventListener {
    protected TextView txtResult;
    protected Button btn_start;
    protected Button btn_stop;

    private EventManager asr;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPermission();
        initView();


        asr = EventManagerFactory.create(MainActivity.this,"wp");//注册自己的输出事件类
        asr.registerListener(this);//// 调用 EventListener 中 onEvent方法
    }
    private void initView() {

        txtResult = (TextView) findViewById(R.id.txtResult);
        btn_start =(Button) findViewById(R.id.btn_start);
        btn_stop = (Button) findViewById(R.id.btn_stop);

        btn_start.setOnClickListener(new View.OnClickListener() {//开始
            @Override
            public void onClick(View v) {
                txtResult.setText("开");
               start();
            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {//停止
            @Override
            public void onClick(View v) {
                txtResult.setText("关");
                stop();
            }
        });
    }

    //自带
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm :permissions){
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.
                Toast.makeText(this,"没有权限",Toast.LENGTH_SHORT).show();
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()){
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    public void start(){
        HashMap params = new HashMap();
        params.put("kws-file", "assets:///WakeUp.bin"); // 设置唤醒资源, 唤醒资源请到 http://yuyin.baidu.com/wake#m4 来评估和导出
        String bt=new JSONObject(params).toString();
        asr.send("wp.start", bt, null, 0, 0);
    }
    public void stop(){
        asr.send("wp.stop", null, null, 0, 0);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {}

    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        txtResult.setText(name);
        try {
            if ("wp.data".equals(name)) {
                JSONObject jsonObject=new JSONObject(params);
                String word=jsonObject.getString("word");
                if (word.equals("陌途陌影")){
                    Toast.makeText(this, "唤醒成功啊", Toast.LENGTH_LONG).show();
                }
                // 识别相关的结果都在这里
                Log.d("唤醒","唤醒成功");
            }
            else{
                Toast.makeText(this,"唤醒失败",Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }




    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 基于SDK集成4.2 发送取消事件
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        // 基于SDK集成5.2 退出事件管理器
        // 必须与registerListener成对出现，否则可能造成内存泄露
        asr.unregisterListener(this);
    }
}
