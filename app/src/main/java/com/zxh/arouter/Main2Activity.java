package com.zxh.arouter;

import android.os.Bundle;

import com.zxh.arouter_annotation.ARouter;
import com.zxh.arouter_annotation.Parameter;

import androidx.appcompat.app.AppCompatActivity;

@ARouter(path = "/app/Main2Activity",group = "app")
public class Main2Activity extends AppCompatActivity {
    @Parameter(name = "name")
    String name;
    @Parameter
    int age;
    @Parameter
    boolean isOk;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
