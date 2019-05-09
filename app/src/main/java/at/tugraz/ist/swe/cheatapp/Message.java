package at.tugraz.ist.swe.cheatapp;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

@Entity
public class Message {

    @PrimaryKey(autoGenerate = true)
    private int messageId;

    private int userId;
    private String messageText;
    private boolean messageSent;

    public Message(int userId, String messageText, boolean messageSent) {
        this.userId = userId;
        this.messageText = messageText;
        this.messageSent = messageSent;
    }

    public Message(String jsonMessageString, boolean messageSent) throws JSONException {
        JSONObject jsonMessage = new JSONObject(jsonMessageString);
        this.userId = jsonMessage.getInt("userId");
        this.messageText = jsonMessage.getString("messageText");
        this.messageSent = messageSent;
    }

    public Message(final Message other) {
        this.userId = other.userId;
        this.messageText = other.messageText;
        this.messageSent = other.messageSent;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public boolean getMessageSent() {
        return messageSent;
    }

    public void setMessageSent(boolean messageSent) {
        this.messageSent = messageSent;
    }

    public String getJsonString() {
        JSONObject jsonMessage = new JSONObject();
        try {
            jsonMessage.put("userId", this.userId);
            jsonMessage.put("messageText", messageText);
        } catch (JSONException exception) {
            exception.printStackTrace();
            return null;
        }
        return jsonMessage.toString();
    }
}
