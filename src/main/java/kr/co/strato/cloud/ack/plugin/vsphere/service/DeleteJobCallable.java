package kr.co.strato.cloud.ack.plugin.vsphere.service;

import kr.co.strato.cloud.ack.api.model.RequestParamDto;
import kr.co.strato.cloud.ack.api.service.ACKInterfaceService;
import kr.co.strato.cloud.ack.global.config.ApplicationContextProvider;
import kr.co.strato.cloud.ack.plugin.common.service.Callbackable;
import kr.co.strato.cloud.ack.plugin.common.service.ClusterJobCallable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeleteJobCallable extends ClusterJobCallable<Object> {
	
	private RequestParamDto.Delete arg;
	
	public DeleteJobCallable(Callbackable callbackable, String clusterName, String callbackUrl, RequestParamDto.Delete arg) {
		super(callbackable, clusterName, callbackUrl);
		this.arg = arg;
	}

	@Override
	public Object clusterJob() throws Exception {
		
		log.debug("[DeleteClusterJob] >> Request Param = {}", arg.toString());
		
		ACKInterfaceService interfaceService = ApplicationContextProvider.getBean(ACKInterfaceService.class);
		boolean result = interfaceService.deleteCluster(arg);
		
		log.debug("[DeleteClusterJob] >> result = {}", result);
		
		return result;
	}

}
