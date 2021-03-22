package ru.job4j.dto;

public class Candidate {
    private String name;
    private int age;
    private String email;
    private String position;

    public Candidate(String name, int age, String position, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
