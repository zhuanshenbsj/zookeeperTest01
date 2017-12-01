package com.wcxu.bigdata.zk;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

public class SimpleZkClient {

	// 尽量保持跟zookeeper的zoo.cfg 配置文件一致，都写域名不要写ip。记得配置hosts文件
	private static final String connectString = "zoo1:2181,zoo2:2181,zoo3:2181";
	private static final int sessionTimeout = 20000;
	ZooKeeper zkClient = null;

	@Before
	public void init() throws IOException {
		zkClient = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				// 收到事件通知后的回调函数（应该是我们自己的事件处理函数)
				System.out.println(event.getType() + "---" + event.getPath());
				try {
					zkClient.getChildren("/", true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 数据的增删改查
	 * 
	 * @throws InterruptedException
	 * @throws KeeperException
	 */
	// 创建数据节点到zk中
	@Test
	public void testCreate() throws KeeperException, InterruptedException {

		// 参数1,要创建的节点的路径
		// 参数2,节点的数据
		// 参数3,节点的权限
		// 参数4,节点的类型
		String nodeCreated = zkClient.create("/eclipse", "hello zk".getBytes(), Ids.OPEN_ACL_UNSAFE,
				CreateMode.PERSISTENT);
		// 上传的数据可以是任何类型，但都要转成byte[]数组
	}

	// 判断znode是否存在
	@Test
	public void testExist() throws KeeperException, InterruptedException {
		Stat stat = zkClient.exists("/eclipse", false);
		// 如果不存在，则stat为null
		System.out.println(stat == null ? "not exist" : "exist");
	}

	// 获取子节点
	@Test
	public void getChildren() throws KeeperException, InterruptedException {
		List<String> listChild = zkClient.getChildren("/", true);// true为使用默认监听
		for (String child : listChild) {
			System.out.println(child);
		}
		Thread.sleep(Integer.MAX_VALUE);
		// 程序会一直监听： 如果对/ 根节点的子节点进行修改，就会触发监听
		// 监听只能生效一次，所以在process中重新生效监听
	}

	// 获取znode的数据
	@Test
	public void getData() throws KeeperException, InterruptedException {
		byte[] data = zkClient.getData("/eclipse", false, null);
		System.out.println(new String(data));
	}

	// 删除znode
	@Test
	public void deleteZnode() throws KeeperException, InterruptedException {
		// 第二个参数表示指定要删除的版本，-1表示全部删除
		zkClient.delete("/eclipse", -1);
	}

	// 更新znode数据
	@Test
	public void setZnode() throws KeeperException, InterruptedException {
		zkClient.setData("/app1", "diu".getBytes(), -1);
		byte[] data = zkClient.getData("/app1", false, null);
		System.out.println(new String(data));
	}
}
