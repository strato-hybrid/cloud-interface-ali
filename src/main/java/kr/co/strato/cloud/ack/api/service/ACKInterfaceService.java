package kr.co.strato.cloud.ack.api.service;


import com.aliyun.cs20151215.Client;
import com.aliyun.cs20151215.models.*;
import com.aliyun.cs20151215.models.Runtime;
import kr.co.strato.cloud.ack.api.model.ACKCloudConfig;
import kr.co.strato.cloud.ack.api.model.CloudAuthArg;
import kr.co.strato.cloud.ack.api.model.CloudResponseDto.GetList;
import kr.co.strato.cloud.ack.api.model.RequestParamDto;
import kr.co.strato.cloud.ack.global.util.AlibabaCreateClient;
import kr.co.strato.cloud.ack.global.util.ExecCallable;
import kr.co.strato.cloud.ack.global.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class ACKInterfaceService {

	@Autowired
	Environment env;

	@Autowired
	private AlibabaCreateClient client;


	private String kubeConfigPath;


	public String provisioningCluster(CloudAuthArg authArg, RequestParamDto.Create arg)throws Exception  {
		String kubeConfig = null;

		log.info("[provisioningCluster] start");
		String regionId = arg.getRegionId();


		// 2. Create an ACK cluster.
		try {
			// 1. ACK 인증
			Client ackclient = getClient(authArg, regionId);

			//클러스터생성
			com.aliyun.cs20151215.models.CreateClusterResponse createClusterRes = createCluster(ackclient, arg);
			log.info("createCluster statusCode >>>" + createClusterRes.getStatusCode());
			if(createClusterRes.getStatusCode()==202){
				String clusterId = createClusterRes.body.clusterId;
				wait(authArg, clusterId);
					//kubeConfig 정보 가져오기
					kubeConfig = getKubeConfig(clusterId,authArg);
			}else{
				throw new Exception("fail to create Cluster");
			}

		}catch (Exception e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return kubeConfig;
	}

	/**
	 *  작업 대기 (클러스터 생성, 삭제, 업데이트)
	 */
	private synchronized void wait(CloudAuthArg authArg,String clusterId) {
		try {
			wait(20000);
		} catch (InterruptedException e) {
			log.error("fail sync wait", e);
		}
		// 20초마다 작업 상태 체크
		while (true) {
			try {
				String statusCheck = describeClusterDetail(authArg, clusterId).getBody().getState();
				log.info("Cluster:{} >>> STATUS: {}",clusterId,statusCheck);

				if (statusCheck.equals("running") ) {
					// 생성 완료상태이면 루프를 종료
					break;
				}else if(statusCheck.equals("failed")){
					throw new Exception("Error: fail to create cluster");
				}
			} catch (com.aliyun.tea.TeaException teaException) {
				if (teaException.getCode().equals("404") && teaException.getMessage().contains("ErrorClusterNotFound")) {
					log.error("Cluster not found >>>> possibly deleted: {}", clusterId, teaException);
				} else {
					log.error("Error occurred while communicating with Alibaba Cloud: {}", clusterId, teaException);
				}
				break;
			} catch (Exception e) {
				log.error("Error occurred while communicating with Alibaba Cloud: {}", clusterId, e);
				break;
			}
			try {
				wait(20000);
			} catch (InterruptedException e) {
				log.error("fail sync wait", e);
			}
		}
	}


	/**
	 *  클러스터 상세보기
	 * @param authArg
	 * @param clusterId
	 * @return
	 * @throws Exception
	 */
	public com.aliyun.cs20151215.models.DescribeClusterDetailResponse describeClusterDetail(CloudAuthArg authArg, String clusterId) throws Exception {
		log.info("[DescribeClusterDetailResponse] start");
		String regionId = authArg.getRegion();
		if(regionId== null){
			regionId = "ap-northeast-2";
		}
		Client ack = getClient(authArg, regionId);
		DescribeClusterDetailResponse describeClusterDetailResponse = null;
		try{
			describeClusterDetailResponse = ack.describeClusterDetail(clusterId);
		}catch (Exception e){
			throw new RuntimeException(e);
		}
		return describeClusterDetailResponse;
	}

	private Client getClient(CloudAuthArg authArg, String regionId) {
		Map<String, Object> principalProfile = client.getACKAuth(authArg, regionId);
		Client ackClient = (Client) principalProfile.get("client");
		return ackClient;
	}

	/**
	 *  클러스터 생성
	 *  : ACK cluster 는 두가지 네트워크 플러그인 중 하나를 선택해야함.
	 *
	 *  1. Terway -> 알리바바 클라우드에서 ack용으로 개발된 네트워크로 표준 Kubernetes 네트워크 정책을 사용.
	 *  2. Flannel -> 오픈소스 CNI 플러그인으로 기본기능만 제공되며 표준 Kubernetes 네트워크 정책을 지원하지 않음.
	 *
	 *  현재는 대표로 Terway 방식으로 생성하고 있음. => .setAddons(Collections.singletonList(new Addon()
	 * 						.setName("terway-eniip")
	 * 						.setConfig("")))
	 */
	public static com.aliyun.cs20151215.models.CreateClusterResponse createCluster(Client client, RequestParamDto.Create arg) throws Exception {
		//노드풀 세팅
		Nodepool nodepool = new Nodepool().setScalingGroup(new Nodepool.NodepoolScalingGroup()
						.setVswitchIds(Arrays.asList(arg.getVswitchIds()))
						.setSystemDiskCategory(arg.getDiskCategory())
						.setSystemDiskSize(Long.valueOf(arg.getWorkerSpec().getStorage()))
						.setSystemDiskPerformanceLevel("PL0")
						.setInstanceTypes(Arrays.asList(arg.getWorkerInstanceType()))
						.setInstanceChargeType("PostPaid")
						.setLoginPassword("")
						.setPlatform(arg.getTemplateName())
						.setImageType(arg.getTemplateName()))
				.setNodepoolInfo(new Nodepool.NodepoolNodepoolInfo().setName(arg.getNodepoolName()))
				.setCount(Long.valueOf(arg.getWorkerSpec().getNodeCount()))
				.setKubernetesConfig(new Nodepool.NodepoolKubernetesConfig()
						.setCpuPolicy("none")
						.setRuntime("containerd")
						.setRuntimeVersion("1.6.20"));
		//클러스터 생성
		com.aliyun.cs20151215.models.CreateClusterRequest request = new com.aliyun.cs20151215.models.CreateClusterRequest()
				.setName(arg.getClusterName())
				.setClusterType("ManagedKubernetes")
				.setKubernetesVersion(arg.getKubernetesVersion())
				.setRegionId(arg.getRegionId())
				.setAddons(Collections.singletonList(new Addon()
						.setName("terway-eniip")
						.setConfig("")))
				.setLoadBalancerSpec(arg.getLoadBalancerSpec())
				.setOsType("Linux")
				.setPlatform(arg.getTemplateName())
				.setImageType(arg.getTemplateName())
				.setPodVswitchIds(java.util.Arrays.asList(arg.getVswitchIds()))
				.setRuntime(new Runtime().setName("containerd").setVersion("1.6.20"))
				.setChargeType("PostPaid")
				.setVpcid(arg.getVpcId())
				.setServiceCidr(arg.getServiceCidr())
				.setVswitchIds(java.util.Arrays.asList(arg.getVswitchIds()))
				.setNodepools(Collections.singletonList(nodepool))
				.setNumOfNodes(0L)
				;

		return client.createCluster(request);
	}

	/**
	 * 클러스터 삭제
	 * @param arg
	 * @return
	 */

	public boolean deleteCluster(RequestParamDto.Delete arg) throws Exception {
		CloudAuthArg config = new CloudAuthArg(env.getProperty("ack.auth.accessKeyId"), env.getProperty("ack.auth.accessKeySecret"));
		return deleteCluster(config, arg);
	}

	public boolean deleteCluster(CloudAuthArg authArg, RequestParamDto.Delete arg) throws Exception {
		boolean isDelete = false;
		String regionId = authArg.getRegion();
		if(regionId== null){
			regionId = "ap-northeast-2";
		}
		Map<String, Object> principalProfile = client.getACKAuth(authArg,regionId);
		com.aliyun.cs20151215.Client client = (com.aliyun.cs20151215.Client) principalProfile.get("client");
		com.aliyun.cs20151215.models.DeleteClusterRequest deleteClusterRequest = new com.aliyun.cs20151215.models.DeleteClusterRequest()
				.setRetainResources(java.util.Arrays.asList(
						arg.getClusterId()
				));
		log.info("[deleteCluster] start");
		DeleteClusterResponse deleteClusterResponse = client.deleteCluster(arg.getClusterId(), deleteClusterRequest);
		if(deleteClusterResponse.getStatusCode()==202){
			wait(authArg,arg.getClusterId());
			isDelete = true;
		}else {
			throw new Exception("Error: fail to delete cluster" );
		}
		return isDelete;
	}

	/**
	 * 클러스터 스케일 조정
	 * @param arg
	 * @return
	 */
	public boolean scaleCluster(RequestParamDto.Scale arg) {
		ACKCloudConfig config = ACKCloudConfig.getDefault(env);
		return scaleCluster(config, arg);
	}


	public boolean scaleCluster(ACKCloudConfig config, RequestParamDto.Scale arg) {
		boolean isSuccess = false;

		//TODO: 클러스터 스케일 조정 로직 작성

		return isSuccess;
	}

	/**
	 * 클러스터 중복 채크
	 * true : 중복, false: 미중복
	 * @param arg
	 * @return
	 */
	public boolean duplicateCheckCluster(ACKCloudConfig config, RequestParamDto.Duplicate arg) {
		boolean isDuplicate = false;

		//TODO: 클러스터 이름 중복 채크 로직 작성

		return false;
	}

	/**
	 * 클러스터 리스트 조회
	 * @param
	 * @return
	 */
	public List<GetList> getListCluster(CloudAuthArg authArg, String region) {
		log.info("[getListCluster] start");

		// 1. ACK 인증
		Map<String, Object> principalProfile = client.getACKAuth(authArg, region);

		Client ack = (Client) principalProfile.get("client");

		List<GetList> clusters = new ArrayList<GetList>();

		DescribeClustersV1Request request =new DescribeClustersV1Request();
		request.setPageSize(50L); // 페이지 크기 설정
		request.setPageNumber(1L); // 페이지 번호 설정
		try {
			DescribeClustersV1Response response = ack.describeClustersV1(request);
			for (DescribeClustersV1ResponseBody.DescribeClustersV1ResponseBodyClusters c : response.getBody().getClusters()) {
				GetList cluster = new GetList();
				cluster.setClusterName(c.getName());
				cluster.setKubernetesVersion(c.getDockerVersion());

				System.out.println("Cluster Name: " + c.getName());
				System.out.println("Cluster Id: " + c.getClusterId());
				System.out.println("Cluster State: " + c.getState());
				System.out.println("Cluster Region Id: " + c.getRegionId());
				System.out.println("--------------------------");
				List<GetList.NodePools> nodePools = new ArrayList<>();

				DescribeClusterNodePoolsResponse nodePoolsResponse = ack.describeClusterNodePools(c.getClusterId());
				for(DescribeClusterNodePoolsResponseBody.DescribeClusterNodePoolsResponseBodyNodepools n : nodePoolsResponse.getBody().nodepools) {

					GetList.NodePools nodePoolInfo = new GetList.NodePools();
					nodePoolInfo.setNodePoolName(n.getNodepoolInfo().getName());
					nodePoolInfo.setNodeCount(n.nodepoolInfo.getNodepoolId().length());
					nodePoolInfo.setVmType(n.getKubernetesConfig().getRuntimeVersion());

					nodePools.add(nodePoolInfo);
				}

				cluster.setNopePools(nodePools);
				clusters.add(cluster);
			}

		}catch (Exception e) {
			throw new RuntimeException(e);
		}
		return clusters;
	}


	public String getKubeConfig(String clusterId, CloudAuthArg authArg) {
		log.info("[getKubeConfig] start");
		if(authArg.isNull()){
			ACKCloudConfig ackCloudConfig = ACKCloudConfig.getDefault(env);
			authArg.setRegion("ap-northeast-2");
			authArg.setAccessKey(ackCloudConfig.getUsername());
			authArg.setAccessSecret(ackCloudConfig.getPassword());
		}
		String regionId = authArg.getRegion();
		Map<String, Object> principalProfile = client.getACKAuth(authArg, regionId);

		Client ack = (Client) principalProfile.get("client");

		String kubeConfig =null;
		DescribeClusterUserKubeconfigRequest request = new DescribeClusterUserKubeconfigRequest();
		try {
			DescribeClusterUserKubeconfigResponse response = ack.describeClusterUserKubeconfig(clusterId,request);
			kubeConfig = response.getBody().config;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return kubeConfig;

	}

}
