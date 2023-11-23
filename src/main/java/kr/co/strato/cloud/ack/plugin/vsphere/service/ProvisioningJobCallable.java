package kr.co.strato.cloud.ack.plugin.vsphere.service;

import kr.co.strato.cloud.ack.api.model.CloudAuthArg;
import kr.co.strato.cloud.ack.api.model.RequestParamDto;
import kr.co.strato.cloud.ack.api.model.ACKCloudConfig;
import kr.co.strato.cloud.ack.api.service.ACKInterfaceService;
import kr.co.strato.cloud.ack.global.config.ApplicationContextProvider;
import kr.co.strato.cloud.ack.plugin.common.service.Callbackable;
import kr.co.strato.cloud.ack.plugin.common.service.ClusterJobCallable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProvisioningJobCallable extends ClusterJobCallable<Object> {
	private ACKInterfaceService interfaceService;
	private RequestParamDto.Create arg;
	private CloudAuthArg authArg;
	public ProvisioningJobCallable(Callbackable callbackable, String clusterName, String callbackUrl, RequestParamDto.Create arg) {
		this(callbackable, clusterName, callbackUrl, arg, null);
	}
	
	public ProvisioningJobCallable(Callbackable callbackable, String clusterName, String callbackUrl, RequestParamDto.Create arg, CloudAuthArg authArg) {
		super(callbackable, clusterName, callbackUrl);
		this.arg = arg;
		this.authArg = authArg;
	}
	
	@Override
	public Object clusterJob() throws Exception {
		
		log.debug("[ProvisioningClusterJob] >> Request Param = {}", arg.toString());


		interfaceService = ApplicationContextProvider.getBean(ACKInterfaceService.class);
		String result = interfaceService.provisioningCluster(authArg, arg);

		log.debug("[ProvisioningClusterJob] >> result = {}", result);
		
		return result;
	}

}
