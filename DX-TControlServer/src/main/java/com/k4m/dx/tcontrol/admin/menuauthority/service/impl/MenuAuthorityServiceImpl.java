package com.k4m.dx.tcontrol.admin.menuauthority.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.k4m.dx.tcontrol.admin.menuauthority.service.MenuAuthorityService;
import com.k4m.dx.tcontrol.admin.menuauthority.service.MenuAuthorityVO;
import com.k4m.dx.tcontrol.login.service.UserVO;


@Service("menuAuthorityService")
public class MenuAuthorityServiceImpl implements MenuAuthorityService{
	
	@Resource(name = "MenuAuthorityDAO")
	private MenuAuthorityDAO menuAuthorityDAO;

	
	/**
	 * 사용자 메뉴권한 조회
	 * @param 
	 * @throws Exception
	 */
	@Override
	public List<MenuAuthorityVO> selectUsrmnuautList(MenuAuthorityVO menuAuthorityVO) throws Exception {
		return menuAuthorityDAO.selectUsrmnuautList(menuAuthorityVO);
	}

	
	/**
	 * 사용자 메뉴ID 조회
	 * @param 
	 * @throws Exception
	 */
	@Override
	public List<UserVO> selectMnuIdList() throws Exception {
		return menuAuthorityDAO.selectMnuIdList();
	}
	
	
	/**
	 * 사용자 등록시 메뉴권한 등록
	 * @param 
	 * @throws Exception
	 */
	@Override
	public void insertUsrmnuaut(UserVO userVo) throws Exception {
		menuAuthorityDAO.insertUsrmnuaut(userVo);		
	}


	/**
	 * 사용자 메뉴권한 수정
	 * @param 
	 * @throws Exception
	 */
	@Override
	public void updateUsrMnuAut(Object object) throws Exception {
		menuAuthorityDAO.updateUsrMnuAut(object);	
		
	}


	/**
	 * 사용자 화면권한 조회
	 * @param 
	 * @throws Exception
	 */
	@Override
	public List<Map<String, Object>> selectMenuAut(Map<String, Object> param) throws Exception {
		return menuAuthorityDAO.selectMenuAut(param);
	}



}
