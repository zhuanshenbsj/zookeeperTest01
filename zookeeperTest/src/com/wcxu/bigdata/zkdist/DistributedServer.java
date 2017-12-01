package com.wcxu.bigdata.zkdist;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class DistributedServer {

	private ZooKeeper zk = null;
	private static final String connectString = "zoo1:2181,zoo2:2181,zoo3:2181";
	private static final int sessionTimeout = 20000;
	private static final String parentNode = "/servers/";

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
				System.out.println(event.getType() + "---" + event.getPath());
				try {
					zk.getChildren("/", true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 向zk集群注册服务器集群信息
	 * 
	 * @param hostname
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public void registerServer(String hostname) throws KeeperException, InterruptedException {
		String createNode = zk.create(parentNode + "server", hostname.getBytes(), Ids.OPEN_ACL_UNSAFE,
				CreateMode.EPHEMERAL_SEQUENTIAL);
		System.out.println(hostname + "is on line.." + createNode);
	}

	/**
	 * 业务功能
	 * 
	 * @throws InterruptedException
	 */
	public void handleBusiness(String hostname) throws InterruptedException {
		System.out.println(hostname + "start working");
		Thread.sleep(Long.MAX_VALUE);
	}

	public static void main(String[] args) throws IOException, KeeperException, InterruptedException {

		// 获取zk连接，
		DistributedServer server = new DistributedServer();
		server.getConnect();

		// 利用zk连接注册服务器信息
		server.registerServer(args[0]);

		// 启动业务功能
		server.handleBusiness(args[0]);

	}
}
