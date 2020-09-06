package com.zxh.arouter_api;

import java.util.Map;

/**
 * 路由组文件接口
 */
public interface ARouterLoadGroup {
    Map<String,Class<? extends ARouterLoadPath>> loadGroup();
}
