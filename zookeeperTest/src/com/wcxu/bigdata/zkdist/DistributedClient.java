package com.wcxu.bigdata.zkdist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class DistributedClient {

	private static final String connectString = "zoo1:2181,zoo2:2181,zoo3:2181";
	private static final int sessionTimeout = 20000;
	private static final String parentNode = "/servers";

	private ZooKeeper zk = null;
	// 注意：线程安全
	private volatile ArrayList<String> serverList;

	/**
	 * 创建到zk的客户端连接
	 * 
	 * @throws IOException
	 */
	public void getConnect() throws IOException {
		zk = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				// 收到事件通知后的回调函数（应该是我们自己的事件处理函数)
				try {
					// 重新更新服务器列表，并重新注册监听
					getServerList();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 获取服务器信息列表
	 * 
	 */
	public void getServerList() throws Exception {
		// 获取服务器子节点信息，并且对父节点进行监听
		List<String> children = zk.getChildren(parentNode, true);

		// 先创建一个局部的list来存储服务器信息
		ArrayList<String> servers = new ArrayList<String>();
		for (String child : children) {
			// child只是子节点名
			byte[] data = zk.getData(parentNode + "/" + child, false, null);
			servers.add(new String(data));
		}

		// 把servers赋值给成员变量serverList，以提供给各个业务线程使用
		serverList = servers;

		// 打印一下服务器列表
		System.out.println(serverList.toString());
	}

	/**
	 * 业务功能
	 * 
	 * @throws InterruptedException
	 */
	public void handleBusiness() throws InterruptedException {
		System.out.println("client" + "start working");
		Thread.sleep(Long.MAX_VALUE);
	}

	public static void main(String[] args) throws Exception {
		// 获取zk连接
		DistributedClient distributedClient = new DistributedClient();
		distributedClient.getConnect();
		// 获取servers的子节点信息（并监听），从中获取服务器信息列表
		distributedClient.getServerList();
		// 业务线程
		distributedClient.handleBusiness();
	}
}
