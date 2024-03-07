package com.example.Uncha_Muncha_Bot.service;

import com.example.Uncha_Muncha_Bot.enums.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ResourceBundleService {
    @Autowired
    private ResourceBundleMessageSource resourceBundleMessageSource;

    public String getMessage(String code, Language language) {
        return resourceBundleMessageSource.getMessage(code, null, new Locale(language.name()));
    }
}
