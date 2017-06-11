package com.fangcs.sms.protocol.cmpp;

import com.fangcs.sms.protocol.Message;
import com.fangcs.sms.protocol.Writer;
import com.fangcs.sms.protocol.cmpp.message.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CMPPWriter implements Writer {

    protected DataOutputStream out;

    public CMPPWriter(OutputStream os) {
        this.out = new DataOutputStream(os);
    }

   
    public void write(Message message) throws IOException {
        if(message instanceof CMPPBaseMessage){
            byte[] bytes = null;
            try {
                bytes = ((CMPPBaseMessage) message).toBytes();
            } catch (Exception ex){ }
            if(bytes != null){
                writeBytes(bytes);
            }
        }
    }

    private void writeBytes(byte[] bytes) throws IOException {
        out.write(bytes);
        out.flush();
    }
}
