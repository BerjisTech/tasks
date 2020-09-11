package tech.berjis.tasks;

public class Chat {
    private long time;
    private boolean read, delete;
    private String sender_chat_id, receiver_chat_id, sender, receiver, text, type;

    public Chat(long time, boolean read, boolean delete, String sender_chat_id, String receiver_chat_id, String sender, String receiver, String text, String type) {
        this.time = time;
        this.read = read;
        this.delete = delete;
        this.sender_chat_id = sender_chat_id;
        this.receiver_chat_id = receiver_chat_id;
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.type = type;
    }

    public Chat(){}

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public String getSender_chat_id() {
        return sender_chat_id;
    }

    public void setSender_chat_id(String sender_chat_id) {
        this.sender_chat_id = sender_chat_id;
    }

    public String getReceiver_chat_id() {
        return receiver_chat_id;
    }

    public void setReceiver_chat_id(String receiver_chat_id) {
        this.receiver_chat_id = receiver_chat_id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
