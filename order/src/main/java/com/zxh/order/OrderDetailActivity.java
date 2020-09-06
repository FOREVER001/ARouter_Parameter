package com.zxh.order;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.zxh.arouter_annotation.ARouter;

@ARouter(path = "/order/OrderDetailActivity",group = "order")
public class OrderDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
    }
}
