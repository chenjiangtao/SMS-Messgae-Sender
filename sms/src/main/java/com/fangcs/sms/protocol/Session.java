package com.fangcs.sms.protocol;

import java.io.IOException;


public interface Session extends java.io.Closeable {
    String getSessionId();
    boolean isAuthenticated();
    boolean authenticate();
    void heartbeat();
    void submit(String content, String spNumber, String userNumber);
    void send(Message message);
    void process(Message message) throws IOException;
}
