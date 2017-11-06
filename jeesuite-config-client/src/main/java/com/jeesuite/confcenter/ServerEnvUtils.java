package com.jeesuite.confcenter;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.commons.lang3.StringUtils;


public class ServerEnvUtils {

	private static final String UNKNOW = "unknow";

	private static String serverIpAddr = getServerIpAddr();
	
	private static String serverPort = getServerPort();

	public static String getServerIpAddr()  {  
		if(serverIpAddr != null)return serverIpAddr;
		try {			
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();  
			outter:while (en.hasMoreElements()) {  
				NetworkInterface i = en.nextElement();  
				for (Enumeration<InetAddress> en2 = i.getInetAddresses(); en2.hasMoreElements();) {  
					InetAddress addr = en2.nextElement();  
					if (!addr.isLoopbackAddress()) {  
						if (addr instanceof Inet4Address) {    
							serverIpAddr = addr.getHostAddress();  
							break outter;
						}  
					}  
				}  
			}  
		} catch (Exception e) {
			serverIpAddr = UNKNOW;
		}
	    return serverIpAddr;  
	} 
	
	
	public static String getServerPort(){
		if(serverPort != null)return serverPort;
		serverPort = System.getProperty("server.port");
		if(StringUtils.isBlank(serverPort)){
			serverPort = System.getProperty("dubbo.port");
		}
		if(StringUtils.isBlank(serverPort)){
			serverPort = getTomcatServerPortByMBean();
		}
		return UNKNOW;
	}
	
	private static String getTomcatServerPortByMBean() {
		String sport = null;
		try {
			MBeanServer mBeanServer = null;
			ArrayList<MBeanServer> mBeanServers = MBeanServerFactory.findMBeanServer(null);
			if (mBeanServers.size() > 0) {
				for (MBeanServer _mBeanServer : mBeanServers) {
					mBeanServer = _mBeanServer;
					break;
				}
			}
			if (mBeanServer == null) {
				return null;
			}
			Set<ObjectName> objectNames = mBeanServer.queryNames(new ObjectName("Catalina:type=Connector,*"), null);
			for (ObjectName objectName : objectNames) {
				String protocol = (String) mBeanServer.getAttribute(objectName, "protocol");
				if (protocol.equals("HTTP/1.1")) {
					int port = (Integer) mBeanServer.getAttribute(objectName, "port");
					sport=String.valueOf(port);
				}
			}
		} catch (Exception e) {}
		return sport;
	}
}
