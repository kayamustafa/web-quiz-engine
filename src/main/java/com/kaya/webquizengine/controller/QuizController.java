package com.kaya.webquizengine.controller;

import com.kaya.webquizengine.model.Completion;
import com.kaya.webquizengine.model.Quiz;
import com.kaya.webquizengine.exception.QuizNotFoundException;
import com.kaya.webquizengine.model.User;
import com.kaya.webquizengine.repository.CompletionRepository;
import com.kaya.webquizengine.repository.QuizRepository;
import com.kaya.webquizengine.service.UserService;
import com.kaya.webquizengine.utils.Feedback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api")
public class QuizController {
    private final QuizRepository quizRepository;

    private final CompletionRepository completionRepository;

    private final UserService userService;

    @Autowired
    public QuizController(QuizRepository quizRepository, CompletionRepository completionRepository, UserService userService){
        this.quizRepository = quizRepository;
        this.completionRepository = completionRepository;
        this.userService = userService;
    }

    public Page<Quiz> getAllQuizzes(int pageNo, int pageSize){
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("id"));
        return quizRepository.findAll(paging);
    }

    @GetMapping(path = "/quizzes")
    public Page<Quiz> getQuizzes(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "3") int pageSize){
        return getAllQuizzes(page, pageSize);
    }

    @PostMapping(path = "/quizzes")
    public Quiz createQuiz(@Valid @RequestBody Quiz quiz){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.loadUserByUsername(userDetails.getUsername());
        quiz.setUser(user);
        return quizRepository.save(quiz);
    }

    @DeleteMapping("/quizzes/{id}")
    public ResponseEntity<?> deleteQuiz(@PathVariable int id){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No quiz with id: " + id
                ));
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user found.");
        }
        if (quiz.getUser().getId() != user.getId()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        quizRepository.delete(quiz);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(path = "/quizzes/{id}")
    public Quiz getQuizById(@PathVariable int id){
        return quizRepository.findById(id).orElseThrow(QuizNotFoundException::new);
    }

    @PostMapping(path = "/quizzes/{id}/solve")
    public Feedback solveQuiz(@RequestBody HashMap<String, List<Integer>> answer,
                              @PathVariable int id, @Autowired Principal principal){

        Quiz quiz = getQuizById(id);
        User user = userService.loadUserByUsername(principal.getName());

        List<Integer> quizAnswer = new ArrayList<>();
        quizAnswer.addAll(quiz.getAnswer());
        List<Integer> inputAnswer = new ArrayList<>();
        inputAnswer.addAll(answer.get("answer"));

        if(quizAnswer == null) quiz.setAnswer(new ArrayList<Integer>());
        if(quizAnswer.equals(inputAnswer)){
            Completion completion = new Completion();
            completion.setQuiz(quiz);
            completion.setUser(user);
            System.out.println(completion.getCompletedAt());
            completionRepository.save(completion);
            return new Feedback(true, "Congratulations, you're right!");
        }
        else
            return new Feedback(false, "Wrong answer! Please, try again.");

    }

    @GetMapping("/completed")
    public Page<Completion> getCompletedQuizzes(Principal principal, Pageable pageable){
        return completionRepository.findAllByUserOrderByCompletedAtDesc(principal.getName(), pageable);
    }

}
