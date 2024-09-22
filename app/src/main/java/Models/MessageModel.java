package Models;

public class MessageModel {
    private String uid;
    private String message;
    private String senderName;
    private String fileUrl;
    private String fileName;
    private long timestamp;
    private String messageId; // New field for unique message identifier

    public MessageModel(String uid, String message) {
        this.uid = uid;
        this.message = message;
    }

    public MessageModel(String uid, String message, String fileUrl) {
        this.uid = uid;
        this.message = message;
    }

    public MessageModel() {
        // Default constructor required for calls to DataSnapshot.getValue(MessageModel.class)
    }

    public MessageModel(String uid, String message, String senderName, String fileUrl, String fileName) {
        this.uid = uid;
        this.message = message;
        this.senderName = senderName;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
