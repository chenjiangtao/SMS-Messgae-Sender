package com.wondertek.ctmp.protocol.smgp;

import com.wondertek.ctmp.protocol.util.ByteUtil;


public class SMGPExitRespMessage extends SMGPBaseMessage {

	public SMGPExitRespMessage() {
		this.commandId = SMGPConstants.SMGP_EXIT_RESP;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("SMGPExitRespMessage:[sequenceNumber=").append(
				sequenceString()).append(",");
		buffer.append("]");
		return buffer.toString();
	}
}