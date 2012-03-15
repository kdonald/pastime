package com.pastime.players;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import com.sun.xml.internal.ws.util.StringUtils;

public class SignupForm {

    private String picture_url;

    @NotEmpty
    private String first_name;
    
    @NotEmpty
    private String last_name;

    @NotEmpty
    @Size(min=6, max=16)
    private String password;
    
    @NotEmpty
    private String email;
    
    @NotNull
    @DateTimeFormat(pattern="MM/dd/YYYY")
    private Date birthday;
    
    @NotNull
    private Gender gender;
    
    @NotEmpty
    @Size(min=5, max=5)
    private String zip_code;

    private String referral_code;

    public String getPicture_url() {
        return picture_url;
    }

    public void setPicture_url(String picture_url) {
        this.picture_url = picture_url;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = StringUtils.capitalize(first_name.trim());
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = StringUtils.capitalize(last_name.trim());
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.trim().toLowerCase();
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getZip_code() {
        return zip_code;
    }

    public void setZip_code(String zip_code) {
        this.zip_code = zip_code.trim();
    }

    public String getReferral_code() {
        return referral_code;
    }

    public void setReferral_code(String referral_code) {
        this.referral_code = referral_code.trim().toLowerCase();
    }

}