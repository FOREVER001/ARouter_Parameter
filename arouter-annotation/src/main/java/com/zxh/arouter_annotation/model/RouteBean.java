package com.zxh.arouter_annotation.model;

import javax.lang.model.element.Element;

public class RouteBean {
    public enum Type {
        ACTIVITY
    }
    private Type mType;
    private Element mElement;
    private Class<?> mClazz;
    private String path;
    private String group;

    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
        mType = type;
    }

    public Element getElement() {
        return mElement;
    }

    public void setElement(Element element) {
        mElement = element;
    }

    public Class<?> getClazz() {
        return mClazz;
    }

    public void setClass(Class<?> aClass) {
        mClazz= aClass;
    }

    public String getPath() {
        return path == null ? "" : path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGroup() {
        return group == null ? "" : group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    private RouteBean(Type type, Class<?> aClass, String path, String group) {
        mType = type;
        mClazz= aClass;
        this.path = path;
        this.group = group;
    }

    public static RouteBean create(Type type, Class<?> aClass, String path, String group){
        return new RouteBean(type,aClass,path,group);
    }

    private RouteBean (Builder builder){
        this.mType=builder.mType;
        this.mElement=builder.mElement;
        this.group=builder.group;
        this.path=builder.path;
        this.mClazz=builder.mClazz;
    }
   public static  class Builder{
        private Type mType;
        private Element mElement;
        private Class mClazz;
        private String path;
        private String group;

        public Builder setType(Type type) {
            mType = type;
            return  this;
        }

        public Builder setElement(Element element) {
            mElement = element;
            return  this;
        }

        public Builder setClass(Class<?> aClass) {
            mClazz= aClass;
            return  this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return  this;
        }

        public Builder setGroup(String group) {
            this.group = group;
            return  this;
        }

        public RouteBean build(){
            if(path==null || path.length()==0){
                throw new IllegalArgumentException("请设置path,如：/app/MainActivity");
            }
           return new RouteBean(this);
        }
    }

    @Override
    public String toString() {
        return "RouteBean{" +
                "mElement=" + mElement +
                ", path='" + path + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
