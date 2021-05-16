# websocketdemo

# **服务端接口文档**

| 接口rul | 参数                    | 返回值                             | 类型 | 描述                     |
| ------- | ----------------------- | ---------------------------------- | ---- | ------------------------ |
| send    | msg(String)             | 发送人数(String)                   | GET  | 给所有连接的用户发送通知 |
| send    | uid(String),msg(String) | 发送用户与信息（String）           | Post | 给单个用户发送通知       |
| getstu  | uid（String）           | 连接状态（String）                 | POST | 获取连接                 |
| Logout  | uid（String）           | info,data,current_time,status,code | Get  | 断开此用户连接           |

# **客户端**socket状态

socket.on()监听的事件：

 connect：连接成功
 connecting：正在连接
 disconnect：断开连接
 connect_failed：连接失败
 error：错误发生，并且无法被其他事件类型所处理
 message：同服务器端message事件
 anything：同服务器端anything事件
 reconnect_failed：重连失败
 reconnect：成功重连
 reconnecting：正在重连
  当第一次连接时，事件触发顺序为：connecting->connect；当失去连接时，事件触发顺序为：disconnect->reconnecting（可能进行多次）->connecting->reconnect->connect。

## 一、服务端连接配置

```java
//通过Java类的模式给SocketIo赋值
ws:
  port: 8888
  title: arduino服务器
  host: 0.0.0.0
  boss-count: 1
  work-count: 100
  allow-custom-requests: true
  upgrade-timeout: 10000
  ping-timeout: 60000
  ping-interval: 25000
//yml
      
@Data
@ConfigurationProperties("ws")
public class AppProperties {
    /**
     * title
     */
    private String title;

    /**
     * host
     */
    private String host;

    /**
     * port
     */
    private Integer port;

    /**
     * bossCount
     */
    private int bossCount;

    /**
     * workCount
     */
    private int workCount;

    /**
     * allowCustomRequests
     */
    private boolean allowCustomRequests;

    /**
     * upgradeTimeout
     */
    private int upgradeTimeout;

    /**
     * pingTimeout
     */
    private int pingTimeout;

    /**
     * pingInterval
     */
    private int pingInterval;
}
//Properties



//Configuration
@Configuration
@EnableConfigurationProperties({
        AppProperties.class,
})
public class AppConfiguration {

    @Scope("prototype")
    @Bean
    public SocketIOServer socketIoServer(AppProperties appProperties) {
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setTcpNoDelay(true);
        socketConfig.setSoLinger(0);
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setSocketConfig(socketConfig);
        config.setHostname(appProperties.getHost());
        config.setPort(appProperties.getPort());
        config.setBossThreads(appProperties.getBossCount());
        config.setWorkerThreads(appProperties.getWorkCount());
        config.setAllowCustomRequests(appProperties.isAllowCustomRequests());
        config.setUpgradeTimeout(appProperties.getUpgradeTimeout());
        config.setPingTimeout(appProperties.getPingTimeout());
        config.setPingInterval(appProperties.getPingInterval());
        return new SocketIOServer(config);
    }
```

### Spring boot get bean 工具类

```java
package com.coding.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }
    //获取applicationContext
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    //通过name获取 Bean.
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

}

```

### 多用户启动

```java
@PostConstruct
//启动的时候会被调用一次
    private void autoStart() {
        log.info("start ws");
        socketIOServer.addConnectListener(client -> {
            log.info("check success");
            CLIENT_MAP.put(client.getSessionId(), client);
            log.info("uid:{}", client.getSessionId());
        });
        //退出时切断
        socketIOServer.addDisconnectListener(client -> {
            CLIENT_MAP.remove(client.getSessionId());
            client.disconnect();
            log.info("移除client:{}", client.getSessionId());
        });
        socketIOServer.start();
        log.info("start finish");
    }

    @PreDestroy
    private void onDestroy() {
        if (socketIOServer != null) {
            socketIOServer.stop();
        }
    }
```

### 多用户消息

```java
public int sendMsg(Object demo) {
        CLIENT_MAP.forEach((key, value) -> {
            value.sendEvent("server_event", demo);
            log.info("发送数据成功:{}", key);
        });
        return CLIENT_MAP.size();
```

### 单用户消息

```java
public String sendOneMsg(Map<String,String> map) {
        UUID uid = UUID.fromString(map.get("uid"));
        map.remove("uid");
        Object demo = map;
        SocketIOClient client = CLIENT_MAP.get(uid);
        client.sendEvent("server_event", demo);
        log.info("发送数据成功:{}", map.get("msg"));
        String mssg = "给此用户 " + uid + " 发送：" + map.get("msg");
        return mssg;
    }
```

### 挂断

```java
public HashMap<String,String> Logout(UUID uid) {
        HashMap<String,String> date = new HashMap<>();
        log.info("移除uid:{}",uid);
        CLIENT_MAP.remove(uid);
        date.put("info","0");
        date.put("data"," ");
        date.put("current_time"," ");
        date.put("status","已关闭连接");
        date.put("code"," ");
        return date;
    }
```

# 客户端

### 点击获取连接状态

```js
 function btn(){
        const socket = io('http://127.0.0.1:8888');
        let txt
        socket.on('connect', () => {
            //连接成功后 返回状态
            txt =  document.getElementById("code")
            txt.innerHTML = socket.id
            $.ajax({
                type: "POST", // 以post方式发起请求
                url: "http://localhost:8080/getstu", // 你的请求链接
                data: { // 提交数据
                    "uid": socket.id, // 前者为字段名，后者为数据
                },
                success(info) {
                    var txt =  document.getElementById("status")
                    txt.innerHTML=info.info
                }
            })
            socket.on('server_event', data => {
                $("#data").html(data.msg);
                $("#current_time").html(data.date);
            });
        });
    }
```

### 点击断开

```js
function btns() {
        var txt =  document.getElementById("code").innerText
        $.ajax({
            type: "GET", // 以post方式发起请求
            url: "http://localhost:8080/Logout", // 你的请求链接
            data: { // 提交数据
                "uid": txt, // 前者为字段名，后者为数据
            },
            success(info) {
                    var txt =  document.getElementById("status")
                    txt.innerHTML=info.status
                    var txt1 =  document.getElementById("data")
                    txt1.innerHTML=info.data
                    var txt2 =  document.getElementById("current_time")
                    txt2.innerHTML=info.current_time
                    var txt3 =  document.getElementById("code")
                    txt3.innerHTML=info.code
            }
        })
    }
```

