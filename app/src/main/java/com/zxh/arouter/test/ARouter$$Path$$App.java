package com.zxh.arouter.test;

import com.zxh.arouter.Main2Activity;
import com.zxh.arouter.MainActivity;
import com.zxh.arouter_annotation.model.RouteBean;
import com.zxh.arouter_api.ARouterLoadPath;

import java.util.HashMap;
import java.util.Map;

public class ARouter$$Path$$App implements ARouterLoadPath {
    @Override
    public Map<String, RouteBean> loadPath() {
        Map<String ,RouteBean> map=new HashMap<>();

        map.put("/app/MainActivity",
                RouteBean.create(RouteBean.Type.ACTIVITY
                        ,MainActivity.class
                        ,"/app/MainActivity","app"));
        map.put("/app/Main2Activity",
                RouteBean.create(RouteBean.Type.ACTIVITY
                        , Main2Activity.class
                        ,"/app/Main2Activity","app"));

        return map;
    }
}
