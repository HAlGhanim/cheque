package com.cornerstone.cheque.controller

import com.cornerstone.cheque.auth.jwt.AuthResponse
import com.cornerstone.cheque.auth.jwt.JwtService
import com.cornerstone.cheque.model.Role
import com.cornerstone.cheque.model.User
import com.cornerstone.cheque.repo.UserRepository
import com.cornerstone.cheque.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class UserController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @PostMapping("/auth/register")
    fun create(@RequestBody entity: User): ResponseEntity<Any> {
        if (userRepository.findByEmail(entity.email) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Email already exists"))
        }
        val hashedPassword = passwordEncoder.encode(entity.password)
        val newUser = User(
            email = entity.email,
            password = hashedPassword,
            role = Role.USER,
            status = "Active",
            joinedDate = entity.joinedDate
        )
        userRepository.save(newUser)
        val token = jwtService.generateToken(newUser.email,  newUser.role.toString())

        val response = AuthResponse(token = token, user = newUser)
        return ResponseEntity.ok(response)
    }


    @GetMapping("/users/me")
    @PreAuthorize("isAuthenticated()")
    fun getCurrentUser(): ResponseEntity<User> {
        val email = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found with email: $email")
        return ResponseEntity.ok(user)
    }

}
