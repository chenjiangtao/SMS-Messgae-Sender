package com.fangcs.sms.protocol;

import java.io.IOException;

public interface Reader {
    Message read() throws IOException;
}
