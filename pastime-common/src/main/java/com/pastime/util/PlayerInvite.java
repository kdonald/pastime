package com.pastime.util;

public class PlayerInvite {
    
    private String code;

    private InvitedPlayer player;
    
    public PlayerInvite(String code, Integer id, String firstName, String lastName, String picture, String username) {
        this.code = code;
        this.player = new InvitedPlayer(id, firstName, lastName, picture, username);
    }

    public PlayerInvite(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public InvitedPlayer getPlayer() {
        return player;
    }

    public static class InvitedPlayer {

        private Integer id;
        
        private String firstName;
        
        private String lastName;
        
        private String picture;
        
        private String username;

        public InvitedPlayer(Integer id, String firstName, String lastName, String picture, String username) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.picture = picture;
            this.username = username;
        }

        public Integer getId() {
            return id;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getPicture() {
            return picture;
        }

        public String getUsername() {
            return username;
        }
        
    }
}
