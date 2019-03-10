package com.wisesupport.test.spring;

/**
 * @author c00286900
 */
public class ClientService {

    private TestBean testBean;

    public TestBean getTestBean() {
        return testBean;
    }

    public void setTestBean(TestBean testBean) {
        this.testBean = testBean;
    }
}
