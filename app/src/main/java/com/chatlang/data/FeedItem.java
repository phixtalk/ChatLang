package com.chatlang.data;

/**
 * Created by ADMIN on 9/1/2017.
 */

public class FeedItem {
    private String id, userId, postTags, viewCount, countHype, countCast, haveHype, haveCast, haveFollow,  showIdentity, allowComments, countComments;
    private String name, status, image, profilePic, timeStamp, url, description, email, joined, filetype, userinterests, bodyContent;

    public FeedItem() {
    }

    public FeedItem(String userId, String name) {
        super();
        this.userId = userId;
        this.name = name;
    }

    public FeedItem(String id, String name, String image, String status,String profilePic, String timeStamp, String url, String userId,
                    String description,String viewCount, String countHype, String countCast, String haveHype, String haveCast, String email,
                    String joined, String filetype, String userinterests, String postTags, String showIdentity, String allowComments, String countComments, String bodyContent) {
        super();
        this.id = id;
        this.name = name;
        this.image = image;
        this.status = status;
        this.profilePic = profilePic;
        this.timeStamp = timeStamp;
        this.url = url;
        this.userId=userId;
        this.description = description;
        this.viewCount=viewCount;
        this.countCast=countCast;
        this.haveHype=haveHype;
        this.haveCast=haveCast;
        this.haveFollow=haveFollow;
        this.email=email;
        this.joined=joined;
        this.filetype = filetype;
        this.userinterests = userinterests;

        this.postTags=postTags;
        this.showIdentity=showIdentity;
        this.allowComments = allowComments;
        this.countComments = countComments;
        this.bodyContent = bodyContent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public void setEmail(String email) {
        this.email = email;
    }
    public String getEmail() {
        return email;
    }

    public void setDateJoined(String joined) {this.joined = joined;}
    public String getDateJoined() {
        return joined;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }
    public String getFiletype() {
        return filetype;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserId() {
        return userId;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

    public void setViewCount(String viewCount) {
        this.viewCount = viewCount;
    }
    public String getViewCount() {
        return viewCount;
    }

    public void setCountHype(String countHype) {
        this.countHype = countHype;
    }
    public String getCountHype() {
        return countHype;
    }

    public void setCountCast(String countCast) {
        this.countCast = countCast;
    }
    public String getCountCast() {
        return countCast;
    }

    public void setHaveHype(String haveHype) {
        this.haveHype = haveHype;
    }
    public String getHaveHype() {
        return haveHype;
    }

    public void setHaveCast(String haveCast) {
        this.haveCast = haveCast;
    }
    public String getHaveCast() {
        return haveCast;
    }

    public void setHaveFollow(String haveFollow) {
        this.haveFollow = haveFollow;
    }
    public String getHaveFollow() {
        return haveFollow;
    }

    public void setUserInterests(String userinterests) {
        this.userinterests = userinterests;
    }
    public String getUserInterests() {
        return userinterests;
    }

    public void setShowIdentity(String showIdentity) {
        this.showIdentity = showIdentity;
    }
    public String getShowIdentity() {
        return showIdentity;
    }

    public void setAllowComments(String allowComments) {
        this.allowComments = allowComments;
    }
    public String getAllowComments() {
        return allowComments;
    }

    public void setCountComments(String countComments) {
        this.countComments = countComments;
    }
    public String getCountComments() {
        return countComments;
    }

    public void setPostTags(String postTags) {
        this.postTags = postTags;
    }
    public String getPostTags() {
        return postTags;
    }

    public void setBodyContent(String bodyContent) {this.bodyContent=bodyContent;}
    public String getBodyContent(){return bodyContent;}

}
