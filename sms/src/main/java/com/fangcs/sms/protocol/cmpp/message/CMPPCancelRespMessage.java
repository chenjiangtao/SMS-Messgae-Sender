package com.fangcs.sms.protocol.cmpp.message;

import com.fangcs.sms.protocol.cmpp.util.ByteUtil;


public class CMPPCancelRespMessage extends CMPPBaseMessage {

    private int successId;                 //成功标识 0：成功 1：失败

    public CMPPCancelRespMessage() {
        super(CMPPConstants.CMPP_CANCEL_RESP, 1);
    }

    public int getSuccessId() {
        return successId;
    }

    public void setSuccessId(int successId) {
        this.successId = successId;
    }

    @Override
    protected void setBody(byte[] bodyBytes) throws Exception {
        int offset = 0;
        successId = ByteUtil.byteToInt(bodyBytes[offset]);
        offset += 1;
        super.setBody(bodyBytes);
    }

    @Override
    protected byte[] getBody() throws Exception {
        byte[] bodyBytes = new byte[getCommandLength()];
        int offset = 0;
        bodyBytes[offset] = ByteUtil.intToByte(successId);
        offset += 1;
        return bodyBytes;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CMPPCancelRespMessage:[sequenceId="+ sequenceString() +",");
        sb.append("successId=" + successId + "]");
        return sb.toString();
    }
}