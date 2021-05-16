package com.coding.service;


import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@RequiredArgsConstructor
public class sockser {
    private static final Map<UUID, SocketIOClient> CLIENT_MAP = new ConcurrentHashMap<>();
    private final SocketIOServer socketIOServer;
    /**
     * 启动的时候会被调用一次
     */
   /* @PostConstruct
    private void autoStart() {
        // socketIOServer.start();

    }
    @PreDestroy
    private void onDestroy() {
        if (socketIOServer != null) {
            //关闭所有连接
            socketIOServer.stop();
        }
    }*/

    public int sendMsg(Object demo) {

        CLIENT_MAP.forEach((key, value) -> {
            value.sendEvent("server_event", demo);
            //client.sendEvent("xx",msg)
            log.info("发送数据成功:{}", key);
        });
        return CLIENT_MAP.size();
    }
    public String sendOneMsg(Map<String,String> map) {
        SocketIOClient client = CLIENT_MAP.get(map.get("uid"));
        //根据UID发送消息
        client.sendEvent("server_event", map.get("msg"));
        //client.sendEvent("xx",msg)
        log.info("发送数据成功:{}", map.get("msg"));
        String mssg = "给此用户 " + map.get("uid") + " 发送：" + map.get("msg");
        return mssg;
    }
    public String getUid() {
        String sId = new String();
        socketIOServer.start();
        socketIOServer.addConnectListener(client -> {
            //addConnectListener 监听
            CLIENT_MAP.put(client.getSessionId(), client);
            sId.valueOf(client.getSessionId());
            log.info("check success");
            //key -> uuid value->client
        });
        return sId;
    }
    public void Logout(String uid) {
        SocketIOClient client1 = CLIENT_MAP.get(uid);
        CLIENT_MAP.remove(client1.getSessionId());
        //在HashMap中移除此信息
        client1.disconnect();
    }
}
