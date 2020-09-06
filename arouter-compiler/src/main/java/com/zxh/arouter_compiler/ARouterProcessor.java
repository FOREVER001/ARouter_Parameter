package com.zxh.arouter_compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.zxh.arouter_annotation.ARouter;
import com.zxh.arouter_annotation.model.RouteBean;
import com.zxh.arouter_compiler.constants.ArouterConstants;
import com.zxh.arouter_compiler.utils.EmptyUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({ArouterConstants.AROUTER_ANNOTATION})
// 注解处理器接收的参数
@SupportedOptions({ArouterConstants.MODULE_NAME, ArouterConstants.APT_PACKAGE})
public class ARouterProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Types typeUtils;
    private Filer mFiler;
    private Messager mMessager;

    // 子模块名，如：app/order/personal。需要拼接类名时用到（必传）ARouter$$Group$$order
    private String moduleName;

    // 包名，用于存放APT生成的类文件
    private String packageNameForAPT;

    private Map<String, List<RouteBean>> tempPathMap = new HashMap<>();
    private Map<String, String> tempGroupMap = new HashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        Map<String, String> options = processingEnvironment.getOptions();
        if (!EmptyUtils.isEmpty(options)) {
            moduleName = options.get(ArouterConstants.MODULE_NAME);
            packageNameForAPT = options.get(ArouterConstants.APT_PACKAGE);
            // 有坑：Diagnostic.Kind.ERROR，异常会自动结束，不像安卓中Log.e
            mMessager.printMessage(Diagnostic.Kind.NOTE, "moduleName >>> " + moduleName);
            mMessager.printMessage(Diagnostic.Kind.NOTE, "packageNameForAPT >>> " + packageNameForAPT);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!set.isEmpty()) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
            if (!elements.isEmpty()) {
                try {
                    parseElements(elements);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 解析所有被@ARouter注解的类元素集合
     *
     * @param elements
     */
    private void parseElements(Set<? extends Element> elements) {
        TypeElement activityTypeElement = elementUtils.getTypeElement(ArouterConstants.ANDROID_ACTIVITY);
        TypeMirror activityTypeMirror = activityTypeElement.asType();
        mMessager.printMessage(Diagnostic.Kind.NOTE, "Activity--TypeMirror：" + activityTypeMirror.toString());
        //遍历几点
        for (Element element : elements) {
            //获取每个元素的类信息，用于比较
            TypeMirror elementMirror = element.asType();
            // TODO: 2020-09-01 elementMirror.toString()结果如下：
            // TODO 遍历元素信息：com.zxh.arouter.Main2Activity
            // TODO 遍历元素信息：com.zxh.arouter.MainActivity
            mMessager.printMessage(Diagnostic.Kind.NOTE, "遍历元素信息：" + elementMirror.toString());
            //获取类上面注解的值
            ARouter arouter = element.getAnnotation(ARouter.class);
            RouteBean routeBean = new RouteBean.Builder()
                    .setElement(element)
                    .setGroup(arouter.group())
                    .setPath(arouter.path())
                    .build();
            // 高级判断：ARouter注解仅能用在类之上，并且是规定的Activity
            // 类型工具类方法isSubtype，相当于instance一样

            boolean subtype = typeUtils.isSubtype(elementMirror, activityTypeMirror);
            mMessager.printMessage(Diagnostic.Kind.NOTE, "==subtype=====" + subtype);
            if (subtype) {
                routeBean.setType(RouteBean.Type.ACTIVITY);
            } else {
                // 不匹配抛出异常，这里谨慎使用！考虑维护问题
                throw new RuntimeException("@ARouter目前只能使用在Activity上");
            }
            //赋值临时map存储,用来存放路由组Group对应的详细Path类对象
            valueOfPathMap(routeBean);
        }
        //第一步：生成路由组Group对应详细path类文件，如ARouter$$Path$$app
        createPathFile();
       //第二步，生成路由组Group文件，如ARouter$$Group$$app
        createGroupFile();
    }

    private void createGroupFile() {
        String finalClass=ArouterConstants.AROUTER_GEN_SUFFIX+moduleName;
        TypeElement loadGroupType = elementUtils.getTypeElement(ArouterConstants.LOAD_GROUP);
        TypeElement loadPathType = elementUtils.getTypeElement(ArouterConstants.LOAD_PATH);
        ParameterizedTypeName methodReturn = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.get(loadPathType)))
        );

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("loadGroup")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(methodReturn);

