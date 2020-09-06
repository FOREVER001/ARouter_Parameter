package com.zxh.arouter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.zxh.arouter.apt.ARouter$$Group$$order;
import com.zxh.arouter.apt.ARouter$$Path$$order;
import com.zxh.arouter_annotation.ARouter;
import com.zxh.arouter_annotation.Parameter;
import com.zxh.arouter_annotation.model.RouteBean;
import com.zxh.arouter_api.ARouterLoadPath;

import java.util.Map;

@ARouter(path = "/app/MainActivity",group = "app")
public class MainActivity extends AppCompatActivity {
    @Parameter(name = "fialname")
    String name;
    @Parameter
    int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jump(View view) {
        ARouter$$Group$$order aRouter$$Group$$order = new ARouter$$Group$$order();
        Map<String, Class<? extends ARouterLoadPath>> map = aRouter$$Group$$order.loadGroup();
        Class<? extends ARouterLoadPath> clazz = map.get("order");
        try {
            ARouter$$Path$$order path$$order = (ARouter$$Path$$order) clazz.newInstance();
            Map<String, RouteBean> map1 = path$$order.loadPath();
            RouteBean routeBean = map1.get("/order/OrderActivity");
            Intent intent = new Intent(this, routeBean.getClazz());
            intent.putExtra("compamy","BAT");
            intent.putExtra("city","上海");
            intent.putExtra("num",100);
            startActivity(intent);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}
