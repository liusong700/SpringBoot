package com.test.springboot.websocket;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/websocket")
@Component
public class MyWebSocket {

    private static final Logger logger = LoggerFactory.getLogger(MyWebSocket.class);

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //房间号
    private int roomNo;
    // 用户命
    private String name;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<MyWebSocket> webSocketSet = new CopyOnWriteArraySet<>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        this.name = "连接" + getOnlineCount();
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        try {
            sendInfo(getOnlineCount() + "", "", 1);
        } catch (IOException e) {
            System.out.println("IO异常");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        try {
            sendInfo(getOnlineCount() + "", this.getName(), 3);
        } catch (IOException e) {
            System.out.println("IO异常");
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("来自客户端的消息:" + message);
        JSONObject jsonObject = JSONObject.parseObject(message);
        //群发消息
        for (MyWebSocket item : webSocketSet) {
            try {
                if (!CollectionUtils.isEmpty(JSONArray.parseArray(jsonObject.getString("names")))) {
                    if (item.equals(this) || jsonObject.getString("names").indexOf(item.getName()) > 0) {
                        item.sendMessage(this.getName() + ":" + jsonObject.getString("message"), 2);
                    }
                } else {
                    item.sendMessage(this.getName() + ":" + jsonObject.getString("message"), 2);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    private void sendMessage(String message, int type) throws IOException {
        //this.session.getBasicRemote().sendText(message);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("name", this.name);
        jsonObject.put("message", message);
        List<MyWebSocket> myWebSocketList = new ArrayList<>();
        for (MyWebSocket myWebSocket : webSocketSet) {
            if (!myWebSocket.getName().equals(this.name)) {
                myWebSocketList.add(myWebSocket);
            }
        }
        jsonObject.put("webSocket", myWebSocketList);
        this.session.getAsyncRemote().sendText(jsonObject.toJSONString());
    }

    /**
     * 群发自定义消息
     */
    private static void sendInfo(String message, String name, int type) throws IOException {
        for (MyWebSocket item : webSocketSet) {
            try {
                if (!StringUtils.isEmpty(name)) {
                    item.sendMessage(message + "," + name, type);
                } else {
                    item.sendMessage(message, type);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static synchronized int getOnlineCount() {
        return onlineCount;
    }

    private static synchronized void addOnlineCount() {
        MyWebSocket.onlineCount++;
    }

    private static synchronized void subOnlineCount() {
        MyWebSocket.onlineCount--;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
