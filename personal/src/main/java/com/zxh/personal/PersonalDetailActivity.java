package com.zxh.personal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.zxh.arouter_annotation.ARouter;
import com.zxh.arouter_annotation.Parameter;

@ARouter(path = "/personal/PersonalDetailActivity",group = "personal")
public class PersonalDetailActivity extends AppCompatActivity {
    @Parameter(name = "conpanyAddress")
    String conpanyAddress;
    @Parameter
    int code;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_detail);
    }
}