//        Map<String, Class<? extends ARouterLoadPath>> map=new HashMap<>();
        methodBuilder.addStatement("$T<$T,Class<? extends $T>> $N = new $T<>()"
                ,ClassName.get(Map.class)
        ,ClassName.get(String.class)
        ,ClassName.get(loadPathType)
        ,ArouterConstants.LOAD_GROUP_MAP,
                ClassName.get(HashMap.class));
        for (Map.Entry<String, String> entry : tempGroupMap.entrySet()) {
//            map.put("app",ARouter$$Path$$App.class);
            methodBuilder.addStatement("$N.put($S,$T.class);"
                    ,ArouterConstants.LOAD_GROUP_MAP
                    ,entry.getKey(),
                    // 类文件在指定包名下
                    ClassName.get(packageNameForAPT,entry.getValue()));
            mMessager.printMessage(Diagnostic.Kind.NOTE, "====tempGroupMap====" +entry.getKey());


        }



        methodBuilder.addStatement("return $N",ArouterConstants.LOAD_GROUP_MAP);

        TypeSpec typeSpec = TypeSpec.classBuilder(finalClass)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(loadGroupType))
                .addMethod(methodBuilder.build())
                .build();

        JavaFile javaFile = JavaFile.builder(packageNameForAPT, typeSpec)
                .build();
        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createPathFile() {
        TypeElement loadPathType = elementUtils.getTypeElement(ArouterConstants.LOAD_PATH);
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class), // Map
                ClassName.get(String.class), // Map<String,
                ClassName.get(RouteBean.class) // Map<String, RouterBean>
        );
        mMessager.printMessage(Diagnostic.Kind.NOTE, "====methodReturns====" + methodReturns.toString());
        // 遍历分组，每一个分组创建一个路径类文件，如：ARouter$$Path$$app
        for (Map.Entry<String, List<RouteBean>> entry : tempPathMap.entrySet()) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, "==createPathFile=====" + entry.getKey());

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("loadPath")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .returns(methodReturns);
//             Map<String ,RouteBean> map=new HashMap<>();
            methodBuilder.addStatement("$T<$T,$T> $N=new $T<>()"
                    , ClassName.get(Map.class)
                    , ClassName.get(String.class)
                    , ClassName.get(RouteBean.class)
                    , ArouterConstants.LOAD_PATH_MAP,
                    ClassName.get(HashMap.class));



            List<RouteBean> routeBeanList = entry.getValue();
            mMessager.printMessage(Diagnostic.Kind.NOTE, "====routeBeanList====" + routeBeanList.size());

            for (RouteBean routeBean : routeBeanList) {
                mMessager.printMessage(Diagnostic.Kind.NOTE, "====path==" + routeBean.getPath());
                methodBuilder.addStatement("$N.put($S,$T.create($T.$L,$T.class,$S,$S))"
                        ,ArouterConstants.LOAD_PATH_MAP
                        , routeBean.getPath()
                        , ClassName.get(RouteBean.class),
                        ClassName.get(RouteBean.Type.class)
                        , routeBean.getType()
                        , ClassName.get((TypeElement) routeBean.getElement())
                        , routeBean.getPath()
                        , routeBean.getGroup()
                );
            }

            methodBuilder.addStatement("return $N", ArouterConstants.LOAD_PATH_MAP);

            MethodSpec methodSpec = methodBuilder.build();
            String key = entry.getKey();
            String finalClass = "ARouter$$Path$$" + key;
            TypeSpec typeSpec = TypeSpec.classBuilder(finalClass)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ClassName.get(loadPathType))
                    .addMethod(methodSpec)
                    .build();

            JavaFile javaFile = JavaFile.builder(packageNameForAPT, typeSpec)
                    .build();

            try {
                javaFile.writeTo(mFiler);
                mMessager.printMessage(Diagnostic.Kind.NOTE, "==createPathFile===111111==");
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 非常重要一步！！！！！路径文件生成出来了，才能赋值路由组tempGroupMap
            tempGroupMap.put(entry.getKey(), finalClass);
            mMessager.printMessage(Diagnostic.Kind.NOTE, "==tempGroupMap==size==="+tempGroupMap.size());
        }

    }

    /**
     * 赋值临时map存储，用来存放路由组Group对应的详细Path类对象，
     * 生成路由路径类文件时遍历，
     *
     * @param routeBean
     */
    private void valueOfPathMap(RouteBean routeBean) {
        if (checkRoutePath()) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, "RouterBean >>> " + routeBean.toString());
            //开始赋值遍历
            List<RouteBean> routeBeans = tempPathMap.get(routeBean.getGroup());
            //如果从Map中找不到key:bean.getGroup的数据，就新建List集合在添加进map中
            if (EmptyUtils.isEmpty(routeBeans)) {
                routeBeans = new ArrayList<>();
                routeBeans.add(routeBean);
                tempPathMap.put(routeBean.getGroup(), routeBeans);
            } else {//找到了key，直接加入List集合
                routeBeans.add(routeBean);
            }
        } else {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
        }
    }

    // TODO: 2020-09-01 检查路由路径地址
    private boolean checkRoutePath() {
        return true;
    }


}
