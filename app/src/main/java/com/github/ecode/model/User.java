package com.github.ecode.model;

public class User {
    private Integer id;
    private String username;
    private String name;
    private String role;
    private String email;
    private String status;
    private String profilePicture;
    private String phone;
    private String sex;
    private String address;
    private Long score;
    private String birthDate;
    private String createTime;
    private String updateTime;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Long getScore() { return score; }
    public void setScore(Long score) { this.score = score; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
}
