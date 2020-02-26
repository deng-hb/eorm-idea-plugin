package com.denghb.eorm.generator.model;

public class PackagePathModel {

    private String name;

    private String path;

    public PackagePathModel(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    // 显示
    @Override
    public String toString() {
        return name;
    }
}
