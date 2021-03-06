<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="ui" uri="http://egovframework.gov/ctl/ui"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@include file="../../cmmn/cs.jsp"%>
<%
	/**
	* @Class Name : backupRestoreAuditLog.jsp
	* @Description : backupRestoreAuditLog 화면
	* @Modification Information
	*
	*   수정일         수정자                   수정내용
	*  ------------    -----------    ---------------------------
	*  2018.01.09     최초 생성
	*
	* author 김주영 사원
	* since 2018.01.09
	*
	*/
%>
<script>
	var table = null;

	function fn_init() {
		table = $('#table').DataTable({
			scrollY : "310px",
			searching : false,
			deferRender : true,
			scrollX: true,
			columns : [
				{ data : "", defaultContent : "", targets : 0, orderable : false, checkboxes : {'selectRow' : true}}, 
				{ data : "rnum", defaultContent : ""}, 
				{ data : "logDateTime", defaultContent : ""}, 
				{ data : "entityName", defaultContent : ""}, 
				{ data : "remoteAddress", defaultContent : ""}, 
				{ data : "serverAddress", defaultContent : ""}, 
				{ data : "backupWorkType", defaultContent : ""}, 
				{ data : "backupType", defaultContent : ""}, 
				{ data : "logDateTimeFrom",  defaultContent : ""}, 
				{ data : "logDateTimeTo",  defaultContent : ""}, 
				{ data : "containsCryptoKey",  defaultContent : ""}, 
				{ data : "containsPolicy",  defaultContent : ""}, 
				{ data : "containsServer",  defaultContent : ""}, 
				{ data : "containsAdminUser",  defaultContent : ""}, 
				{ data : "containsConfig",  defaultContent : ""}, 
				{ data : "containsCoreLog",  defaultContent : ""}, 
				{ data : "containsSiteLog",  defaultContent : ""}, 
				{ data : "containsBackupLog",  defaultContent : ""}, 
				{ data : "containsSystemUsageLog",  defaultContent : ""}, 
				{ data : "containsSystemStatusLog",  defaultContent : ""}, 
				{ data : "containsTableCryptLog",  defaultContent : ""},
				{ data : "filePath",  defaultContent : ""}
	
			 ],'select': {'style': 'multi'}
		});
		
		table.tables().header().to$().find('th:eq(0)').css('min-width', '10px');
		table.tables().header().to$().find('th:eq(1)').css('min-width', '30px');
		table.tables().header().to$().find('th:eq(2)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(3)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(4)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(5)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(6)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(7)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(8)').css('min-width', '150px');
		table.tables().header().to$().find('th:eq(9)').css('min-width', '150px');
		table.tables().header().to$().find('th:eq(10)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(11)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(12)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(13)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(14)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(15)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(16)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(17)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(18)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(19)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(20)').css('min-width', '100px');
		table.tables().header().to$().find('th:eq(21)').css('min-width', '500px');
		
	    $(window).trigger('resize');
	    
	}
	
	$(window.document).ready(function() {
		var dateFormat = "yyyy-mm-dd", from = $("#from").datepicker({
			changeMonth : false,
			changeYear : false,
			onClose : function(selectedDate) {
				$("#to").datepicker("option", "minDate", selectedDate);
			}
		})

		to = $("#to").datepicker({
			changeMonth : false,
			changeYear : false,
			onClose : function(selectedDate) {
				$("#from").datepicker("option", "maxDate", selectedDate);
			}
		})
		
		$('#from').val($.datepicker.formatDate('yy-mm-dd', new Date()));
		$('#to').val($.datepicker.formatDate('yy-mm-dd', new Date()));
		
		fn_init();
		
		$.ajax({
			url : "/selectBackupRestoreAuditLog.do",
			data : {
				from : $('#from').val(),
				to : 	$('#to').val(),
				worktype : $('#worktype').val(),
				entityuid : $('#entityuid').val()
			},
			dataType : "json",
			type : "post",
			beforeSend: function(xhr) {
		        xhr.setRequestHeader("AJAX", true);
		     },
			error : function(xhr, status, error) {
				if(xhr.status == 401) {
					alert("<spring:message code='message.msg02' />");
					top.location.href = "/";
				} else if(xhr.status == 403) {
					alert("<spring:message code='message.msg03' />");
					top.location.href = "/";
				} else {
					alert("ERROR CODE : "+ xhr.status+ "\n\n"+ "ERROR Message : "+ error+ "\n\n"+ "Error Detail : "+ xhr.responseText.replace(/(<([^>]+)>)/gi, ""));
				}
			},
			success : function(result) {
				table.clear().draw();
				if(result.data!=null){
					table.rows.add(result.data).draw();
				}
			}
		});
	});
	

	/* 조회 버튼 클릭시*/
	function fn_select() {
		$.ajax({
			url : "/selectBackupRestoreAuditLog.do",
			data : {
				from : $('#from').val(),
				to : 	$('#to').val(),
				worktype : $('#worktype').val(),
				entityuid : $('#entityuid').val()
			},
			dataType : "json",
			type : "post",
			beforeSend: function(xhr) {
		        xhr.setRequestHeader("AJAX", true);
		     },
			error : function(xhr, status, error) {
				if(xhr.status == 401) {
					alert("<spring:message code='message.msg02' />");
					top.location.href = "/";
				} else if(xhr.status == 403) {
					alert("<spring:message code='message.msg03' />");
					top.location.href = "/";
				} else {
					alert("ERROR CODE : "+ xhr.status+ "\n\n"+ "ERROR Message : "+ error+ "\n\n"+ "Error Detail : "+ xhr.responseText.replace(/(<([^>]+)>)/gi, ""));
				}
			},
			success : function(result) {
				table.clear().draw();
				if(result.data!=null){
					table.rows.add(result.data).draw();
				}
			}
		});

	}
	
	/* 다운로드 버튼 클릭시*/
	function fn_download() {

	}
	
	/* 삭제 버튼 클릭시*/
	function fn_delete() {

	}
	

