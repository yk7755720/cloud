package com.enity;

/**
 * @Auther: mapengfei
 * @Date: 2018/5/19
 * @Description:
 */

public class FileResult {
    private String path;
    private String name;
    private String size;
    public String getPath() {
        return path;
    }

    public FileResult setPath(String path) {
        this.path = path;
        return this;
    }

    public String getName() {
        return name;
    }

    public FileResult setName(String name) {
        this.name = name;
        return this;
    }

    public String getSize() {
        return size;
    }

    public FileResult setSize(String size) {
        this.size = size;
        return this;
    }

}
