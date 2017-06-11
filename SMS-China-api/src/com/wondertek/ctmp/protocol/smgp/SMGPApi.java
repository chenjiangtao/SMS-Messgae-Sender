package com.wondertek.ctmp.protocol.smgp;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.wondertek.ctmp.protocol.util.ByteUtil;
import com.wondertek.ctmp.protocol.util.MD5;
import com.wondertek.ctmp.protocol.util.SequenceGenerator;



public class SMGPApi {

	public static final byte MT=0;
	public static final byte MO=1;
	public static final byte MT_MO=2;
	
	private Socket  socket;
	private boolean connected;
	


	private String host;
	private int port;
	private String clientId;
	private String password;
	private byte loginMode;
	private byte version;
	
	private Object readLock=new Object();
	private Object writeLock=new Object();
	
    private int soTimeout=30000;
	
	public SMGPApi(){
		
	}
	
	//浣滀负鏈嶅姟绔娇鐢�
	public SMGPApi(Socket socket){
		this.socket=socket;
		this.connected=true;
	}
	
	
	public synchronized SMGPLoginRespMessage connect() throws IOException{
		    
		    disconnect();
		    
		    socket=new Socket(host,port);
		    SMGPLoginMessage loginMsg=new SMGPLoginMessage();
		    loginMsg.setClientId(clientId);
		    loginMsg.setLoginMode(loginMode);
		    loginMsg.setVersion(version);
		    
		    Calendar calendar = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
			String tmp=dateFormat.format(calendar.getTime());
			loginMsg.setTimestamp(Integer.parseInt(tmp));
			
			loginMsg.setClientAuth(MD5.md5(clientId,password,tmp));
		   
			loginMsg.setSequenceNumber(SequenceGenerator.nextSequence());
			
		    try {	
		    	writeBytes(loginMsg.toBytes());
			} catch (Exception e) {
				e.printStackTrace();
				throw new IOException(e.getMessage());
			} 
			
			SMGPBaseMessage baseMsg=null;
			
			byte[] header = new byte[SMGPBaseMessage.SZ_HEADER];
			byte[] cmdBytes = null;
			
			//begin process read data
			try {
				readBytes(header, 0, SMGPBaseMessage.SZ_HEADER);
				int cmdLen = ByteUtil.byte2int(header, 0);

				if (cmdLen > 8096 || cmdLen < SMGPBaseMessage.SZ_HEADER) {
					close();
					throw new IOException("read stream error,cmdLen="+ cmdLen + ",close connection");
				}
				cmdBytes = new byte[cmdLen];
				System.arraycopy(header, 0, cmdBytes, 0, SMGPBaseMessage.SZ_HEADER);
				readBytes(cmdBytes, SMGPBaseMessage.SZ_HEADER, cmdLen - SMGPBaseMessage.SZ_HEADER);
			} catch (IOException readError) {
				close();
				throw readError;
			}
			
			try {
				 baseMsg = SMGPConstants.fromBytes(cmdBytes);
				
			} catch (Exception e) {
				close();
				e.printStackTrace();
				throw new IOException("build SMGPBaseMessage error:"+e.getMessage());
			}	
			if(baseMsg==null)return null;
			if(baseMsg instanceof SMGPLoginRespMessage){
				SMGPLoginRespMessage resp=(SMGPLoginRespMessage)baseMsg;
				if(resp.getStatus()==0){
					connected=true;
					return resp;
				}else{
					System.out.println("bind fail,result:"+resp.getStatus());
					return resp;
				}
			}else{
				close();
				throw new IOException("the first packet was not SMGPBindRespMessage:"+baseMsg);
			}		
	}
	
	public synchronized void disconnect(){
		if(!connected)return;
		SMGPExitMessage term=new SMGPExitMessage();
		try {
			sendMsg(term);
		    SMGPBaseMessage baseMsg=null;
		    while((baseMsg=receiveMsg())!=null){
		    	if(baseMsg instanceof SMGPExitRespMessage){
		    		break;
		    	}
		    }
			close();
			connected=false;
		} catch (IOException e) {
			//ignore
		}
		
	}
	
	
	public  void sendMsg(SMGPBaseMessage baseMsg) throws IOException{
		if(baseMsg==null) return ;
		if(!connected){
			throw new IOException("socket has not established");
		}
		if(baseMsg.getSequenceNumber()==0){
		    baseMsg.setSequenceNumber(SequenceGenerator.nextSequence());
		}
		try{
			writeBytes(baseMsg.toBytes());
		}catch(Exception e){
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}

	}
	
	public  SMGPBaseMessage receiveMsg() throws IOException{
		
		if(!connected){
			throw new IOException("socket has not established");
		}
		byte[] header = new byte[SMGPBaseMessage.SZ_HEADER];
		byte[] cmdBytes = null;
		
		//begin process read data
		try {
			readBytes(header, 0, SMGPBaseMessage.SZ_HEADER);
			int cmdLen = ByteUtil.byte2int(header, 0);

			if (cmdLen > 8096 || cmdLen < SMGPBaseMessage.SZ_HEADER) {
				close();
				throw new IOException("read stream error,cmdLen="+ cmdLen + ",close connection");
			}
			cmdBytes = new byte[cmdLen];
			System.arraycopy(header, 0, cmdBytes, 0, SMGPBaseMessage.SZ_HEADER);
			readBytes(cmdBytes, SMGPBaseMessage.SZ_HEADER, cmdLen - SMGPBaseMessage.SZ_HEADER);
		} catch (IOException readError) {
			close();
			throw readError;
		}
		
		try {
			SMGPBaseMessage baseMsg = SMGPConstants.fromBytes(cmdBytes);
			return baseMsg;
		} catch (Exception e) {
			e.printStackTrace();
			close();
			throw new IOException("build SMGPBaseMessage error:"+e.getMessage());
		}	
	}
	
	
	private void readBytes(byte[] bytes,int offset, int len) throws IOException{
		if(socket.isClosed())
			throw new IOException("Socket is closed");
		socket.setSoTimeout(soTimeout);
		InputStream  in = socket.getInputStream();
        int count=0;
        synchronized(readLock){         
	        while(len>0 && (count=in.read(bytes,offset,len))>-1){
	        	offset+=count;
	        	len-=count;
	        }
        }
		
	}
	
	private void writeBytes(byte[] bytes) throws IOException {
		if(socket.isClosed())
			throw new IOException("Socket is closed");
		synchronized(writeLock){
			OutputStream out=socket.getOutputStream();
		    out.write(bytes);
		    out.flush();
		}
	}
	
	public void close(){
		try {
			socket.close();
			connected=false;
			
		} catch (Exception e) { //ignore
		}
		
	    synchronized(readLock){
	    	readLock.notifyAll();
	    }

	    synchronized(writeLock){
	    	writeLock.notifyAll();
	    }

	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}



	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}


	
	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public byte getLoginMode() {
		return loginMode;
	}

	public void setLoginMode(byte loginMode) {
		this.loginMode = loginMode;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	public String toString(){
		StringBuffer buffer=new StringBuffer();
		buffer.append("SMGPApi:[clientId=").append(clientId).append(",")
		      .append("host=").append(host).append(",")
		      .append("port=").append(port).append(",")
		      .append("password=").append(password).append(",")
		      .append("loginMode=").append(loginMode).append("]");
		return buffer.toString();
	}
	
	
	
}