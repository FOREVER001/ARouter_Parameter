package com.zxh.arouter_api;

import com.zxh.arouter_annotation.model.RouteBean;

import java.util.Map;

/**
 * 路由组对应的路由path生成记录接口
 */
public interface ARouterLoadPath {
    Map<String, RouteBean> loadPath();
}
