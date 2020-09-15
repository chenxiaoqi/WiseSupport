package com.lazyman.timetennis.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lazyman.timetennis.arena.Arena;
import com.lazyman.timetennis.arena.Court;
import com.lazyman.timetennis.user.User;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Booking implements Serializable {
    private Integer id;

    private String openId;

    private Date date;

    private Arena arena;

    private Court court;

    private Integer start;

    private Integer end;

    private Boolean charged;

    private Date updateTime;

    private User owner;

    private Integer fee;

    private List<User> shareUsers;

    public List<User> getShareUsers() {
        return shareUsers;
    }

    public void setShareUsers(List<User> shareUsers) {
        this.shareUsers = shareUsers;
    }

    public Integer getFee() {
        return fee;
    }

    public void setFee(Integer fee) {
        this.fee = fee;
    }

    private boolean cancelAble;

    public boolean isCancelAble() {
        return cancelAble;
    }

    public void setCancelAble(boolean cancelAble) {
        this.cancelAble = cancelAble;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId == null ? null : openId.trim();
    }

    @JsonFormat(pattern = "yyyy-MM-dd")
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Arena getArena() {
        return arena;
    }

    public void setArena(Arena arena) {
        this.arena = arena;
    }

    public Court getCourt() {
        return court;
    }

    public void setCourt(Court court) {
        this.court = court;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Boolean getCharged() {
        return charged;
    }

    public void setCharged(Boolean charged) {
        this.charged = charged;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}