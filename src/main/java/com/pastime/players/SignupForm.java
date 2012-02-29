package com.pastime.players;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

public class SignupForm {

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @NotEmpty
    private String password;
    
    @NotEmpty
    private String email;
    
    @NotNull
    private int month;
    
    @NotNull
    private int day;
    
    @NotNull
    private int year;
    
    @NotNull
    private Gender gender;
    
    @NotEmpty
    @Size(min=5, max=5)
    private String zipCode;

    private String r;
    
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = StringUtils.capitalize(firstName.trim());
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = StringUtils.capitalize(lastName.trim());
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.trim().toLowerCase();
    }
    
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Date getBirthday() {
        return new LocalDate(year, month, day).toDate();
    }
        
    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode.trim();
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r.trim().toLowerCase();
    }

}