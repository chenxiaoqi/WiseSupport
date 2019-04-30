package com.wisesupport.test.mapstruct;

import com.wisesupport.test.jackson.Album;
import org.junit.Test;

public class TestMapStruct {

    @Test
    public void test(){
        Album album = new Album("marvel 4");
        AlbumDto dto = AlbumMapper.INSTANCE.carToCarDto(album);
        System.out.println(dto.getAlbumTitle());
    }
}

