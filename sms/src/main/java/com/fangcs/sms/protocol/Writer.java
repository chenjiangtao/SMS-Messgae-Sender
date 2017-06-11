package com.fangcs.sms.protocol;

import java.io.IOException;


public interface Writer {
    void write(Message message) throws IOException;
}
