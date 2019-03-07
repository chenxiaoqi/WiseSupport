package com.wisesupport.test.jackson;

import java.util.*;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author c00286900
 * @version [版本号, 2019/2/21]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class Album {
    private String title;
    private String[] links;
    private List songs = new ArrayList();
    private Artist artist;
    private Map<String,String> musicians = new HashMap<>();

    public Album(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setLinks(String[] links) {
        this.links = links;
    }

    public String[] getLinks() {
        return links;
    }

    public void setSongs(List songs) {
        this.songs = songs;
    }

    public List getSongs() {
        return songs;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Artist getArtist() {
        return artist;
    }

    public Map getMusicians() {
        return Collections.unmodifiableMap(musicians);
    }

    public void addMusician(String key, String value) {
        musicians.put(key, value);
    }
}
