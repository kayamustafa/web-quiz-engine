package com.kaya.webquizengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "completion")
public class Completion {
    @Id
    @GeneratedValue
    private int id;

    @ManyToOne
    @NotNull
    @JsonIgnore
    private User user;

    @ManyToOne
    @NotNull
    @JsonIgnore
    private Quiz quiz;

    @Column(name = "quizIds", nullable = false)
    @JsonProperty("quizId")
    private int xquizId;

    @Column(name = "quizTitles", nullable = false)
    @JsonProperty("quizTitle")
    private String xquizTitle;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    public LocalDateTime getCompletedAt(){
        return completedAt;
    }

    public Completion(){
        completedAt = LocalDateTime.now();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
        this.xquizId = quiz.getId();
        this.xquizTitle = quiz.getTitle();
    }

    public int getXquizId(){
        return this.xquizId;
    }
    public String getXquizTitle(){
        return this.xquizTitle;
    }
}
