package com.glens.speech.utils;

public class DialogMessage {
    //    设置收到消息类型为0 发送信息类型为1
    public static final int TYPE_RECEIVED = 0;
    public static final int TYPE_SEND = 1;

    private String content;
    private int type;

    //构造函数赋值
    public DialogMessage(String content, int type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }

}