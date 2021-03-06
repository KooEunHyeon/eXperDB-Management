package com.k4m.dx.tcontrol.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.k4m.dx.tcontrol.socket.ProtocolID;
import com.k4m.dx.tcontrol.socket.SocketCtl;
import com.k4m.dx.tcontrol.socket.TranCodeType;
import com.k4m.dx.tcontrol.util.DateUtil;
import com.k4m.dx.tcontrol.util.FileEntry;
import com.k4m.dx.tcontrol.util.FileListSearcher;
import com.k4m.dx.tcontrol.util.FileUtil;

/**
 * kafka-connect CRUD
 *
 * @author 박태혁
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일       수정자           수정내용
 *  -------     --------    ---------------------------
 *  2017.05.22   박태혁 최초 생성
 * </pre>
 */

public class DxT015 extends SocketCtl{
	
	private Logger errLogger = LoggerFactory.getLogger("errorToFile");
	private Logger socketLogger = LoggerFactory.getLogger("socketLogger");
	
	public DxT015(Socket socket, BufferedInputStream is, BufferedOutputStream	os) {
		this.client = socket;
		this.is = is;
		this.os = os;
	}

	public void execute(String strDxExCode, JSONObject jObj) throws Exception {
		
		socketLogger.info("DxT015.execute : " + strDxExCode);
		byte[] sendBuff = null;
		String strErrCode = "";
		String strErrMsg = "";
		String strSuccessCode = "0";

		String strCommandCode = (String) jObj.get(ProtocolID.COMMAND_CODE);
		String strLogFileDir = (String) jObj.get(ProtocolID.FILE_DIRECTORY);
		

		List<Map<String, Object>> outputArray = new ArrayList<Map<String, Object>>();
		

		
		JSONObject outputObj = new JSONObject();
		
		//strLogFileDir = "/home/devel/experdb/data/pg_log"
		//socketLogger.info("File Dir : " + strLogFileDir);
		
		try {

			FileListSearcher fs = new FileListSearcher(strLogFileDir);
			
			if(strCommandCode.equals(ProtocolID.COMMAND_CODE_R)) {
				
				JSONObject searchInfoObj = (JSONObject) jObj.get(ProtocolID.SEARCH_INFO);
				String strFrom = (String) searchInfoObj.get(ProtocolID.START_DATE);
				String strTo = (String) searchInfoObj.get(ProtocolID.END_DATE);
				
				Date dtFrom = DateUtil.getDateToString(strFrom);
				Date dtTo = DateUtil.getDateToString(strTo);

				List<HashMap<String, String>> resultFileList = new ArrayList<HashMap<String, String>>();
				
				List<FileEntry> fileList = fs.getSearchFiles();
				
				Collections.sort(fileList, new CompareNameDesc());
				
				for(FileEntry fn: fileList) {
					HashMap<String, String> hp = new HashMap<String, String>();
					
					String strFileName = fn.getFileName();
					String strFileSize = FileUtil.getFileSize(fn.getFileSize(), 2);
					String strLastModified = FileUtil.getFileLastModifiedDate(fn.getLastModified());
					
					Date dtLastModified = DateUtil.getDateToString(strLastModified);
					
					String strExtender = FileUtil.fileExtenderSubString(strFileName);
					
					if(strExtender.equals("log")) {
						if(((dtLastModified.compareTo(dtFrom) > 0) || (dtLastModified.compareTo(dtFrom) == 0))
								&&((dtLastModified.compareTo(dtTo) < 0) || (dtLastModified.compareTo(dtTo) == 0))) {
							hp.put(ProtocolID.FILE_NAME, strFileName);
							hp.put(ProtocolID.FILE_SIZE, strFileSize);
							hp.put(ProtocolID.FILE_LASTMODIFIED, strLastModified);
							
							//socketLogger.info("File Name : " + strFileName);
							
							resultFileList.add(hp);
						}
					}
					
					hp = null;
					
					//socketLogger.info("File Name after : " + resultFileList.get(0).get(ProtocolID.FILE_NAME));
					
				}
				
				
				
				outputObj.put(ProtocolID.DX_EX_CODE, strDxExCode);
				outputObj.put(ProtocolID.RESULT_CODE, strSuccessCode);
				outputObj.put(ProtocolID.ERR_CODE, strErrCode);
				outputObj.put(ProtocolID.ERR_MSG, strErrMsg);
				outputObj.put(ProtocolID.RESULT_DATA, resultFileList);
				
				sendBuff = outputObj.toString().getBytes();
				send(4, sendBuff);
				
				resultFileList = null;
				
				
			} else if(strCommandCode.equals(ProtocolID.COMMAND_CODE_V)) {
				
				String strReadLine = (String) jObj.get(ProtocolID.READLINE);
				String strSeek = (String) jObj.get(ProtocolID.SEEK);
				String dwLen = (String) jObj.get(ProtocolID.DW_LEN);
				
				int intDwlen = Integer.parseInt(dwLen);
				int intReadLine = Integer.parseInt(strReadLine);
				
				int intLastLine = intDwlen;
				
				String strFileName = (String) jObj.get(ProtocolID.FILE_NAME);
				//strLogFileDir = "C:\\logs\\";
				//strFileName = "webconsole.log.2017-05-31";
				File inFile = new File(strLogFileDir, strFileName);
				
				//byte[] buffer = FileUtil.getFileToByte(inFile);
				//HashMap hp = FileUtil.getFileView(inFile, Integer.parseInt(startLen), Integer.parseInt(dwLen));
				//HashMap hp = FileUtil.getRandomAccessFileView(inFile, Integer.parseInt(startLen), Integer.parseInt(dwLen));
				HashMap hp = FileUtil.getRandomAccessFileView(inFile, Integer.parseInt(strReadLine), Integer.parseInt(strSeek), intLastLine);
				
				outputObj.put(ProtocolID.DX_EX_CODE, strDxExCode);
				outputObj.put(ProtocolID.RESULT_CODE, strSuccessCode);
				outputObj.put(ProtocolID.ERR_CODE, strErrCode);
				outputObj.put(ProtocolID.ERR_MSG, strErrMsg);
				outputObj.put(ProtocolID.RESULT_DATA, hp.get("file_desc"));
				outputObj.put(ProtocolID.FILE_SIZE, hp.get("file_size"));
				outputObj.put(ProtocolID.SEEK, hp.get("seek"));
				outputObj.put(ProtocolID.DW_LEN, intLastLine + Integer.parseInt(strReadLine));
				outputObj.put(ProtocolID.END_FLAG, hp.get("end_flag"));
				
				hp = null;
				inFile = null;
				
				send(outputObj);
			} else if(strCommandCode.equals(ProtocolID.COMMAND_CODE_DL)) {
				String strFileName = (String) jObj.get(ProtocolID.FILE_NAME);
				File inFile = new File(strLogFileDir, strFileName);
				if(inFile.exists()) {
					send(inFile);
				} else {
					outputObj.put(ProtocolID.DX_EX_CODE, TranCodeType.DxT015_DL);
					outputObj.put(ProtocolID.RESULT_CODE, "1");
					outputObj.put(ProtocolID.ERR_CODE, TranCodeType.DxT015_DL);
					outputObj.put(ProtocolID.ERR_MSG, "DxT015_DL Error [FIle Not Found Error]");
					
					sendBuff = outputObj.toString().getBytes();
					send(4, sendBuff);
				}
			}
			
			//send(TotalLengthBit, outputObj.toString().getBytes());
			
		} catch (Exception e) {
			errLogger.error("DxT015 {} ", e.toString());
			
			outputObj.put(ProtocolID.DX_EX_CODE, TranCodeType.DxT015);
			outputObj.put(ProtocolID.RESULT_CODE, "1");
			outputObj.put(ProtocolID.ERR_CODE, TranCodeType.DxT015);
			outputObj.put(ProtocolID.ERR_MSG, "DxT015 Error [" + e.toString() + "]");
			
			sendBuff = outputObj.toString().getBytes();
			send(4, sendBuff);

		} finally {
			outputObj = null;
			sendBuff = null;
		}	    
	}
	
