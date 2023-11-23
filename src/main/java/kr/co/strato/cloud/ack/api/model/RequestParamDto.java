package kr.co.strato.cloud.ack.api.model;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel(description = "Sample Interface Request 파라메타")
@Data
public class RequestParamDto {
	
	@Data
	public static class Create {
		@NotNull
	    private String clusterName;

		private String templateName; //osType
		
		private String controlPlaneIp;
		
		private NodeSpec masterSpec;
		
		private NodeSpec workerSpec;
		
		private IpPool ipPool;

		@NotNull
		private String regionId;

		private String kubernetesVersion;

		/*필요 파라미터*/
		private String vpcId;
		private String workerInstanceType;//인스턴스 사양
		private String nodepoolName;
		private String vswitchIds; //가상 스위치 Id
		private String serviceCidr;// 서비스 Cidr
		private String loadBalancerSpec;
		private String diskCategory;

	}
	
	@Data
	public static class Scale {
		@NotNull
		private String clusterName;
				 
		@NotNull
		private Integer nodeCount;
	}
	
	@Data
	public static class Delete {
//		@NotNull
//		private String clusterName;
		@NotNull
		private String clusterId;
		private String regionId;
	}
	
	@Data
	public static class Duplicate {
		@NotNull
		private String clusterName;
	}
	
	@Data
	public static class NodeSpec {
		
		@NotNull
		private Integer cpu;
		
		@NotNull
		private Integer memory;
		
		@NotNull
		private Integer storage; //diskSize
		
		@NotNull
		private Integer nodeCount;
	}
	
	@Data
	public static class IpPool {
		
		@NotNull
		private String start;
		
		@NotNull
		private String end;
		
	}
	
}
