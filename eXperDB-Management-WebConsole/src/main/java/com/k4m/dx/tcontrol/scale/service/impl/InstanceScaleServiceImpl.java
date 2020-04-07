package com.k4m.dx.tcontrol.scale.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.k4m.dx.tcontrol.admin.accesshistory.service.impl.AccessHistoryDAO;
import com.k4m.dx.tcontrol.admin.dbserverManager.service.DbServerVO;
import com.k4m.dx.tcontrol.cmmn.CmmnUtils;
import com.k4m.dx.tcontrol.cmmn.client.ClientInfoCmmn;
import com.k4m.dx.tcontrol.cmmn.client.ClientProtocolID;
import com.k4m.dx.tcontrol.common.service.AgentInfoVO;
import com.k4m.dx.tcontrol.common.service.HistoryVO;
import com.k4m.dx.tcontrol.common.service.impl.CmmnServerInfoDAO;
import com.k4m.dx.tcontrol.scale.service.InstanceScaleService;
import com.k4m.dx.tcontrol.scale.service.InstanceScaleVO;

import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;

/**
* @author 
* @see aws scale 관련 화면 serviceImpl
* 
*      <pre>
* == 개정이력(Modification Information) ==
*
*   수정일                 수정자                   수정내용
*  -------     --------    ---------------------------
*  2020.03.24              최초 생성
*      </pre>
*/
@Service("InstanceScaleServiceImpl")
public class InstanceScaleServiceImpl extends EgovAbstractServiceImpl implements InstanceScaleService {

	@Resource(name = "instanceScaleDAO")
	private InstanceScaleDAO instanceScaleDAO;

	@Resource(name = "accessHistoryDAO")
	private AccessHistoryDAO accessHistoryDAO;
	
	@Resource(name = "cmmnServerInfoDAO")
	private CmmnServerInfoDAO cmmnServerInfoDAO;

