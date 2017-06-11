package test;

import java.io.UnsupportedEncodingException;

import com.wondertek.ctmp.protocol.smgp.SMGPApi;
import com.wondertek.ctmp.protocol.smgp.SMGPSubmitMessage;
import com.wondertek.ctmp.protocol.util.SequenceGenerator;

public class SMGPApiTest {

	public static void main(String[] args) {

		SMGPApi mtApi = new SMGPApi();
		mtApi.setHost("127.0.0.1");
		mtApi.setPort(8900);
		mtApi.setClientId("02100000");
		mtApi.setPassword("1");
		mtApi.setVersion((byte) 0);
		mtApi.setLoginMode(SMGPApi.MT_MO);

		SMGPSession session = new SMGPSession();
		session.api = mtApi;

		new Thread(session).start();

		SMGPSubmitMessage submit = new SMGPSubmitMessage();
		submit.setSrcTermId("1065902100000");
		submit.setDestTermIdArray(new String[] { "13585844052" });
		submit.setMsgFmt((byte) 8);

		String content = "smgp测试test";

		byte[] bContent = null;

		try {
			bContent = content.getBytes("iso-10646-ucs-2");
		} catch (UnsupportedEncodingException e) {}

		if (bContent.length <= 140) {
			submit.setBMsgContent(bContent);
			submit.setMsgFmt((byte) 8);
			submit.setNeedReport((byte) 1);
			submit.setServiceId("");
			submit.setSequenceNumber(SequenceGenerator.nextSequence());
			submit.setAtTime("");
			submit.setNeedReport((byte) 1);

			session.send(submit);
		}

	}

}
