package com.pastime.players;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.pastime.util.PastimeEnvironment;
import com.pastime.util.Principal;

@Repository
public class PlayerRepository {

    private static final String PICTURE_FILE_EXTENSION = ".png";
    
    private JdbcTemplate jdbcTemplate;
    
    private PastimeEnvironment environment;
    
    private Resource pictures;
    
    private String playerSql;
    
    @Inject
    public PlayerRepository(JdbcTemplate jdbcTemplate, PastimeEnvironment environment, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
        this.pictures = resourceLoader.getResource("players/");
        this.playerSql = SqlUtils.sql(new ClassPathResource("player.sql", getClass()));
    }
    
    @Transactional
    public Player findMe(Principal principal) {
        return findPlayer(principal.getPlayerId());
    }

    public URI findPicture(Integer playerId, PictureType type) {
        Resource path = playerPicturesPath(playerId + "/");
        if (path.exists()) {
            return pictureFrom(path, type);
        } else {
            return defaultPicture(playerId, type);            
        }
    }
    
    // internal helpers

    private Player findPlayer(Integer id) {
        return jdbcTemplate.queryForObject(playerSql, new PlayerMapper(environment), id);
    }
    
    private Resource playerPicturesPath(String path) {
        try {
            return pictures.createRelative(path);
        } catch (IOException e) {
            throw new IllegalArgumentException("Not a valid relative path: " + path, e);
        }
    }
    
    private URI defaultPicture(Integer playerId, PictureType type) {
        Player player = findPlayer(playerId);            
        try {
            return pictures.createRelative("defaults/" + player.getGender().name().toLowerCase() + "/" + type.name().toLowerCase() + PICTURE_FILE_EXTENSION).getURI();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to access picture repository to load default player profile pictures", e);
        }
    }
    
    private URI pictureFrom(Resource directory, PictureType type) {
        try {
            return directory.createRelative(type.name().toLowerCase() + PICTURE_FILE_EXTENSION).getURI();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to access picture repository to load player profile picture", e);
        }
    }
    
    // cglib ceremony 
    public PlayerRepository() {}
    
}