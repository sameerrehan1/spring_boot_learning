package com.samCode.spring_boot_learning.security

import com.samCode.spring_boot_learning.database.model.RefreshToken
import com.samCode.spring_boot_learning.database.model.User
import com.samCode.spring_boot_learning.repository.RefreshTokenRepository
import com.samCode.spring_boot_learning.repository.UserRepository
import org.bson.types.ObjectId
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64

@Service
class AuthService(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val hashEncoder: HashEncoder
) {

    data class TokenPair(
        val accessToken: String,
        val refreshToken: String
    )

    fun register(email:String, password:String): User {
        return userRepository.save(
            User(
                email = email,
                hashedPassword = hashEncoder.encode(password),
            )
        )
    }


    fun login(email: String, password: String): TokenPair {
        val user= userRepository.findByEmail(email) ?: throw BadCredentialsException("User not found")
        if(!hashEncoder.matches(password, user.hashedPassword)) {
                throw BadCredentialsException("Invalid password")
        }

        val newAccessToken = jwtService.generateAccessToken(user.id.toHexString())
        val newRefreshToken = jwtService.generateRefreshToken(user.id.toHexString())
        return TokenPair(
            newAccessToken,
            newRefreshToken
        )
    }

    fun refresh(refreshToken: String): TokenPair {
        if(!jwtService.validateRefreshToken(refreshToken)) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user= userRepository.findById(ObjectId(userId)).orElseThrow{
            IllegalArgumentException("Invalid Refresh Token")
        }

        val hashed = hashToken(refreshToken)
        refreshTokenRepository.findByUserIdandHashedToken(user.id, hashed)
            ?: throw IllegalArgumentException("Refresh Token not found")

        refreshTokenRepository.deleteByUserIdandHashedToken(user.id, hashed)

        val newAccessToken = jwtService.generateAccessToken(user.id.toHexString())
        val newRefreshToken = jwtService.generateRefreshToken(user.id.toHexString())
        storeRefreshToken(user.id, newRefreshToken)
        return TokenPair(
            newAccessToken,
            newRefreshToken
        )
    }
        @Transactional
    private fun storeRefreshToken(userId: ObjectId , rawRefreshToken: String) {
            val hashed = hashToken(rawRefreshToken)
            val expiryMs = jwtService.refreshTokenValidityMs
            val expiresAt = Instant.now().plusMillis(expiryMs)

            refreshTokenRepository.save(
                RefreshToken(
                    userId = userId,
                    expiresAt = expiresAt,
                    hashedToken = hashed
                )
            )

    }

    private fun hashToken(token: String): String {
        val digest= MessageDigest.getInstance("SHA-256")
        val hashBytes =  digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }

}