package com.example.ResumeParser.dto;

public class ResumeData {
    private String name;
    private String email;
    private String phoneNumber;
    private double yearsOfExperience;
    private String skills;  // add skills as String (or change to Set<String> if you want)
    private String fileName;

    public ResumeData() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public double getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(double yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}