	  /**
     * String으로 내림차순(Desc) 정렬
     * @author Administrator
     *
     */
    static class CompareNameDesc implements Comparator<FileEntry>{
 
        @Override
        public int compare(FileEntry o1, FileEntry o2) {
            // TODO Auto-generated method stub
            return o2.getFileName().compareTo(o1.getFileName());
        }        
    }
    
    /**
     * String으로 오름차순(Asc) 정렬
     * @author Administrator
     *
     */
    static class CompareNameAsc implements Comparator<FileEntry>{
 
        @Override
        public int compare(FileEntry o1, FileEntry o2) {
            // TODO Auto-generated method stub
            return o1.getFileName().compareTo(o2.getFileName());
        }        
    }
    
    /**
     * int로 내림차순(Desc) 정렬
     * @author Administrator
     *
     */
    static class CompareSeqDesc implements Comparator<FileEntry>{
 
        @Override
        public int compare(FileEntry o1, FileEntry o2) {
            // TODO Auto-generated method stub
            return o1.getLastModified() > o2.getLastModified() ? -1 : o1.getLastModified() < o2.getLastModified() ? 1:0;
        }        
    }
    
    /**
     * int로 오름차순(Asc) 정렬
     * @author Administrator
     *
     */
    static class CompareSeqAsc implements Comparator<FileEntry>{
 
        @Override
        public int compare(FileEntry o1, FileEntry o2) {
            // TODO Auto-generated method stub
            return o1.getLastModified() < o2.getLastModified() ? -1 : o1.getLastModified() > o2.getLastModified() ? 1:0;
        }        
    }
    
    public static void main(String[] args) throws Exception {
		String strLogFileDir = "C:\\logs\\";
		String strFileName = "webconsole.log.2017-05-31";
		String startLen = "100";
		String seek = "0";
		
		File inFile = new File(strLogFileDir, strFileName);
		
		//byte[] buffer = FileUtil.getFileToByte(inFile);
		//HashMap hp = FileUtil.getFileView(inFile, Integer.parseInt(startLen), Integer.parseInt(dwLen));
		HashMap hp = FileUtil.getRandomAccessFileView(inFile, Integer.parseInt(startLen), Integer.parseInt(seek), 0);
		
		System.out.println(hp.get("file_desc"));
		System.out.println(hp.get("file_size"));
		System.out.println(hp.get("seek"));
		System.out.println(hp.get("end_flag"));
    }

}


