package com.rofs.spring.jooq;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserJooqRepository userRepository;

    @PostMapping("/users")
    public String save(@RequestBody SaveUserRequest request) {
        int save = userRepository.save(request.identifier());
        return "Saved " + request.identifier() + "(" + save + ") user(s)";
    }


    @GetMapping("/users/{id}")
    public User get(@PathVariable long id) {
        User byId = userRepository.findById(id);
        return byId;
    }
}
