package com.zxh.arouter;

import com.zxh.arouter.MainActivity;
import com.zxh.arouter_api.ParameterLoad;

public class XActivity$$ParameterLoad implements ParameterLoad {
    @Override
    public void loadParameter(Object target) {
        MainActivity mainActivity= (MainActivity) target;
        mainActivity.name=mainActivity.getIntent().getStringExtra("name");
        mainActivity.age=mainActivity.getIntent().getIntExtra("age",0);
    }
}
