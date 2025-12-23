package com.samCode.spring_boot_learning.repository

import com.samCode.spring_boot_learning.database.model.RefreshToken
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface RefreshTokenRepository: MongoRepository<RefreshToken, ObjectId> {
    fun findByUserIdandHashedToken(userId: ObjectId, hashedToken: String): RefreshToken?
    fun deleteByUserIdandHashedToken(userId: ObjectId, hashedToken: String)

}