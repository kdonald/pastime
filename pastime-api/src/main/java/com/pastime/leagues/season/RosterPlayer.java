package com.pastime.leagues.season;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import com.pastime.util.Gender;

public class RosterPlayer {
    
    private Gender gender;

    private LocalDate birthday;
    
    public RosterPlayer(Gender gender, LocalDate birthday) {
        this.gender = gender;
        this.birthday = birthday;
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

    public boolean samePersonAs(RosterPlayer person) {
        return false;
    }
    
}
