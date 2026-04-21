package com.sabih.url_shortner.exception;

public class UrlNotFoundException extends RuntimeException{
    public UrlNotFoundException(String shortCode){
        super("Short code not found: "+shortCode);
    }
}
