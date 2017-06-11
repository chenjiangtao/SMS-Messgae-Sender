package test;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.wondertek.ctmp.protocol.smgp.SMGPActiveTestMessage;
import com.wondertek.ctmp.protocol.smgp.SMGPActiveTestRespMessage;
import com.wondertek.ctmp.protocol.smgp.SMGPApi;
import com.wondertek.ctmp.protocol.smgp.SMGPBaseMessage;
import com.wondertek.ctmp.protocol.smgp.SMGPLoginRespMessage;
import com.wondertek.ctmp.protocol.smgp.SMGPSubmitMessage;

public class SMGPSession implements Runnable {

	protected static final SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");

	protected Logger log = LogManager.getLogger("smgp");

	protected Logger fromLog = LogManager.getLogger("from");

	protected Logger toLog = LogManager.getLogger("to");

	protected SMGPApi api;

	protected boolean allowSend = true;

	protected Object sendLock = new Object();

	private void checkBind() {

		while (!api.isConnected()) {
			try {
				SMGPLoginRespMessage resp = api.connect();
				if (resp != null && resp.getStatus() == 0) {
					log.info("SMGPSession login success host=" + api.getHost() + ",port=" + api.getPort()
							+ ",clientId=" + api.getClientId());
					synchronized (api) {
						api.notifyAll();
					}
					break;
				} else {
					log.error("SMGPSession login fail, host=" + api.getHost() + ",port=" + api.getPort() + ",clientId="
							+ api.getClientId() + ",result=" + resp.getStatus());
				}
			} catch (IOException e) {
				log.error("SMGPSession login error", e);
			}
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {}
		}
	}

	public void send(SMGPSubmitMessage submit) {
		while (!allowSend) {
			synchronized (sendLock) {
				try {
					sendLock.wait();
				} catch (InterruptedException e) {}
			}
		}

		while (!api.isConnected()) {
			synchronized (api) {
				try {
					api.wait();
				} catch (InterruptedException e) {}
			}
		}
		try {
			api.sendMsg(submit);
			toLog.info("将消息成功发送到网关:" + submit);
		} catch (IOException e) {
			api.close();
			toLog.warn("SMGPSession send msg fail,retry :" + submit, e);
			send(submit);
		}

	}

	public void run() {
		Thread.currentThread().setName("SMGPSession");
		checkBind();
		ActiveTestThread activeThread = new ActiveTestThread(api);
		activeThread.start();
		while (true) {
			try {
				SMGPBaseMessage baseMsg = api.receiveMsg();
				if (baseMsg == null) {
					log.error("SMGPSession receive msg null");
					api.close();
					checkBind();
					continue;
				}
				if (baseMsg instanceof SMGPActiveTestMessage) {
					SMGPActiveTestRespMessage resp = new SMGPActiveTestRespMessage();
					resp.setSequenceNumber(baseMsg.getSequenceNumber());
					api.sendMsg(resp);
				} else if (baseMsg instanceof SMGPActiveTestRespMessage) {
					//do nothing;
				} else {
					fromLog.info("从网关收到消息:" + baseMsg);
				}
			} catch (Exception e) {
				log.error("SMGPSession receive msg error:" + e.getMessage());
				api.close();
				checkBind();
			}
		}
	}

}
