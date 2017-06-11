package com.fangcs.sms.protocol.cmpp.message;



public class CMPPActiveTestMessage extends CMPPBaseMessage {

    public CMPPActiveTestMessage() {
        super(CMPPConstants.CMPP_ACTIVE_TEST, 0);
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CMPPActiveTestMessage:[sequenceId="+sequenceString()+"]");
        return buffer.toString();
    }
}