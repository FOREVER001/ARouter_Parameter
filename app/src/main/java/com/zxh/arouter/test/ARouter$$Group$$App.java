package com.zxh.arouter.test;

import com.zxh.arouter_api.ARouterLoadGroup;
import com.zxh.arouter_api.ARouterLoadPath;

import java.util.HashMap;
import java.util.Map;

public class ARouter$$Group$$App implements ARouterLoadGroup {
    @Override
    public Map<String, Class<? extends ARouterLoadPath>> loadGroup() {
        Map<String, Class<? extends ARouterLoadPath>> map=new HashMap<>();
        map.put("app",ARouter$$Path$$App.class);
        return map;
    }
}
