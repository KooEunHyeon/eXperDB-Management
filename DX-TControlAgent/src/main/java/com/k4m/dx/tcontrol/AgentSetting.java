package com.k4m.dx.tcontrol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Scanner;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class AgentSetting {
	
	public static void main(String[] args) throws Exception {
		
		/**
		 * 1. database.url
		 * 2. database.username
		 * 3. database.password
		 * 4. socket.server.port
		 * 5. agent.install.ip
		 */
		String strDatabaseIp = "";
		String strDatabasePort = "";
		String strDatabaseName = "";
		String strDatabaseUrl = "";
		
		String strDatabaseUsername = "";
		String strDatabasePassword = "";
		String strAgentIp = "";
		String strAgentPort = "";
		
		
		
		Scanner scan = new Scanner(System.in);
		
		System.out.println("database IP 를  입력하세요:");
		
		strDatabaseIp = scan.nextLine();
		
		System.out.println("database Port 를  입력하세요:");
		
		strDatabasePort = scan.nextLine();
		
		System.out.println("database Name 를  입력하세요:");
		
		strDatabaseName = scan.nextLine();
		
		System.out.println("database.username 을  입력하세요:");
		
		strDatabaseUsername = scan.nextLine();
		
		System.out.println("database.password 을  입력하세요:");
		
		strDatabasePassword = scan.nextLine();
		
		System.out.println("Agent IP 를  입력하세요:");
		
		strAgentIp = scan.nextLine();
		
		System.out.println("Agent port 를  입력하세요:");
		
		strAgentPort = scan.nextLine();
		
		strDatabaseUrl = "jdbc:postgresql://" + strDatabaseIp + ":" + strDatabasePort + "/" + strDatabaseName;
		
		System.out.println("#####################################################");
		System.out.println("database 접속정보 :" + strDatabaseUrl);
		System.out.println("database.username :" + strDatabaseUsername);
		System.out.println("database.password :" + strDatabasePassword);
		System.out.println("Agent IP :" + strAgentIp);
		System.out.println("Agent port :" + strAgentPort);
		System.out.println("#####################################################");
		
		System.out.println("입력한 내용으로 적용하시겠습니까? (y, n)");
		
		String strApply = scan.nextLine();
		
		if(strApply.equals("y")) {
			
		    StandardPBEStringEncryptor pbeEnc = new StandardPBEStringEncryptor();
		    pbeEnc.setPassword("k4mda"); // PBE 값(XML PASSWORD설정)
			
		    String url = pbeEnc.encrypt(strDatabaseUrl);
		    String username = pbeEnc.encrypt(strDatabaseUsername);
		    String password = pbeEnc.encrypt(strDatabasePassword);
		    
		    Properties prop = new Properties();
		    
		    ClassLoader loader = Thread.currentThread().getContextClassLoader();
		    File file = new File(loader.getResource("context.properties").getFile());
		    
		    String path = file.getParent() + File.separator;
		    
		   // System.out.println(path);
		    
		    try {
		    	prop.load(new FileInputStream(path + "context.properties"));
		    } catch(FileNotFoundException e) {
		    	System.out.println("Exit(0) File Not Found ");
		    	System.exit(0);
		    } catch(Exception e) {
		    	System.out.println("Exit(0) Error : " + e.toString());
		    	System.exit(0);
		    }
		    
		    prop.setProperty("database.url", "ENC(" + url + ")");
		    prop.setProperty("database.username", "ENC(" + username + ")");
		    prop.setProperty("database.password", "ENC(" + password + ")");
		    
		    prop.setProperty("socket.server.port", strAgentPort);
		    prop.setProperty("agent.install.ip", strAgentIp);
		    
		    try {
		    	prop.store(new FileOutputStream(path + "context.properties"), "");
		    } catch(FileNotFoundException e) {
		    	System.out.println("Exit(0) File Not Found ");
		    	System.exit(0);
		    } catch(Exception e) {
		    	System.out.println("Exit(0) Error : " + e.toString());
		    	System.exit(0);
		    }

		    System.out.println("#### Agent Setting success !! #####");
		} else {
			System.out.println("#### Exit(0) Cancel Agent Setting #####");
		}
		



	}
}