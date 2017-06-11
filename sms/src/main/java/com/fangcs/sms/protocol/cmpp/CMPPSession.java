package com.fangcs.sms.protocol.cmpp;

import com.fangcs.sms.protocol.Message;
import com.fangcs.sms.protocol.Session;
import com.fangcs.sms.protocol.cmpp.message.*;
import com.fangcs.sms.protocol.cmpp.util.SequenceGenerator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;


public class CMPPSession implements Session {

    private static final Logger log = LogManager.getLogger(CMPPSession.class);

    private CMPPConnection connection;
    private String sessionId;
    private boolean authenticated;
    private Object lock = new Object();

    public CMPPSession(CMPPConnection connection, boolean authenticated){
        super();
        this.connection = connection;
        this.sessionId = UUID.randomUUID().toString();
        this.authenticated = authenticated;
    }

   
    public String getSessionId() {
        return sessionId;
    }

   
    public boolean isAuthenticated() {
        return authenticated;
    }

   
    public void submit(String content, String spNumber, String userNumber){
        CMPPSubmitMessage submit = new CMPPSubmitMessage();
        submit.setServiceId("ACORN");
        submit.setAtTime("");
        submit.setSrcId(spNumber);
        submit.setMsgSrc(connection.getSourceAddr());
        submit.setFeeType("01");
        submit.setMsgText("浣犲ソ", 15);
        submit.addDestTerminalId(userNumber);
        submit.setSequenceId(SequenceGenerator.nextSequence());
        send(submit);
    }

   
    public void heartbeat(){
        if(isAuthenticated()) {
            CMPPActiveTestMessage activeTest=new CMPPActiveTestMessage();
            activeTest.setSequenceId(SequenceGenerator.nextSequence());
            send(activeTest);
        }
    }

  
    public boolean authenticate() {

        CMPPConnectMessage loginMsg=new CMPPConnectMessage();
        loginMsg.setSourceAddr(connection.getSourceAddr());
        loginMsg.setVersion(connection.getVersion());

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
        String tmp=dateFormat.format(calendar.getTime());
        loginMsg.setTimestamp(Integer.parseInt(tmp));
        loginMsg.setSharedSecret(connection.getPassword());
        loginMsg.setSequenceId(SequenceGenerator.nextSequence());
        send(loginMsg);
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException ex){
                setAuthenticated(false);
            }
        }
        return isAuthenticated();
    }

  
    public void close() throws IOException {
       
        if(isAuthenticated() ) {
            CMPPTerminateMessage exit = new CMPPTerminateMessage();
            exit.setSequenceId(SequenceGenerator.nextSequence());
            send(exit);
            synchronized (lock) {
                try {
                    lock.wait(6000);
                } catch (InterruptedException ex){
                    setAuthenticated(false);
                }
            }
        }
        connection.close();
    }

   
    public void send(Message message){
        connection.send(message);
    }

   
    public void process(Message message) throws IOException {
        if(message instanceof CMPPBaseMessage){
            CMPPBaseMessage baseMsg = (CMPPBaseMessage)message;
            if(isAuthenticated()){
                if (baseMsg instanceof CMPPActiveTestMessage) {
                    process((CMPPActiveTestMessage)baseMsg);
                } else if (baseMsg instanceof CMPPActiveTestRespMessage) {
                    // do nothing
                } else if(baseMsg instanceof CMPPTerminateRespMessage){
                    process((CMPPTerminateRespMessage)baseMsg);
                } else if(message instanceof CMPPSubmitRespMessage) {
                    process((CMPPSubmitRespMessage)message);
                } else if(message instanceof CMPPDeliverMessage) {
                    process((CMPPDeliverMessage)message);
                }
            } else if(baseMsg instanceof CMPPConnectRespMessage){
                process((CMPPConnectRespMessage)baseMsg);
            } else {
                throw new IOException("the first packet was not CMPPBindRespMessage:" + baseMsg);
            }
        }
    }

    private void process(CMPPActiveTestMessage msg) throws IOException {
        CMPPActiveTestRespMessage resp = new CMPPActiveTestRespMessage();
        resp.setSequenceId(msg.getSequenceId());
        send(resp);
    }

    private void process(CMPPConnectRespMessage rsp) throws IOException {
        synchronized (lock) {
            if(rsp.getStatus() == 0){
                setAuthenticated(true);
                log.info("cmpp login success host=" + connection.getHost() + ",port=" + connection.getPort() + ",sourceAddr=" + connection.getSourceAddr());
            } else {
                setAuthenticated(false);
                log.error("cmpp login failure, host=" + connection.getHost() + ",port=" + connection.getPort() + ",sourceAddr=" + connection.getSourceAddr() + ",status=" + rsp.getStatus());
            }
            lock.notifyAll();
        }
    }

    private void process(CMPPTerminateRespMessage msg) throws IOException {
        synchronized (lock) {
            setAuthenticated(false);
            lock.notifyAll();
        }
        log.info("cmpp exist success host=" + connection.getHost() + ",port=" + connection.getPort() + ",sourceAddr=" + connection.getSourceAddr());
    }

    private void process(CMPPSubmitRespMessage rsp) throws IOException {
        switch (rsp.getResult())   {
            case 0:{   //鍙戦�鎴愬姛

            } break;
            case 103:{  //骞冲彴娴佹帶,鍙戦�閫熷害杩囧揩

            } break;
            default: break;
        }
    }

    private void process(CMPPDeliverMessage msg) throws IOException {

        CMPPDeliverRespMessage rsp = new CMPPDeliverRespMessage();
        rsp.setSequenceId(msg.getSequenceId());
        rsp.setMsgId(msg.getMsgId());
        rsp.setResult(0);
        send(rsp);

    }

    private void setAuthenticated(boolean value) {
        this.authenticated = value;
    }
}

