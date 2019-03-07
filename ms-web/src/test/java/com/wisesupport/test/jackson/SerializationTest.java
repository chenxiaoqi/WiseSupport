package com.wisesupport.test.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author c00286900
 * @version [版本号, 2019/2/21]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */

public class SerializationTest {

    @Test
    public void test() throws ParseException, IOException {

        System.out.println(createObjectMapper().writeValueAsString(create()));
    }

    @Test
    public void testPath() throws IOException, ParseException {


        ObjectMapper objectMapper = createObjectMapper();
        String genreJson = objectMapper.writeValueAsString(create());

        JsonNode node = objectMapper.readTree(genreJson);
        System.out.println(node.path("artist"));
    }

    @Test
    public void testListType() throws IOException {
        Zoo zoo = new Zoo("London Zoo", "London");
        Lion lion = new Lion("Simba");
        Elephant elephant = new Elephant("Manny");
        zoo.addAnimal(elephant).add(lion);

        ObjectMapper objectMapper = createObjectMapper();

        String zooJson = objectMapper.writeValueAsString(zoo);
        System.out.println(zooJson);

        objectMapper.readValue(zooJson, Zoo.class);

        System.out.println(zoo);

    }

    @Test
    public void testDynamic() {
        String json = "";
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy/MM/dd"));
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy() {
            @Override
            public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
                if (field.getDeclaringClass().equals(Artist.class) && field.getName().equals("name")) {
                    return "Artist-Name";
                } else {
                    return super.nameForField(config, field, defaultName);
                }
            }

            @Override
            public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
                return super.nameForGetterMethod(config, method, defaultName);
            }
        });
        mapper
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
        ;
        return mapper;
    }

    private Album create() throws ParseException {
        Album album = new Album("Kind Of Blue");
        album.setLinks(new String[]{"link1", "link2"});
        List<String> songs = new ArrayList<>();
        songs.add("So What");
        songs.add("Flamenco Sketches");
        songs.add("Freddie Freeloader");
        album.setSongs(songs);
        Artist artist = new Artist();
        artist.name = "Miles Davis";
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        artist.birthDate = format.parse("26-05-1926");
        album.setArtist(artist);
        album.addMusician("Miles Davis", "Trumpet, Band leader");
        album.addMusician("Julian Adderley", "Alto Saxophone");
        album.addMusician("Paul Chambers", "double bass");
        return album;
    }
}
