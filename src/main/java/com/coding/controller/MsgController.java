package com.coding.controller;

import com.coding.service.MsgSendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Api(tags = "消息接口")
@RequiredArgsConstructor
@RestController
public class MsgController {

    private final MsgSendService msgSendService;


    @ApiOperation("给单个人发送消息")
    @PostMapping("send")
    public String send(@RequestParam HashMap<String, String> map) {
        map.put("date", LocalDateTime.now().toString());
        String pa = msgSendService.sendOneMsg(map);
        return pa;
    }

    @ApiOperation("给所有人发送消息")
    @GetMapping("send")
    public String sendAll(String msg) {
        Map<String, String> map = new HashMap<>();
        map.put("msg", msg);
        map.put("date", LocalDateTime.now().toString());
        int size = msgSendService.sendMsg(map);
        return "发送成功" + size;
    }

    @ApiOperation("获当前用户的uuid")
    @PostMapping("/getstu")
    public HashMap<String, String> open(@RequestParam HashMap<String, String> parms) {
        String s = parms.get("uid");
        HashMap<String, String> data = new HashMap<>();
        if (s != null) {
            String uid = msgSendService.checkUid(UUID.fromString(s));
            data.put("info", uid);
            return data;
        }
        data.put("info", "未知错误");
        return data;
    }

    @ApiOperation("关闭此用户")
    @GetMapping("/Logout")
    public HashMap<String, String> open(@RequestParam String uid) {
        HashMap<String, String> logout = msgSendService.Logout(UUID.fromString(uid));
        return logout;
    }

}
