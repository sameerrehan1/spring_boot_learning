package com.samCode.spring_boot_learning.controllers

import com.samCode.spring_boot_learning.database.model.Note
import com.samCode.spring_boot_learning.repository.NoteRepository
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/notes")
class NoteController(
    private val repository: NoteRepository
) {

    data class NoteRequest(
        val id: String?,
        val title: String,
        val content: String,
        val color: String,
        val ownerId: String
    )

    data class NoteResponse(
        val id: String,
        val title: String,
        val content: String,
        val color: String,
        val createdAt: Instant,
    )

    @PostMapping
    fun save(@RequestBody request: NoteRequest): NoteResponse {

        // Fix 2: Closed the parentheses properly for the Note constructor
        val savedNote = repository.save(
            Note(
                id = request.id?.let{ ObjectId(it)} ?:ObjectId.get(),
                title = request.title,
                content = request.content,
                color = request.color,
                createdAt = Instant.now(),
                ownerId = ObjectId(request.ownerId)
            )
        )

        // Fix 3: Convert the saved "Note" entity into a "NoteResponse"
        return NoteResponse(
            id = savedNote.id.toHexString(), // Assuming your Note entity has an 'id' field
            title = savedNote.title,
            content = savedNote.content,
            color = savedNote.color,
            createdAt = savedNote.createdAt
        )
    }
}