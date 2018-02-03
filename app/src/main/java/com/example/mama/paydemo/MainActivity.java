package com.example.mama.paydemo;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //handler标志code
    private static final int SDK_PAY_FLAG = 0xf1;
    private TextView tv_text;
    private Button btn_pay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_pay = (Button) findViewById(R.id.btn_pay);
        tv_text = (TextView) findViewById(R.id.tv_text);

        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPayData();
            }
        });
    }
    //获取服务端参数
    public void getPayData(){
        String url = "yoururl?id=1&total=0.01";
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(getApplicationContext(),"获取数据失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("response","response"+response.toString());
                //此句如果打印日志，response.body().string()的值只能获取一次
//                Log.d("response","response"+response.body().string());
                jieXi(response.body().string());
            }
        });
    }

    //解析参数
    private void jieXi(String response) {
        try {
            JSONObject object = new JSONObject(response);
            JSONObject object2 = new JSONObject(object.getString("data"));
            payzhifu(object2.getString("info"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //开始执行支付
    private void payzhifu(String info) {
        final String orderInfo = info;   // 订单信息
        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                PayTask alipay = new PayTask(MainActivity.this);
                //返回参数是map类型，官方实例是string 有错误
                Map<String,String> result = alipay.payV2(orderInfo,true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }
    //执行结果回掉
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SDK_PAY_FLAG:
                    //获取返回的参数
                    Map<String ,String> map = (Map<String,String>) msg.obj;
                    tv_text.setText(map.toString());
                    Toast.makeText(MainActivity.this, "::::::",Toast.LENGTH_LONG).show();
                    break;
            }

        }
    };
}