	/**
	 * scale list setting
	 * 
	 * @param instanceScaleVO
	 * @throws Exception 
	 */
	@Override
	public JSONObject instanceListSetting(InstanceScaleVO instanceScaleVO) throws Exception {
		JSONObject result = new JSONObject();
		JSONArray jsonArray = new JSONArray(); // 객체를 담기위해 JSONArray 선언.
		JSONArray InstancesArrly = new JSONArray();

		String scaleId = (String)instanceScaleVO.getScale_id();
		JSONObject scalejsonObj = new JSONObject();
		String stateChk = "";
		
		Map<String, Object> param = new HashMap<String, Object>();
		Map<String, Object> agentList = null;

		Properties props = new Properties();
		props.load(new FileInputStream(ResourceUtils.getFile("classpath:egovframework/tcontrolProps/globals.properties")));
		
		//scalejsonObj = scaleJsonDownSearch("main", scaleId); 기존 조회방식
		
		param.put("search_gbn", "main");
		param.put("scale_set", "");
		param.put("login_id", (String)instanceScaleVO.getLogin_id());
		param.put("instance_id", scaleId);                          //확인완료
		param.put("db_svr_id", instanceScaleVO.getDb_svr_id());
		param.put("process_id", "");
		param.put("monitering", "");
		
		////
		//db_svr_ipadr_id 넣어야함

		try {	
			agentList = scaleInAgent(param);
			
			if (!agentList.isEmpty()) {
				String resultCode = (String)agentList.get(ClientProtocolID.RESULT_CODE);
				if (resultCode.equals("0")) {
					scalejsonObj = (JSONObject)agentList.get(ClientProtocolID.RESULT_DATA);
				}
			}
			
			if (!scalejsonObj.isEmpty()) {
				InstancesArrly = (JSONArray) scalejsonObj.get("Instances");
	
				int iNumCnt = 0;
				
				if (InstancesArrly != null) {
					for (int j = 0; j < InstancesArrly.size(); j++) {
						JSONObject jsonObj = new JSONObject();
						JSONObject instancesObj = (JSONObject)InstancesArrly.get(j);
						
						JSONArray securityGroupsArrly = (JSONArray) instancesObj.get("SecurityGroups");     //보안그룹

						jsonObj.put("public_IPv4", (String) instancesObj.get("PublicDnsName"));             //public dns IPv4
						jsonObj.put("IPv4_public_ip", (String) instancesObj.get("PublicIpAddress"));        //IPv4 IP
						jsonObj.put("private_ip_address", (String) instancesObj.get("PrivateIpAddress"));   //private_IP

						jsonObj.put("instance_id", instancesObj.get("InstanceId"));                         //인스턴트 id - 확인완료
						jsonObj.put("private_dns_name", (String) instancesObj.get("PrivateDnsName"));       //private_dna_name
						jsonObj.put("key_name", (String) instancesObj.get("KeyName"));                      //키이름
						jsonObj.put("instance_type", (String) instancesObj.get("InstanceType"));            //인스턴트 타입
						
						/* start time */
						String LaunchTimeStr = instancesObj.get("LaunchTime").toString();  //시작일자
						
						String dateLocale = "KST";
					    String lang = props.get("lang").toString();
						if (!lang.equals("ko")) {dateLocale = "UTC";}

						SimpleDateFormat old_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // 받은 데이터 형식
				        old_format.setTimeZone(TimeZone.getTimeZone(dateLocale));
				        SimpleDateFormat new_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 바꿀 데이터 형식
				        
				        Date old_date = old_format.parse(LaunchTimeStr);
						jsonObj.put("start_time", new_format.format(old_date));
						/* start time end */

						jsonObj.put("monitoring_state", (String) instancesObj.get("MonitoringState"));            //모니터링 상태
						stateChk = instancesObj.get("InstanceStatusName").toString();
						jsonObj.put("instance_status_name", stateChk);     											//인스턴스 상태
						jsonObj.put("availability_zone", (String) instancesObj.get("AvailabilityZone")); 
						jsonObj.put("tagsValue", (String) instancesObj.get("TagsName"));                          //name

						/* securityGroupsArrly start*/ 
						if (securityGroupsArrly != null) {
							String nameVal = "";
							String idVal = "";

							for (int k = 0; k < securityGroupsArrly.size(); k++) {
								JSONObject securityGroupObj = (JSONObject)securityGroupsArrly.get(k);

								if (!nameVal.equals("")) {
									nameVal += ", ";
									idVal += ", ";
								}

								nameVal += securityGroupObj.get("GroupName").toString();
								idVal += securityGroupObj.get("GroupId").toString();
							}

							jsonObj.put("security_group", nameVal);
							jsonObj.put("securityGroupId", idVal);
						}
						/* securityGroupsArrly end */
						
						if (!stateChk.contains("terminated")) {
							iNumCnt = iNumCnt + 1;
							jsonObj.put("rownum", iNumCnt);                                                //no
							jsonArray.add(jsonObj);
						}
					}
				}
			}

			if (!jsonArray.isEmpty()) {
				result.put("data", jsonArray);
			} else {
				result.put("data", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * scale 상세 조회
	 * 
	 * @param instanceScaleVO
	 * @throws Exception 
	 */
	@Override
	public JSONArray instanceInfoListSetting(InstanceScaleVO instanceScaleVO) throws Exception {
		Set key = null;
		Iterator<String> iter = null;
		String scaleId = (String)instanceScaleVO.getScale_id();
		JSONArray result = new JSONArray(); // 객체를 담기위해 JSONArray 선언.
		JSONArray scaleArrly = new JSONArray();
		JSONObject scalejsonObj = new JSONObject();

		Properties props = new Properties();
		props.load(new FileInputStream(ResourceUtils.getFile("classpath:egovframework/tcontrolProps/globals.properties")));		

		Map<String, Object> param = new HashMap<String, Object>();
		Map<String, Object> agentList = null;
		
		param.put("search_gbn", "");
		param.put("scale_set", "");
		param.put("login_id", (String)instanceScaleVO.getLogin_id());
		param.put("instance_id", scaleId);                           //확인완료
		param.put("db_svr_id", instanceScaleVO.getDb_svr_id());
		param.put("process_id", "");
		param.put("monitering", "");

		try {
			agentList = scaleInAgent(param);
			
			if (!agentList.isEmpty()) {
				String resultCode = (String)agentList.get(ClientProtocolID.RESULT_CODE);
				if (resultCode.equals("0")) {
					scalejsonObj = (JSONObject)agentList.get(ClientProtocolID.RESULT_DATA);
				}
			}
			
			if (!scalejsonObj.isEmpty()) {
				scaleArrly = (JSONArray) scalejsonObj.get("Reservations");

				if (scaleArrly != null) {
					for (int i = 0; i < scaleArrly.size(); i++) {
						String instantsIdChk = "";
						JSONObject jsonObj = new JSONObject();
						JSONObject scaleSubObj = (JSONObject)scaleArrly.get(i);
					
						JSONArray InstancesArrly = (JSONArray) scaleSubObj.get("Instances");
					
						//jsonObj.put("rownum", i + 1);                                                //no
						jsonObj.put("owner", (String) scaleSubObj.get("OwnerId"));                   //소유자
						jsonObj.put("reservation_id", (String) scaleSubObj.get("ReservationId"));    //예약id

						if (InstancesArrly != null) {
							for (int j = 0; j < InstancesArrly.size(); j++) {
								JSONObject instancesObj = (JSONObject)InstancesArrly.get(j);
							
								JSONObject monitoringObj = (JSONObject)instancesObj.get("Monitoring");              		//모니터링
								JSONObject stateObj = (JSONObject)instancesObj.get("State");                        		//상태 
								JSONObject placementObj = (JSONObject)instancesObj.get("Placement");                		//가상서버
								JSONObject stateReasonObj = (JSONObject)instancesObj.get("StateReason");            		//상태사유
								JSONObject cctRevtSpectObj = (JSONObject)instancesObj.get("CapacityReservationSpecification"); //용량예약설정
								JSONObject hibernationOptionsObj = (JSONObject)instancesObj.get("HibernationOptions");		//최대절전
								JSONObject cpuOptionsObj = (JSONObject)instancesObj.get("CpuOptions");
								
								JSONArray productCodesArrly = (JSONArray) instancesObj.get("ProductCodes");         		//제품코드
								JSONArray securityGroupsArrly = (JSONArray) instancesObj.get("SecurityGroups");     		//보안그룹
								JSONArray tagsArrly = (JSONArray) instancesObj.get("Tags");                         		//인스턴스 name
								JSONArray blockDeviceMappingsArrly = (JSONArray) instancesObj.get("BlockDeviceMappings"); 	//Block Device
								JSONArray networkInterfacesArrly = (JSONArray) instancesObj.get("NetworkInterfaces");		//Network Interfaces
							
								jsonObj.put("public_IPv4", (String) instancesObj.get("PublicDnsName"));			    		//public dns IPv4
								instantsIdChk = (String) instancesObj.get("InstanceId");
								jsonObj.put("instance_id", instantsIdChk);                									//인스턴트 id - 확인완료 
								jsonObj.put("IPv4_public_ip", (String) instancesObj.get("PublicIpAddress"));        		//IPv4 IP
								jsonObj.put("instance_type", (String) instancesObj.get("InstanceType"));            		//인스턴트 타입
								jsonObj.put("private_dns_name", (String) instancesObj.get("PrivateDnsName"));       		//private_dna_name
								jsonObj.put("private_ip_address", (String) instancesObj.get("PrivateIpAddress"));   		//private_IP
								jsonObj.put("vpc_id", (String) instancesObj.get("VpcId"));									//vpc_id
								jsonObj.put("subnet_id", (String) instancesObj.get("SubnetId"));							//subnet_id
								jsonObj.put("key_name", (String) instancesObj.get("KeyName"));                      		//키이름
								jsonObj.put("ebs_optimized", instancesObj.get("EbsOptimized").toString());					//ebs_optimized
								jsonObj.put("root_device_type", (String) instancesObj.get("RootDeviceType"));       		//루트디바이스유형
								jsonObj.put("root_device_name", (String) instancesObj.get("RootDeviceName"));      			//루트디바이스명
								jsonObj.put("virtualization_type", (String) instancesObj.get("VirtualizationType"));		//가상화
								jsonObj.put("capacity_reservation_id", (String) instancesObj.get("CapacityReservationId"));	//용량예약
								jsonObj.put("ami_launch_index", instancesObj.get("AmiLaunchIndex").toString());				//AMI 시작 인덱스
								jsonObj.put("image_id", (String)instancesObj.get("ImageId"));                				//이미지ID
								
								if (instancesObj.get("SourceDestCheck") != null) {
									jsonObj.put("source_dest_check", instancesObj.get("SourceDestCheck").toString());	//sourceDestCheck
								} else {
									jsonObj.put("source_dest_check", null);
								}
							
					            /* tagsArrly start */
								if (tagsArrly != null) {
									String serviceValue="";

									for (int k = 0; k < tagsArrly.size(); k++) {
										JSONObject tagsArrlyObj = (JSONObject)tagsArrly.get(k);

										if (tagsArrlyObj.get("Key").toString().equals("Name")) {
											serviceValue = tagsArrlyObj.get("Value").toString();
										}

										if (serviceValue.equals("")) {
											if (tagsArrlyObj.get("Key").toString().equals("Service")) {
												serviceValue = (String) tagsArrlyObj.get("Value");
											}
										}
									}
									jsonObj.put("tagsValue", serviceValue);   										//name
								} else {
									jsonObj.put("tagsValue", null);    												//name
								}
					            /* tagsArrly end */
							
					            /* placementObj start */
								jsonObj.put("tenancy", (String) placementObj.get("Tenancy"));  						//Tenancy
								jsonObj.put("availability_zone", (String) placementObj.get("AvailabilityZone"));   	//가용영역
					            /* placementObj end */

					            /* securityGroupsArrly start */
								if (securityGroupsArrly != null) {
									String nameVal = "";

									for (int k = 0; k < securityGroupsArrly.size(); k++) {
										JSONObject securityGroupObj = (JSONObject)securityGroupsArrly.get(k);

							            if (!nameVal.equals("")) {
							            	nameVal += ",";
							            }
						            	
							            nameVal += securityGroupObj.get("GroupName").toString();
									}
						        
									jsonObj.put("security_group", nameVal);
								}
								/* securityGroupsArrly end */

					            /* stateReasonObj start */
								if (stateReasonObj != null) {
									jsonObj.put("stateReason_message", (String) stateReasonObj.get("Message"));   //상태사유
								}
					            /* stateReasonObj end */
							
					            /* networkInterfacesArrly start */
								if (networkInterfacesArrly != null) {
									String nameVal = "";
								
									for (int k = 0; k < networkInterfacesArrly.size(); k++) {
							            if (!nameVal.equals("")) {
							            	nameVal += ",";
							            }
						            	
							            nameVal += "eth" + k;
									}
						        
									jsonObj.put("network_interfaces", nameVal);
								}
								/* networkInterfacesArrly end */
							
								/* start time */
								String LaunchTimeStr = (String) instancesObj.get("LaunchTime");  //시작일자
								
								String dateLocale = "KST";
							    String lang = props.get("lang").toString();
								if (!lang.equals("ko")) {dateLocale = "UTC";}

								SimpleDateFormat old_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // 받은 데이터 형식
						        old_format.setTimeZone(TimeZone.getTimeZone(dateLocale));
						        SimpleDateFormat new_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 바꿀 데이터 형식
						        
						        Date old_date = old_format.parse(LaunchTimeStr);
	
								jsonObj.put("start_time", new_format.format(old_date));
							    /* start time end */
							
								//blockDeviceMappingsArrly
								if (blockDeviceMappingsArrly != null) {
									String blockNameVal = "";

									for (int k = 0; k < blockDeviceMappingsArrly.size(); k++) {
										JSONObject blockDeviceMappingsObj = (JSONObject)blockDeviceMappingsArrly.get(k);

							            if (!blockNameVal.equals("")) {
							            	blockNameVal += "<br/>";
							            }
							            	
							            blockNameVal += blockDeviceMappingsObj.get("DeviceName").toString();
									}

									jsonObj.put("block_device_name", (String) blockNameVal);
								} else {
									jsonObj.put("block_device_name", null);
								}
								//blockDeviceMappingsArrly end
							
								/* monitoringObj start */
								key = monitoringObj.keySet(); 
					            iter = key.iterator(); // Iterator 설정
					            while(iter.hasNext()) {
					            	String keyname = iter.next();

					            	jsonObj.put("monitoring_" + keyname.toLowerCase(), (String) monitoringObj.get(keyname).toString());
					            }
					            /* monitoringObj end */
							
								/* stateObj start */
								key = stateObj.keySet(); 
					            iter = key.iterator(); // Iterator 설정
					            while(iter.hasNext()) {
					            	String keyname = iter.next();					            	
					            	jsonObj.put("instance_status_" + keyname.toLowerCase(), stateObj.get(keyname).toString());
					            }
					            /* stateObj end */
								
								/* cctRevtSpectObj start */
					            if (cctRevtSpectObj != null) {
									jsonObj.put("cct_revt_spect_preference", (String) cctRevtSpectObj.get("CapacityReservationPreference"));
									
									JSONObject cctRevtSpectRetgObj = (JSONObject) cctRevtSpectObj.get("CapacityReservationTarget");
									if (cctRevtSpectRetgObj != null) {
										jsonObj.put("cct_revt_spect_re_id", (String) cctRevtSpectRetgObj.get("CapacityReservationId"));
									}
					            }
					            /* cctRevtSpectObj end */
								
								/* hibernationOptionsObj start */
					            if (hibernationOptionsObj != null) {
									jsonObj.put("hiber_configured", hibernationOptionsObj.get("Configured").toString());
					            }
								/* hibernationOptionsObj end */
								
					            /* productCodesArrly start */
								if (productCodesArrly != null) {
									for (int k = 0; k < productCodesArrly.size(); k++) {
										JSONObject productCodeObj = (JSONObject)productCodesArrly.get(k);

										key = productCodeObj.keySet(); 
							            iter = key.iterator(); // Iterator 설정
							            while(iter.hasNext()) {
							            	String keyname = iter.next();

							            	jsonObj.put(keyname.toLowerCase(), (String) productCodeObj.get(keyname).toString());
							            }
									}
								}
					            /* productCodesArrly end */
								
								/* cpuOptionsObj start */
								if (cpuOptionsObj != null) {
									jsonObj.put("core_count", cpuOptionsObj.get("CoreCount").toString());
									jsonObj.put("threads_perCore", cpuOptionsObj.get("ThreadsPerCore").toString());
								}
								/* cpuOptionsObj end */
							}
						} else {
							jsonObj.put("public_IPv4", null);				//퍼블릭 DNS IPv4
							jsonObj.put("instance_id", null);				//인스턴트 id  확인완료
							jsonObj.put("tagsValue", null);					//name
							jsonObj.put("IPv4_public_ip", null);			//IPv4 IP
							jsonObj.put("instance_type", null);				//인스턴트 타입
							jsonObj.put("private_dns_name", null);			//private_dna_name
							jsonObj.put("availability_zone", null);			//가용영역
							jsonObj.put("private_ip_address", null);		//private_ip_address
							jsonObj.put("vpc_id", null);					//vpc_id
							jsonObj.put("subnet_id", null);					//subnet_id
							jsonObj.put("stateReason_message", null);		//stateReason_message
							jsonObj.put("key_name", null);               	//키이름
							jsonObj.put("security_group", null);         	//보안그룹명
							jsonObj.put("network_interfaces", null);        //network_interfaces
							jsonObj.put("source_dest_check", null);			//source_dest_check
							jsonObj.put("ebs_optimized", null);				//ebs_optimized
							jsonObj.put("root_device_type", null);       	//루트디바이스유형
							jsonObj.put("root_device_name", null);          //루트디바이스이름
							jsonObj.put("start_time", null);              	//시작일자
							jsonObj.put("block_device_name", null);			//블록디바이스이름
							jsonObj.put("virtualization_type", null);		//가상화
							jsonObj.put("capacity_reservation_id", null);	//용량 예약
							jsonObj.put("capacity_reservation_id", null);	//용량 예약
							jsonObj.put("cct_revt_spect_preference", null);	//용량 예약 설정
							jsonObj.put("cct_revt_spect_re_id", null);		//용량 예약 설정
							jsonObj.put("ami_launch_index", null);			//AMI 시작 인덱스
							jsonObj.put("tenancy", null);  					//Tenancy
							jsonObj.put("image_id", null);                	//이미지ID
							jsonObj.put("hiber_configured", null);          //최대절전
							jsonObj.put("core_count", null);		        //core_count
							jsonObj.put("threads_perCore", null);	        //threads_perCore
						}

						if (scaleId != null && !"".equals(scaleId)) {
							if (instantsIdChk.equals(scaleId)) {
								result.add(jsonObj);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * scale 상태조회
	 * 
	 * @param param
	 * @throws Exception
	 */
	@Override
	public Map<String, Object> scaleSetResult(InstanceScaleVO instanceScaleVO) throws Exception {
		Map<String, Object> param = new HashMap<String, Object>();
		Map<String, Object> result = new JSONObject();
		JSONObject scalejsonObj = new JSONObject();
		String scalejsonChk = "";
		String scalejsonStr = "";

		String id = instanceScaleVO.getLogin_id();

		try {
			//scale load 상태보기
			param.put("frst_regr_id", id);
			result = (Map<String, Object>)instanceScaleDAO.selectScaleLog(param);

			//aws도 확인힐요 -- 값자체를 찾아야할듯함
			if (result.isEmpty() || !result.get("wrk_id").toString().equals("1")) {
				Map<String, Object> agentList = null;

				param.put("search_gbn", "scaleChk");
				param.put("scale_set", "");
				param.put("login_id", id);
				param.put("instance_id", "");           //확인완료
				param.put("db_svr_id", instanceScaleVO.getDb_svr_id());
				param.put("process_id", "");
				param.put("monitering", "");

				agentList = scaleInAgent(param);

				if (!agentList.isEmpty()) {
					String resultCode = (String)agentList.get(ClientProtocolID.RESULT_CODE);

					if (resultCode.equals("0")) {
						scalejsonObj = (JSONObject)agentList.get(ClientProtocolID.RESULT_DATA);
						scalejsonChk = (String)agentList.get(ClientProtocolID.RESULT_SUB_DATA);
					}
				}

				if (!scalejsonObj.isEmpty()) {
					scalejsonStr = scalejsonObj.toString();
						
					if (scalejsonStr.contains("pending")) {
						result.put("wrk_id", "1");
						result.put("scale_type", "2");
					} else if (scalejsonStr.contains("shutting-down")) {
						result.put("wrk_id", "1");
						result.put("scale_type", "1");
					} else {
						if (!scalejsonChk.isEmpty()) {
							if (!"0".equals(scalejsonChk)) {
								result.put("wrk_id", "1");
								result.put("scale_type", "1");
							} else {
								result.put("wrk_id", "2");
								result.put("scale_type", "");
							}
						} else {
							result.put("wrk_id", "2");
							result.put("scale_type", "");
						}
					}
				} else {
					if (!scalejsonChk.isEmpty()) {
						if (!"0".equals(scalejsonChk)) {
							result.put("wrk_id", "1");
							result.put("scale_type", "1");
						} else {
							result.put("wrk_id", "2");
							result.put("scale_type", "");
						}
					} else {
						result.put("wrk_id", "2");
						result.put("scale_type", "");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * scale in out
	 * 
	 * @param historyVO, param
	 * @throws Exception
	 */
	@Override
	public Map<String, Object> scaleInOutSet(HistoryVO historyVO, Map<String, Object> param) throws Exception {
		Map<String, Object> result = null;
		JSONObject obj = new JSONObject();

		Map<String, Object> agentList = null;

		try {
			//구분값 : yyyMMddHHmmss
			SimpleDateFormat formatDate = new SimpleDateFormat ( "yyyMMddHHmmss");
			Date time = new Date();
			String timeId = formatDate.format(time);

			param.put("search_gbn", "");
			param.put("instance_id", "");         //확인완료
			param.put("process_id", timeId);
			param.put("monitering", "monitering");
			
			agentList = scaleInAgent(param);
		} catch (Exception e) {
			result.put("RESULT", "FAIL");
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 전송 cmd 값 setting
	 * 
	 * @return String
	 * @throws IOException  
	 * @throws FileNotFoundException 
	 * @throws Exception
	 */
	public String scaleCmdSetting(JSONObject obj) throws FileNotFoundException, IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(ResourceUtils.getFile("classpath:egovframework/tcontrolProps/globals.properties")));	

		String scale_path = "";
		String strCmd = "";
		String strSubCmd = "";

		//구분값 : yyyMMddHHmmss
		SimpleDateFormat formatDate = new SimpleDateFormat ( "yyyMMddHHmmss");
		Date time = new Date();
		String timeId = formatDate.format(time);

		String scaleServer = (String)props.get("scale_server");

		//명령어 setting
		String scaleSet = obj.get("scale_set").toString();
		String searchGbn = obj.get("search_gbn").toString();
		String instanceId = obj.get("instance_id").toString();   //확인완료
		String moniteringGbn = obj.get("monitering").toString();

		if (!moniteringGbn.equals("")) {
			scaleSet = moniteringGbn;
		}

		//scale 실행
		if (!scaleSet.isEmpty()) {
			if ("scaleIn".equals(scaleSet) && "scaleIn".equals(scaleSet)) {
				scale_path = props.get("scale_in_cmd").toString();
				strCmd = String.format(scaleServer + " " + scale_path + " ", timeId);
			} else if ("scaleOut".equals(scaleSet)) {
				scale_path = props.get("scale_out_cmd").toString();
				strCmd = String.format(scaleServer + " " + scale_path + " ", timeId);
			} else if ("monitering".equals(scaleSet)) {
				scale_path = props.get("scale_chk_prgress").toString();
				scaleServer = props.get("scale_seach_server").toString();
				strCmd = String.format(scaleServer + " " + scale_path + " ", timeId);
			}
		} else {
			scaleServer = props.get("scale_seach_server").toString();

			if (searchGbn.equals("scaleChk") && (obj.get("scaleChk_sub") != null && obj.get("scaleChk_sub").toString().equals("Y"))) {
				strSubCmd = props.get("scale_chk_prgress").toString();
				strCmd = String.format(strSubCmd, "scale");
			} else {
				strCmd = props.get("scale_json_view").toString();
				
				if ("main".equals(searchGbn)) {
					strSubCmd = "--query 'sort_by\"(Reservations[*].Instances[].{LaunchTime:LaunchTime, InstanceId:InstanceId, PublicDnsName:PublicDnsName,PublicIpAddress:PublicIpAddress,PrivateIpAddress:PrivateIpAddress,PrivateDnsName:PrivateDnsName,KeyName:KeyName,InstanceType:InstanceType,MonitoringState:Monitoring.State,InstanceStatusName:State.Name, AvailabilityZone:Placement.AvailabilityZone, SecurityGroups:SecurityGroups[], TagsName:Tags[?Key==\\`Name\\`] | [0].Value}[], &LaunchTime)\"'";
					//	strSubCmd = "--query \"sort_by(Reservations[*].Instances[].{LaunchTime:LaunchTime, InstanceId:InstanceId, PublicDnsName:PublicDnsName,PublicIpAddress:PublicIpAddress,PrivateIpAddress:PrivateIpAddress,PrivateDnsName:PrivateDnsName,KeyName:KeyName,InstanceType:InstanceType,MonitoringState:Monitoring.State,InstanceStatusName:State.Name, AvailabilityZone:Placement.AvailabilityZone, SecurityGroups:SecurityGroups[], TagsName:Tags[?Key==\\`Name\\`] | [0].Value}[], &LaunchTime)\"";
				}  else if (searchGbn.equals("scaleChk") && (obj.get("scaleChk_sub") == null || obj.get("scaleChk_sub").toString().equals(""))) {
					strSubCmd = "--query \'\"Reservations[*].Instances[].{InstanceId:InstanceId, InstanceStatusName:State.Name, TagsName:Tags[?Key==\\`Name\\`] | [0].Value}[]\"\'";
				}
				
				strCmd = String.format(strCmd, strSubCmd);

				if (instanceId != null && !"".equals(instanceId)) {
					strCmd = strCmd + " \"Name=instance-id,Values=" + instanceId + "*\"";
				} 

				if (searchGbn.equals("scaleChk") && (obj.get("scaleChk_sub") == null || obj.get("scaleChk_sub").toString().equals(""))) {
					strCmd = strCmd + " \"Name=instance-state-name,Values=pending,shutting-down\"";
				}
				
			}

			strCmd = scaleServer + " " + strCmd;
		}

		return strCmd;
	}

	/**
	 * scale 화면 접속 히스토리 등록
	 * 
	 * @param request, historyVO, dtlCd
	 * @throws Exception
	 */
	@Override
	public void scaleSaveHistory(HttpServletRequest request, HistoryVO historyVO, String dtlCd) throws Exception {
		CmmnUtils.saveHistory(request, historyVO);
		historyVO.setExe_dtl_cd(dtlCd);
		accessHistoryDAO.insertHistory(historyVO);
	}

	/**
	 * scale 보안그룹 상세조회
	 * 
	 * @param instanceScaleVO
	 * @throws FileNotFoundException, IOException, ParseException
	 */
	@Override
	public JSONObject instanceSecurityListSetting(InstanceScaleVO instanceScaleVO) throws FileNotFoundException, IOException, ParseException {
		JSONObject result = new JSONObject();
		JSONArray jsonArray = new JSONArray(); // 객체를 담기위해 JSONArray 선언.
		JSONArray scaleArrly = new JSONArray();
		Set key = null;
		Iterator<String> iter = null;
		String scaleId = (String)instanceScaleVO.getScale_id();

		JSONParser jParser = new JSONParser();
		JSONObject scalejsonObj = new JSONObject();

		Properties props = new Properties();
		props.load(new FileInputStream(ResourceUtils.getFile("classpath:egovframework/tcontrolProps/globals.properties")));		

		String scale_path = props.get("scale_path").toString();
		scale_path = scale_path + "instance_list.json";

		try {
			//나중에는 aws로 변경해야함
			FileReader fileReader = new FileReader(scale_path);
			if (fileReader != null) {
				Object scaleObj = jParser.parse(fileReader);
				scalejsonObj = (JSONObject)scaleObj;
				scaleArrly = (JSONArray) scalejsonObj.get("Reservations");

				if (scaleArrly != null) {
					for (int i = 0; i < scaleArrly.size(); i++) {
						String instantsIdChk = "";
						JSONObject scaleSubObj = (JSONObject)scaleArrly.get(i);
						JSONArray InstancesArrly = (JSONArray) scaleSubObj.get("Instances");

						if (InstancesArrly != null) {
							for (int j = 0; j < InstancesArrly.size(); j++) {
								JSONObject instancesObj = (JSONObject)InstancesArrly.get(j);

								instantsIdChk = (String) instancesObj.get("InstanceId");

								JSONArray securityGroupsArrly = (JSONArray) instancesObj.get("SecurityGroups");     //보안그룹

								/* securityGroupsArrly start */
								if (securityGroupsArrly != null) {
									for (int k = 0; k < securityGroupsArrly.size(); k++) {
										JSONObject jsonObj = new JSONObject();
										JSONObject securityGroupObj = (JSONObject)securityGroupsArrly.get(k);

										jsonObj.put("security_group_nm", securityGroupObj.get("GroupName").toString());
										jsonObj.put("security_group_id", securityGroupObj.get("GroupId").toString());

										if (scaleId != null && !"".equals(scaleId)) {
											if (instantsIdChk.equals(scaleId)) {
												jsonArray.add(jsonObj);
											}
										} else {
											jsonArray.add(jsonObj);
										}
									}
								}
								/* securityGroupsArrly end */
							}
						}
					}

					result.put("data", jsonArray);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * scale log search
	 * 
	 * @param instanceScaleVO
	 * @throws Exception 
	 */
	@Override
	public Map<String, Object> selectScaleLog(Map<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		return (Map<String, Object>) instanceScaleDAO.selectScaleLog(param);
	}

	/**
	 * 현재시간 조회
	 * 
	 * @return String
	 * @throws Exception
	 */
	public static String nowTime(){
		Calendar calendar = Calendar.getInstance();
		java.util.Date date = calendar.getTime();
		String today = (new SimpleDateFormat("yyyyMMddHHmmss").format(date));
		return today;
	}

	/**
	 * scale agent 전송
	 * 
	 * @param instanceScaleVO
	 * @throws Exception 
	 */
	public Map<String, Object> scaleInAgent(Map<String, Object> param) throws Exception {
		Map<String, Object> result = null;
		List<Map<String, Object>> dbResult = null;
		JSONObject obj = new JSONObject();
		
		int db_svr_id = Integer.parseInt(param.get("db_svr_id").toString());
		String scale_set = param.get("scale_set").toString();

		try {
			obj.put("scale_set", scale_set);
			obj.put("login_id", param.get("login_id").toString());
			obj.put("instance_id", param.get("instance_id").toString());  //확인완료
			obj.put("search_gbn", param.get("search_gbn").toString());
			obj.put("process_id", param.get("process_id").toString());
			obj.put("db_svr_id", db_svr_id);
			obj.put("monitering", "");
			obj.put("scaleChk_sub", "");

			if (!scale_set.equals("")) {
				dbResult = instanceScaleDAO.selectSvrIpadrList(db_svr_id);
				if (!dbResult.isEmpty()) {
					obj.put("db_svr_ipadr_id", dbResult.get(0).get("db_svr_ipadr_id"));
				} else {
					obj.put("db_svr_ipadr_id", 1);
				}
			} else {
				obj.put("db_svr_ipadr_id", 1);
			}

			/* db 서버 조회 */
			DbServerVO schDbServerVO = new DbServerVO();
			schDbServerVO.setDb_svr_id(db_svr_id);
			DbServerVO dbServerVO = (DbServerVO) cmmnServerInfoDAO.selectServerInfo(schDbServerVO);

			/* agnet 정보조회  */
			String strIpAdr = dbServerVO.getIpadr();
			AgentInfoVO vo = new AgentInfoVO();
			vo.setIPADR(strIpAdr);
			AgentInfoVO agentInfo = (AgentInfoVO) cmmnServerInfoDAO.selectAgentInfo(vo);

			String IP = dbServerVO.getIpadr();
			int PORT = agentInfo.getSOCKET_PORT();

			//cmd 값 셋팅
			String agentCmd = scaleCmdSetting(obj);

			String agentSubCmd = "";
			if (param.get("monitering") != null || param.get("search_gbn").toString().equals("scaleChk")) {
				obj.put("monitering", param.get("monitering").toString());
				obj.put("scaleChk_sub", "Y");
				agentSubCmd = scaleCmdSetting(obj);
			}

			ClientInfoCmmn cic = new ClientInfoCmmn();
			result = cic.scale_exec_call(IP, PORT, agentCmd, agentSubCmd, obj);

			System.out.println("결과");
			System.out.println(result.get("RESULT_CODE"));
			System.out.println(result.get("ERR_CODE"));
			System.out.println(result.get("ERR_MSG"));

		} catch (Exception e) {
			result.put("RESULT", "FAIL");
			e.printStackTrace();
		}
		return result;
	}
}