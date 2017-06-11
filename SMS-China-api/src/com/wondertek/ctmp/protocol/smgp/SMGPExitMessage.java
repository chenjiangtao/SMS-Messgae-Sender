package com.wondertek.ctmp.protocol.smgp;

import com.wondertek.ctmp.protocol.util.ByteUtil;


public class SMGPExitMessage extends SMGPBaseMessage {

	public SMGPExitMessage() {
		this.commandId = SMGPConstants.SMGP_EXIT_TEST;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("SMGPExitMessage:[sequenceNumber=")
				.append(sequenceString()).append("]");
		return buffer.toString();
	}
}