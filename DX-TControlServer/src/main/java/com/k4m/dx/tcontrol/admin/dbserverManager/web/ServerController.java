package com.k4m.dx.tcontrol.admin.dbserverManager.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.k4m.dx.tcontrol.admin.dbserverManager.service.DbServerManagerService;
import com.k4m.dx.tcontrol.admin.dbserverManager.service.DbServerVO;
import com.k4m.dx.tcontrol.admin.menuauthority.service.MenuAuthorityService;
import com.k4m.dx.tcontrol.cmmn.CmmnUtils;

/**
 * [DB SERVER] 컨트롤러 클래스를 정의한다.
 *
 * @author 변승우
 * @see
 * 
 *      <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일       수정자           수정내용
 *  -------     --------    ---------------------------
 *  2017.05.31   변승우 최초 생성
 *      </pre>
 */

@Controller
public class ServerController {
	
	@Autowired
	private MenuAuthorityService menuAuthorityService;

	@Autowired
	private DbServerManagerService dbServerManagerService;
	
	private List<Map<String, Object>> menuAut;
	
	/**
	 * [DB SERVER] DB 서버 화면을 보여준다.
	 * 
	 * @param historyVO
	 * @param request
	 * @return ModelAndView mv
	 * @throws Exception
	 */
	@RequestMapping(value = "/dbServer.do")
	public ModelAndView dbServer(HttpServletRequest request) {
		
		CmmnUtils cu = new CmmnUtils();
		menuAut = cu.selectMenuAut(menuAuthorityService, "16");
		
		ModelAndView mv = new ModelAndView();
		try {		
			mv.addObject("read_aut_yn", menuAut.get(0).get("read_aut_yn"));
			mv.addObject("wrt_aut_yn", menuAut.get(0).get("wrt_aut_yn"));
			mv.setViewName("admin/dbServerManager/dbServer");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mv;
	}
	
	
	
	/**
	 * [DB SERVER] DB서버 리스트를 조회한다.
	 * 
	 * @return resultSet
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	@RequestMapping(value = "/selectDbServerServerList.do")
	@ResponseBody
	public List<DbServerVO> selectDbServerServerList(@ModelAttribute("dbServerVO") DbServerVO dbServerVO) {
	
		CmmnUtils cu = new CmmnUtils();
		menuAut = cu.selectMenuAut(menuAuthorityService, "16");
		
		List<DbServerVO> result = null;
		List<DbServerVO> resultSet = null;
		try {
			
			System.out.println("=======parameter=======");
			System.out.println("서버명 : " + dbServerVO.getDb_svr_id());
			System.out.println("서버명 : " + dbServerVO.getDb_svr_nm());
			System.out.println("아이피 : " + dbServerVO.getIpadr());
			System.out.println("Database : " + dbServerVO.getDft_db_nm());
			System.out.println("=====================");
			
			//읽기권한이 있을경우
			//if(menuAut.get(0).get("read_aut_yn").equals("Y")){
				resultSet = dbServerManagerService.selectDbServerList(dbServerVO);
			//}else{
			//	return resultSet;
			//}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultSet;
	}
	
}