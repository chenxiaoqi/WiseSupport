package com.lazyman.homework.webcrawler.porteasy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReqContext {

    private Map<String, String> hiddenFields = new HashMap<>();

    private String version;

    private int total;

    public ReqContext(int total) {
        this.total = total;
        version = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    public void addHiddenFiled(String name, String value) {
        hiddenFields.put(name, value);
    }

    public Map<String, String> getHiddenFields() {
        return hiddenFields;
    }

    public int getTotal() {
        return total;
    }

    public String getVersion() {
        return version;
    }
}
