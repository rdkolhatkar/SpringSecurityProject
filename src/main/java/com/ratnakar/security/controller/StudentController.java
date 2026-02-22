package com.ratnakar.security.controller;

import com.ratnakar.security.model.Student;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class StudentController {
    /*
        What is CSRF?
        Answer: CSRF stands for Cross-site Request Forgery (CSRF)
        CSRF (Cross-Site Request Forgery) is a security attack where a hacker tricks a logged-in user into performing an unwanted action on a website.
        It happens without the user knowing, because the browser automatically sends cookies (including session cookies).
        Spring Security prevents this by generating and validating a CSRF token for state-changing requests.
    */
    List<Student> studentList = new ArrayList<>(List.of(
            new Student(1, "Navin", "Java"),
            new Student(2, "Rahul", "Python")
    ));
    @GetMapping("/get/Students")
    public List<Student> getStudents() {
        return studentList;
    }
    @GetMapping("/csrf-token")
    public CsrfToken getCsrfToken(HttpServletRequest httpServletRequest){
        // http servlet request object will have the session id and CSRF token in the attribute.
        return (CsrfToken) httpServletRequest.getAttribute("_csrf");
    }
    @PostMapping("/add/Student")
    public void addStudent(@RequestBody Student student){
        studentList.add(student);
    }
}