</script>
<!-- contents -->
<div id="contents">
	<div class="contents_wrap">
		<div class="contents_tit">
			<h4>백업및복원<a href="#n"><img src="../images/ico_tit.png" class="btn_info" /></a></h4>
			<div class="infobox">
				<ul>
					<li>백업및복원설명</li>
				</ul>
			</div>
			<div class="location">
				<ul>
					<li>데이터암호화</li>
					<li>감사로그</li>
					<li class="on">백업및복원</li>
				</ul>
			</div>
		</div>
		<div class="contents">
			<div class="cmm_grp">
				<div class="btn_type_01">
					<span class="btn" onclick="fn_select();"><button type="button">조회</button></span>
					<span class="btn" onclick="fn_download();"><button type="button">다운로드</button></span>
					<span class="btn" onclick="fn_delete();"><button type="button">삭제</button></span>
				</div>
				<div class="sch_form">
					<table class="write">
						<caption>검색 조회</caption>
						<colgroup>
							<col style="width: 100px;" />
							<col style="width: 300px;" />
							<col style="width: 100px;" />
							</col>
						</colgroup>
						<tbody>
							<tr>
								<th scope="row" class="t10">로그기간</th>
								<td>
									<div class="calendar_area">
										<a href="#n" class="calendar_btn">달력열기</a> 
										<input type="text" class="calendar" id="from" name="lgi_dtm_start" title="기간검색 시작날짜" readonly="readonly" /> <span class="wave">~</span>
										<a href="#n" class="calendar_btn">달력열기</a> 
										<input type="text" class="calendar" id="to" name="lgi_dtm_end" title="기간검색 종료날짜" readonly="readonly" />
									</div>
								</td>
							</tr>
							<tr>
								<th scope="row" class="t9">접근자</th>
								<td>
									<select class="select t5" id="entityuid">
										<option value="">전체</option>
											<c:forEach var="entityuid" items="${entityuid}">
												<option value="${entityuid.getEntityUid}">${entityuid.getEntityName}</option>							
											</c:forEach>
									</select>
								</td>
								<th scope="row" class="t9">작업구분</th>
								<td>
									<select class="select t5" id="worktype">
										<option value="">전체</option>
										<option value="BACKUP">BACKUP</option>
										<option value="SCHEDULED_BACKUP">SCHEDULED BACKUP</option>
										<option value="RESTORE">RESTORE</option>
									</select>
								</td>
							</tr>
						</tbody>
					</table>
				</div>

				<div class="overflow_area">
					<table id="table" class="display" cellspacing="0" width="100%">
						<thead>
							<tr>
								<th width="10"></th>
								<th width="30">NO</th>
								<th width="100">작업일시</th>
								<th width="100">작업자</th>
								<th width="100">작업자 주소</th>
								<th width="100">서버 주소</th>
								<th width="100">작업구분</th>
								<th width="100">백업구분</th>
								<th width="150">로그시작일시</th>
								<th width="150">로그종료일시</th>
								<th width="100">암호화 키</th>
								<th width="100">정책</th>
								<th width="100">서버</th>
								<th width="100">eXperDB사용자</th>
								<th width="100">환경설정</th>
								<th width="100">관리로그</th>
								<th width="100">사이트로그</th>
								<th width="100">백업로그</th>
								<th width="100">자원 사용 로그</th>
								<th width="100">상태로그</th>
								<th width="100">태이블 암호화 로그</th>
								<th width="500">파일경로</th>
							</tr>
						</thead>
					</table>
				</div>
			</div>
		</div>
	</div>
</div>
<!-- // contents -->
