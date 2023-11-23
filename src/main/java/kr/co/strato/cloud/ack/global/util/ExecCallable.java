package kr.co.strato.cloud.ack.global.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecCallable implements Callable<Integer> {	
	private static Logger logger = LoggerFactory.getLogger(ExecCallable.class);
	
	private String[] excludeWord;
	public StringBuffer sbLog;
	private boolean isRunning = false;
	private int exitCode;
	private String command;
	private String dir;
	private Process process;
	private ExecutorService streamExecutor;
	private ExecutorService errorExecutor;
	
	
	public ExecCallable(String command) {
		this.command = command;
		this.sbLog = new StringBuffer();
	}
	
	
	@Override
	public Integer call() throws Exception {
		logger.info("ExecProcessAsync Start. Command: {}", command);
		isRunning = true;
		ProcessBuilder builder = new ProcessBuilder();
		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		if (isWindows) {
		    builder.command("cmd.exe", "/c", command);
		} else {
		    builder.command("sh", "-c", command);
		}
		
		builder.directory(new File(dir));
		process = null;
		try {
			process = builder.start();
		} catch (IOException e1) {
			logger.error("", e1);
		}
		
		StreamGobbler streamGobbler = 
				  new StreamGobbler(process.getInputStream(), 0);				
		StreamGobbler errorGobbler = 
				  new StreamGobbler(process.getErrorStream(), 1);
		
		streamExecutor = Executors.newSingleThreadExecutor();
		streamExecutor.submit(streamGobbler);
		
		errorExecutor = Executors.newSingleThreadExecutor();
		errorExecutor.submit(errorGobbler);
		
		exitCode = -1;
		try {
			exitCode = process.waitFor();
		} catch (InterruptedException e) {
			logger.error("", e);
		} finally {										
			if(!streamExecutor.isShutdown()) {
				streamExecutor.shutdown();
			}
			if(!errorExecutor.isShutdown()) {
				errorExecutor.shutdown();
			}
			
			isRunning = false;
			logger.info("ExecProcessAsync End. Command: {}", command);
			logger.info("ExecProcessAsync End. Exit code: {}", exitCode);
			
		}
		
		return exitCode;
	}
	
	public void set(String command, String dir, String[] excludeWord) {
		this.command = command;
		this.dir = dir;
		this.excludeWord = excludeWord;
	}
	
	public void set(String command, String dir) {
		set(command, dir, null);
	}
	
	public void destroy() {		
		streamExecutor.shutdown();
		errorExecutor.shutdown();		
		process.destroy();
	}
	

	/**
	 * 커맨드 실행.
	 * @param command
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public void processAsync(String command, String dir) {
		set(command, dir);
		Executors.newSingleThreadExecutor().submit(this);
	}
	
	public void processSync(String command, String dir) {
		set(command, dir);
		try {
			call();
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	public String getLog() {
		return sbLog.toString();
	}
	
	public int getExitCode() {
		return exitCode;
	}
	
	
	class StreamGobbler implements Runnable {
	    private InputStream inputStream;

	    public StreamGobbler(InputStream inputStream, int logType) {
	        this.inputStream = inputStream;
	    }

	    @Override
	    public void run() {   	
	        //new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
	    
	    	BufferedReader 	reader = new BufferedReader(new InputStreamReader(inputStream));
			
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					
					//예외 문자열이 등록되어 있을 경우 검사하여 기록에 남기지 않는다.
					if(excludeWord != null) {
						for(String word : excludeWord) {
							if(line.contains(word)) {
								continue;
							}
						}
					}
					
					sbLog.append(String.format("%s\r\n", line));
					logger.info(line);
					
				}
			} catch (IOException e) {
				logger.error("", e);
			}
	    }
	}
	
	public boolean isRunning() {
		return isRunning;
	}
}
