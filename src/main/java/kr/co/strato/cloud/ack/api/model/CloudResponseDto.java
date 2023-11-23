package kr.co.strato.cloud.ack.api.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CloudResponseDto {

	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	public static class GetList {
		
		//클러스터 이름
		private String clusterName;
		
		//Kubernetes 버전
		private String kubernetesVersion;
		
		private List<NodePools> nopePools;
		
		@AllArgsConstructor
		@NoArgsConstructor
		@Data
		public static class NodePools {
			private String nodePoolName;
			private String vmType;
			private Integer nodeCount;
			
		}
	}
}
