package com.zxh.arouter_compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import com.zxh.arouter_annotation.Parameter;
import com.zxh.arouter_compiler.constants.ArouterConstants;

import org.omg.CORBA.PRIVATE_MEMBER;

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
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes({ArouterConstants.PARAMETER_ANNOTATION})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ParameterProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Types typeUtils;
    private Messager mMessager;
    private Filer mFiler;
    // 临时map存储，用来存放被@Parameter注解的属性集合，生成类文件时遍历
    // key:类节点, value:被@Parameter注解的属性集合
    private Map<TypeElement, List<Element>> map = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        mMessager = processingEnvironment.getMessager();
        mFiler = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!set.isEmpty()) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Parameter.class);
            //收集element
            praseElement(elements);
            //生成文件
            createFile();
            return true;
        }
        return false;
    }

    private void createFile() {
        for (Map.Entry<TypeElement, List<Element>> entry : map.entrySet()) {
            TypeElement typeElement = entry.getKey();
            String packageName = ClassName.get(typeElement).packageName();
            String activityName = typeElement.getSimpleName().toString();
            mMessager.printMessage(Diagnostic.Kind.NOTE, "====activityName===" + activityName);
            mMessager.printMessage(Diagnostic.Kind.NOTE, "====packageName===" + packageName);
//            public void loadParameter(Object target) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ArouterConstants.PARAMETER_METHOD)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addAnnotation(Override.class)
                    .addParameter(Object.class, "target");
//            MainActivity activity= (MainActivity) target;
            methodBuilder.addStatement("$T $N = ($T) target"
                    , ClassName.get(typeElement)
                    , ArouterConstants.ACTIVITY_STR
                    , ClassName.get(typeElement));
//            mainActivity.name=mainActivity.getIntent().getStringExtra("name");
            List<Element> elementList = entry.getValue();
            for (Element element : elementList) {

                Parameter parameter = element.getAnnotation(Parameter.class);
                String name = parameter.name();
                String attribute;
                if(name!=null && name.length()>0){
                    attribute=name;
                }else {
                    attribute=element.getSimpleName().toString();
                }
                // 遍历注解的属性节点 生成函数体
                TypeMirror typeMirror = element.asType();
                // 获取 TypeKind 枚举类型的序列号
                int type = typeMirror.getKind().ordinal();
                // TypeKind 枚举类型不包含String
                //先只判断3种int,boolean ,String,其他的后期优化可以自行添加
                // 最终拼接的前缀：
                String finalValue = ArouterConstants.ACTIVITY_STR+"." + element.getSimpleName().toString();
                // t.s = t.getIntent().
                String methodContent = finalValue + " = "+ArouterConstants.ACTIVITY_STR+".getIntent().";
                if (type == TypeKind.INT.ordinal()){
                    // t.s = t.getIntent().getIntExtra("age", t.age);
                    methodContent+="getIntExtra($S,"+finalValue+")";
                }else if(type==TypeKind.BOOLEAN.ordinal()){
                    // t.s = t.getIntent().getBooleanExtra("isSuccess", t.age);
                    methodContent+="getBooleanExtra($S,"+finalValue+")";
                }else {
                    // t.s = t.getIntent.getStringExtra("s");
                    if (typeMirror.toString().equalsIgnoreCase(ArouterConstants.STRING)) {
                        methodContent+="getStringExtra($S)";
                    }
                }

                methodBuilder.addStatement(methodContent,attribute);
            }
            TypeElement interfaceTypeElement = elementUtils.getTypeElement(ArouterConstants.PARAMTER_LOAD);
            TypeSpec typeSpec = TypeSpec.classBuilder(activityName + "$$ParameterLoad")
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(methodBuilder.build())
                    .addSuperinterface(ClassName.get(interfaceTypeElement))
                    .build();
//
            JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                    .build();
//
            try {
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 收集element
     * @param elements
     */
    private void praseElement(Set<? extends Element> elements) {
        for (Element element : elements) {
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            if (map.containsKey(typeElement)) {
                map.get(typeElement).add(element);
            } else {
                List<Element> list = new ArrayList<>();
                list.add(element);
                map.put(typeElement, list);
            }
        }

    }
}
