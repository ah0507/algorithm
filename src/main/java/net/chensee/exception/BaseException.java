package net.chensee.exception;

public class BaseException extends Exception {

    protected String message;

    public BaseException(String message){
        this.message = message;
    }

    @Override
    public String getMessage(){
        return message;
    }
}
