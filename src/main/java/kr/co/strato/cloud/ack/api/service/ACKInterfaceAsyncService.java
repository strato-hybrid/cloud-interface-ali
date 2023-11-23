package kr.co.strato.cloud.ack.api.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.strato.cloud.ack.api.model.RequestParamDto;
import kr.co.strato.cloud.ack.plugin.common.model.CallbackData;
import kr.co.strato.cloud.ack.plugin.common.service.CallbackService;
import kr.co.strato.cloud.ack.plugin.common.service.Callbackable;
import kr.co.strato.cloud.ack.plugin.common.service.ClusterJobCallable;
import kr.co.strato.cloud.ack.plugin.common.service.ClusterJobExecutor;
import kr.co.strato.cloud.ack.plugin.vsphere.model.CreateMessageData;
import kr.co.strato.cloud.ack.plugin.vsphere.model.DeleteMessageData;
import kr.co.strato.cloud.ack.plugin.vsphere.model.ScaleMessageData;
import kr.co.strato.cloud.ack.plugin.vsphere.service.DeleteJobCallable;
import kr.co.strato.cloud.ack.plugin.vsphere.service.ProvisioningJobCallable;
import kr.co.strato.cloud.ack.plugin.vsphere.service.ScaleJobCallable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ACKInterfaceAsyncService implements Callbackable {
	
	private static Map<String, ClusterJobExecutor> clusterExecutorMap;
	
	@Autowired
	private CallbackService callbackService;

	public void asyncProvisioningCluster(CreateMessageData data) {
		
		log.debug("[asyncProvisioningCluster] > param = {}", data.toString());
		
		Long workJobIdx = data.getWorkJobIdx();
		String callbackUrl = data.getCallbackUrl();
		String jobType = data.getJobType();
		RequestParamDto.Create param = data.getParam();
		String clusterName = param.getClusterName();
		
		log.debug("[asyncProvisioningCluster] > Job Type: {}", jobType);
		
		ProvisioningJobCallable callable = new ProvisioningJobCallable(this, clusterName, callbackUrl, param);
		callable.setWorkJobIdx(workJobIdx);
		
		registryClusterJob(callable);
		
	}
	
	public void asyncDeleteCluster(DeleteMessageData data) {
			
		log.debug("[asyncDeleteCluster] > param = {}", data.toString());
		
		Long workJobIdx = data.getWorkJobIdx();
		String callbackUrl = data.getCallbackUrl();
		String jobType = data.getJobType();
		RequestParamDto.Delete param = data.getParam();
		String clusterId = param.getClusterId();
		
		log.debug("[asyncDeleteCluster] > Job Type: {}", jobType);
		
		DeleteJobCallable callable = new DeleteJobCallable(this, clusterId, callbackUrl, param);
		callable.setWorkJobIdx(workJobIdx);
		
		registryClusterJob(callable);
		
	}
		
	
	public void asyncScaleCluster(ScaleMessageData data) {
		
		log.debug("[asyncScaleCluster] > param = {}", data.toString());
		
		Long workJobIdx = data.getWorkJobIdx();
		String callbackUrl = data.getCallbackUrl();
		String jobType = data.getJobType();
		RequestParamDto.Scale param = data.getParam();
		String clusterName = param.getClusterName();
		
		log.debug("[asyncScaleCluster] > Job Type: {}", jobType);
		
		ScaleJobCallable callable = new ScaleJobCallable(this, clusterName, callbackUrl, param);
		callable.setWorkJobIdx(workJobIdx);
		
		registryClusterJob(callable);
		
	}

	public void registryClusterJob(ClusterJobCallable<Object> clusterJob) {

		log.debug("[registryClusterJob] > Async job registry");
		
		String clusterName = clusterJob.getClusterName();
		
		//작업을 Queue에 등록시키고 Executor를 실행한다.
		ClusterJobExecutor executer = getClusterExecutorMap().get(clusterName);
		if(executer == null) {
			executer = new ClusterJobExecutor();
			getClusterExecutorMap().put(clusterName, executer);
		}
		
		//Job 실행 등록.
 		executer.putClusterJob(clusterJob);
		if(!executer.isRun()) {
			//Executor를 실행
			executer.start();
		}
	}

	@Override
	public void sendCallback(ClusterJobCallable callable, CallbackData data) {
		String url = callable.getCallbackUrl();
		String status = data.getStatus();
		
		//callback 전송
		callbackService.sendCallback(url, data);
		
		//해당 클러스터에 작업 목록이 없는 경우 Executer 제거
		String clusterName = callable.getClusterName();		
		if(status.equals(CallbackData.STATUS_FINISH)) {
			ClusterJobExecutor executor = getClusterExecutorMap().get(clusterName);
			if(executor.getJobSize() == 0) {
				executor.stop();
				getClusterExecutorMap().remove(clusterName);
			}
		}
	}	
	
	public static Map<String, ClusterJobExecutor> getClusterExecutorMap() {
		if(clusterExecutorMap == null) {
			clusterExecutorMap = Collections.synchronizedMap(new HashMap<String, ClusterJobExecutor>());
		}
		return clusterExecutorMap;
	}
	
}
