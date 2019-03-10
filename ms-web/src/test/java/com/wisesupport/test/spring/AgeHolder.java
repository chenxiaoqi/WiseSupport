package com.wisesupport.test.spring;

/**
 * @author c00286900
 */
public interface AgeHolder {

    default int age() {
        return getAge();
    }

    int getAge();

    void setAge(int age);

}