package com.pastime.leagues.season;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import com.pastime.players.Gender;
import com.pastime.util.Name;

public class ProposedPlayer {
    
    private Integer id;
    
    private Name name;
    
    private String email;
    
    private Gender gender;

    private LocalDate birthday;
    
    public ProposedPlayer(Integer id, Name name, String email, Gender gender, LocalDate birthday) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.birthday = birthday;
    }

    public Integer getId() {
        return id;
    }
    
    public Name getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public Gender getGender() {
        return gender;
    }

    public boolean isFemale() {
        return gender == Gender.FEMALE;
    }

    public boolean isMale() {
        return gender == Gender.MALE;
    }

    public int getAge() {
        return Years.yearsBetween(birthday, new LocalDate()).getYears();
    }

}