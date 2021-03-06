package com.example.demo.user;

import com.example.demo.configuration.exceptions.FoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = this.userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Email %s not found", email)));
        UserDetailsPrincipal userDetailsPrincipal = new UserDetailsPrincipal(user);
        return userDetailsPrincipal;
    }

    public User getPrincipalData(Principal principal) {
        Optional<User> row = this.userRepository.findUserByEmail(principal.getName());
        User user = row.get();
        return user;
    }

    public boolean checkIfPrincipalIsUser(Long userId, Principal principal) {
        Optional<User> row = this.userRepository.findUserByEmail(principal.getName());
        User user = row.get();
        return user.getId() == userId;
    }

    public boolean checkIfPrincipalIsAdmin(Principal principal) {
        Optional<User> row = this.userRepository.findUserByEmail(principal.getName());
        User user = row.get();
        return user.getRoles().stream().map(item -> item.getName()).collect(Collectors.toList()).contains("ADMIN");
    }

    public List<User> getList() {
        return this.userRepository.findAll();
    }

    public User getItem(Long userId) {
        Optional<User> row = this.userRepository.findById(userId);
        if (row.isPresent()) {
            return row.get();
        } else {
            throw new FoundException(String.format("User with ID: %s doesn't exist", userId));
        }
    }

    public User add(User user) {
        if (this.userRepository.findUserByEmail(user.getEmail()).isPresent()) {
            throw new FoundException(String.format("User with email: '%s' already exists. Emails must be unique.", user.getEmail()));
        }
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        return this.userRepository.save(user);
    }

    public void delete(Long userId) {
        this.userRepository.deleteById(userId);
    }

    public void update(User user) {
        User item = this.getItem(user.getId());
        if (!user.getEmail().isEmpty()) {
            item.setEmail(user.getEmail());
        }
        if (!user.getRoles().isEmpty()) {
            item.setRoles(user.getRoles());
        }
        if (!user.getPassword().isEmpty()) {
            item.setPassword(user.getPassword());
        }
        this.userRepository.save(item);
    }
}
