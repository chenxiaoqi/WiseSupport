package com.wisesupport.test.mapstruct;

import com.wisesupport.test.jackson.Album;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AlbumMapper {

    AlbumMapper INSTANCE = Mappers.getMapper( AlbumMapper.class );

    @Mapping(source = "title", target = "albumTitle")
    AlbumDto carToCarDto(Album album);
}

