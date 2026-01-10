package com.blibli.apigateway.dto.request;

public class MemberDto {
    private String email;
    private String full_name;
    private String password;
    private String phoneNo;

    public MemberDto() {
    }

    public MemberDto(String email, String full_name, String password, String phoneNo) {
        this.email = email;
        this.full_name = full_name;
        this.password = password;
        this.phoneNo = phoneNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
}


