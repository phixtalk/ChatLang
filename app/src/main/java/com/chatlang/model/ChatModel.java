package com.chatlang.model;

/**
 * Created by ADMIN on 12/4/2018.
 */

public class ChatModel {

    private String id;
    //private UserModel userModel;
    //private String message;
    private String timeStamp;
    //private FileModel file;
    //private MapModel mapModel;


    //UPGRADE OF CHAT MODEL TO MESSAGES MODEL
    private String message, type;
    private String  time;
    private boolean seen;
    private String from, fromname;

    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }

    public String getFromName() {
        return fromname;
    }
    public void setFromName(String fromname) {
        this.fromname = fromname;
    }

    public ChatModel(String from) {
        this.from = from;
    }
    public ChatModel(String message, String type, String time, boolean seen) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.seen = seen;
    }

    //GOTTEN FROM FILE MODEL
    private String url_file;
    private String name_file;
    private String size_file;
    //GOTTEN FROM MAP MODEL
    private String latitude;
    private String longitude;
    private String address;
    /*
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    */
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public boolean isSeen() {
        return seen;
    }
    public void setSeen(boolean seen) {
        this.seen = seen;
    }
    public void setUrl_file(String url_file) {
        this.url_file = url_file;
    }
    public String getName_file() {
        return name_file;
    }
    public void setName_file(String name_file) {
        this.name_file = name_file;
    }
    public String getSize_file() {
        return size_file;
    }
    public void setSize_file(String size_file) {
        this.size_file = size_file;
    }
    public String getUrl_file() {
        return url_file;
    }


    public String getLatitude() {
        return latitude;
    }
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    public String getLongitude() {
        return longitude;
    }
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    public String getAddress() {return address;}
    public void setAddress(String address) {this.address = address;}

    public ChatModel() {
    }

    /*
    public ChatModel(UserModel userModel, String message, String timeStamp, FileModel file) {
        this.userModel = userModel;
        this.message = message;
        this.timeStamp = timeStamp;
        this.file = file;
    }

    public ChatModel(UserModel userModel, String timeStamp, MapModel mapModel) {
        this.userModel = userModel;
        this.timeStamp = timeStamp;
        this.mapModel = mapModel;
    }
    */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*
    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }
    */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    /*
    public FileModel getFile() {
        return file;
    }

    public void setFile(FileModel file) {
        this.file = file;
    }

    public MapModel getMapModel() {
        return mapModel;
    }

    public void setMapModel(MapModel mapModel) {
        this.mapModel = mapModel;
    }
    */
    /*
    @Override
    public String toString() {
        return "ChatModel{" +
                "mapModel=" + mapModel +
                ", file=" + file +
                ", timeStamp='" + timeStamp + '\'' +
                ", message='" + message + '\'' +
                ", userModel=" + userModel +
                '}';
    }
    */
}
