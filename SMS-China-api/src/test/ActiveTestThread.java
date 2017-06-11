package test;

import java.io.IOException;

import com.wondertek.ctmp.protocol.smgp.SMGPActiveTestMessage;
import com.wondertek.ctmp.protocol.smgp.SMGPApi;
import com.wondertek.ctmp.protocol.util.SequenceGenerator;

public class ActiveTestThread extends Thread {

	private SMGPApi api;
	
	
	public ActiveTestThread(SMGPApi api){
		this.setName("ActiveTestThread");
		this.api=api;
	}
	
	public void run(){
		while(true){
			try {
				Thread.sleep(9000);
			} catch (InterruptedException e) {
			}
			SMGPActiveTestMessage active=new SMGPActiveTestMessage();
			active.setSequenceNumber(SequenceGenerator.nextSequence());
			while(!api.isConnected()){
		        synchronized(api){
		        	try {
		        		api.wait();
					} catch (InterruptedException e) {
					}
		        }
		     }
			
			 try {
				api.sendMsg(active);	
			 } catch (IOException e) {
			 }
		}
	}
	
	
}
