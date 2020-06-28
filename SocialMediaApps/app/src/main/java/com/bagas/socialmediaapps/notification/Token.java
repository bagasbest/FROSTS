package com.bagas.socialmediaapps.notification;

public class Token {
    //an ID issued by the  GCMconnection servers to the client app that allows it ti receive messages

    String  token;

    public Token(String token) {
        this.token = token;
    }

    public Token() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
