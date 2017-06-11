package com.fangcs.sms.protocol.smgp;

import com.fangcs.sms.protocol.Message;
import com.fangcs.sms.protocol.Writer;
import com.fangcs.sms.protocol.smgp.message.SMGPBaseMessage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class SMGPWriter implements Writer {

    protected DataOutputStream out;

    public SMGPWriter(OutputStream os) {
        this.out = new DataOutputStream(os);
    }

  
    public void write(Message message) throws IOException {
       if(message instanceof SMGPBaseMessage){
            byte[] bytes = null;
            try {
                bytes = ((SMGPBaseMessage) message).toBytes();
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
