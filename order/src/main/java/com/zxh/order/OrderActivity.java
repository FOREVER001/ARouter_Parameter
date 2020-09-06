package com.zxh.order;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zxh.arouter_annotation.ARouter;
import com.zxh.arouter_annotation.Parameter;

@ARouter(path = "/order/OrderActivity",group = "order")
public class OrderActivity extends AppCompatActivity {
    @Parameter(name = "compamy")
    String compamy;
    @Parameter
    String city;
    @Parameter
    int  num;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
       new OrderActivity$$ParameterLoad().loadParameter(this);
        Log.e("====compamy==",compamy);
        Log.e("====city==",city);
        Log.e("====num==",num+"");
    }
   public void jump(View view){
        
   }
}
