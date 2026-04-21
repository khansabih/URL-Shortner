package com.sabih.url_shortner.service;

import org.springframework.stereotype.Component;

@Component
public class ShortCodeGenerator {
    private static final String ALPHABETS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABETS.length();

    public String encode(long id){
        if(id == 0)
            return String.valueOf(ALPHABETS.charAt(0));

        StringBuilder sb = new StringBuilder();
        while(id > 0){
            sb.append(ALPHABETS.charAt((int)(id % BASE)));
            id /= BASE;
        }

        return sb.reverse().toString();
    }
}
