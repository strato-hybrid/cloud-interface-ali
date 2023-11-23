package kr.co.strato.cloud.ack.global.util;

import java.io.InputStream;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import lombok.Builder;
import lombok.Data;

public class SSHExecCallable implements Callable<String> {	
	private static Logger logger = LoggerFactory.getLogger(SSHExecCallable.class);
	
	private boolean isRunning = false;

	private String command;
	
	private Session session;
    private ChannelExec channelExec;
    
    private ServerConfig serverConfig;
	
    public SSHExecCallable(ServerConfig serverConfig) {
    	this.serverConfig = serverConfig;
    }
    
	public SSHExecCallable(ServerConfig serverConfig, String command) {
		this(serverConfig);
		this.command = command;
	}
	
	
	@Override
	public String call() throws Exception {
		StringBuilder response = new StringBuilder();
		try {
			logger.info("SSHExec Start. Command: {}", command);
			isRunning = true;
			connectSSH();			
			
			channelExec = (ChannelExec) session.openChannel("exec");
	        channelExec.setCommand(command);
			
			
	        InputStream inputStream = channelExec.getInputStream();
	        channelExec.connect();
	        
	        byte[] buffer = new byte[8192];
            int decodedLength;
            
            while ((decodedLength = inputStream.read(buffer, 0, buffer.length)) > 0) {
                response.append(new String(buffer, 0, decodedLength));
            }
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			destroy();
			isRunning = false;
			logger.info("SSHExec End. Command: {}", command);
		}
		
		return response.toString();
	}
	
	/**
	 * SSH 연결
	 * @throws JSchException
	 */
	private void connectSSH() throws JSchException {
		String host = serverConfig.getHost();
		int port = serverConfig.getPort();
		String username = serverConfig.getUsername();
		String password = serverConfig.getPassword();
		
        session = new JSch().getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");       // 호스트 정보를 검사하지 않도록 설정
        session.connect();
    }
	
	public void setCommand(String command) {
		this.command = command;
	}
	
	public void destroy() {
		if (session != null) session.disconnect();
        if (channelExec != null) channelExec.disconnect();
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	@Data
	@Builder
	public static class ServerConfig {
		private String host;
	    private int port;
	    private String username;
	    private String password;
	    
	    public static ServerConfig getInstance(Environment environment) {
	    	String host = environment.getProperty("management-cluster.ssh.host");
			int port = environment.getProperty("management-cluster.ssh.port", Integer.class);
			String username = environment.getProperty("management-cluster.ssh.username");
			String password = environment.getProperty("management-cluster.ssh.password");
			
			ServerConfig config = ServerConfig.builder()
					.host(host)
					.port(port)
					.username(username)
					.password(password)
					.build();
			return config;
	    }
	}
}
