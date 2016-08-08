/**
 * created by haitao.yao @ May 11, 2011
 */
package com.simple.kv.storage.cmem.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

/**
 * config files for key-value storage
 * 
 * @author haitao.yao @ May 11, 2011
 * 
 */

@Root(name = "kv-cmem-conf")
public class KVStorageConfig implements Serializable {

	/**  */
    private static final long serialVersionUID = 8065719388526529609L;


    @ElementList(name = "instance", inline = true)
	private List<KVStorageDescriptor> kvsDescriptors = new ArrayList<KVStorageDescriptor>(); 
	
    private Map<String, KVStorageDescriptor> kvsMap = new HashMap<String, KVStorageDescriptor>();
    
    @Commit
    public void after() {
        if (!kvsDescriptors.isEmpty()) {
            Map<String, KVStorageDescriptor> tmpMap = new HashMap<String, KVStorageDescriptor>();
            for (KVStorageDescriptor kvsDescriptor : kvsDescriptors) {
                tmpMap.put(kvsDescriptor.getName(), kvsDescriptor);
            }
            kvsMap = tmpMap;
        }
    }
    
    public List<KVStorageDescriptor> getKvsDescriptors() {
        return kvsDescriptors;
    }
    
    public KVStorageDescriptor getSpecialKvsDescriptor(String name) {
        return kvsMap.get(name);
    }
    
}

@Root(name = "instance")
class KVStorageDescriptor {

    private static final int DEFAULT_CONNECTION_COUNT = 10;
    
    @Attribute
    private String name;
    
    @ElementList(name = "servers")
    private List<Server> servers;

    private String[] serversArray;

    @Element(name = "init-connection", required = false)
    private int initConnection = DEFAULT_CONNECTION_COUNT;

    @Element(name = "min-connection", required = false)
    private int minConnection = DEFAULT_CONNECTION_COUNT;

    @Element(name = "max-connection", required = false)
    private int maxConnection = DEFAULT_CONNECTION_COUNT;

    @Element(name = "max-idle", required = false)
    private int maxIdle = -1;

    @Element(name = "socket-timeout", required = false)
    private int socketTimeout = -1;

    @Element(name = "socket-connect-timeout", required = false)
    private int socketConnectTimeout = -1;
    
    @Element(name = "need-error-handler", required = false)
    private boolean needErrorHandler = false;

	@Element(name = "max-retry-delay", required = false)
	private long maxRetryDelay;

    public KVStorageDescriptor() {
    }

    @Commit
    public void after() {
        if (this.servers == null || this.servers.isEmpty()) {
            throw new IllegalStateException(
                    "servers should not be null or empty");
        }
        this.serversArray = new String[this.servers.size()];
        int i = 0;
        for (Server server : this.servers) {
            this.serversArray[i++] = server.getAddress();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public int getInitConnection() {
        return initConnection;
    }

    public void setInitConnection(int initConnection) {
        this.initConnection = initConnection;
    }

    public int getMinConnection() {
        return minConnection;
    }

    public void setMinConnection(int minConnection) {
        this.minConnection = minConnection;
    }

    public int getMaxConnection() {
        return maxConnection;
    }

    public void setMaxConnection(int maxConnection) {
        this.maxConnection = maxConnection;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }
    
    public boolean isNeedErrorHandler() {
        return needErrorHandler;
    }

    @Root(name = "server")
    public static class Server implements Serializable {

        private static final long serialVersionUID = 5684434871033327644L;

        @Attribute
        private String address;

        public Server() {
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public String[] getServersArray() {
        return serversArray;
    }

    public void setServersArray(String[] serversArray) {
        this.serversArray = serversArray;
    }

    public int getSocketConnectTimeout() {
        return socketConnectTimeout;
    }

    public void setSocketConnectTimeout(int socketConnectTimeout) {
        this.socketConnectTimeout = socketConnectTimeout;
    }

	public long getMaxRetryDelay() {
		return maxRetryDelay;
	}
}
