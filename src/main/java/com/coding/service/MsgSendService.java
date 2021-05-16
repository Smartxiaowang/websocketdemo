package com.coding.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MsgSendService {

    private static final Map<UUID, SocketIOClient> CLIENT_MAP = new ConcurrentHashMap<>();
    private final SocketIOServer socketIOServer;

    /**
     * 前端执行
     * const socket = io('http://127.0.0.1:8888', {});
     * <p>
     * 启动的时候会被调用一次
     */
    @PostConstruct
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

    public int sendMsg(Object demo) {
        CLIENT_MAP.forEach((key, value) -> {
            value.sendEvent("server_event", demo);
            log.info("发送数据成功:{}", key);
        });
        return CLIENT_MAP.size();
    }
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

    public String checkUid(UUID uid) {
        log.info("hsah:{}",CLIENT_MAP);
        if (CLIENT_MAP.get(uid) != null) {
            return "连接成功";
        } else {
            return "连接失败";
        }
    }
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

}
