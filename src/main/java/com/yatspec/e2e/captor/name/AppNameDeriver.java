package com.yatspec.e2e.captor.name;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppNameDeriver {

    @Value("${info.app.name}")
    private String appName;

    public String derive()  {
        return appName.replaceAll(" ", "");
    }
}