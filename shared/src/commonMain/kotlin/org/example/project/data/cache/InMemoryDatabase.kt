package org.example.project.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryDatabase : LocalDatabase {
    private val mutex = Mutex()

    private val students = mutableMapOf<String, StudentEntity>()
    private val sessions = mutableMapOf<String, StudySessionEntity>()
    private val messages = mutableListOf<ChatMessageEntity>()
    private val quizResults = mutableListOf<QuizResultEntity>()
    private val skills = mutableMapOf<String, SkillEntity>()
    private val documents = mutableMapOf<String, DocumentEntity>()

    override suspend fun insertStudent(student: StudentEntity) = mutex.withLock {
        students[student.id] = student
    }

    override suspend fun getStudent(id: String): StudentEntity? = mutex.withLock {
        students[id]
    }

    override suspend fun getAllStudents(): List<StudentEntity> = mutex.withLock {
        students.values.toList()
    }

    override suspend fun updateStudent(student: StudentEntity) = mutex.withLock {
        students[student.id] = student
    }

    override suspend fun insertSession(session: StudySessionEntity) = mutex.withLock {
        sessions[session.id] = session
    }

    override suspend fun getSession(id: String): StudySessionEntity? = mutex.withLock {
        sessions[id]
    }

    override suspend fun getSessionsByStudent(studentId: String): List<StudySessionEntity> = mutex.withLock {
        sessions.values.filter { it.studentId == studentId }.sortedByDescending { it.startTime }
    }

    override suspend fun updateSession(session: StudySessionEntity) = mutex.withLock {
        sessions[session.id] = session
    }

    override suspend fun insertMessage(message: ChatMessageEntity) = mutex.withLock {
        messages.add(message)
        Unit
    }

    override suspend fun getMessagesBySession(sessionId: String): List<ChatMessageEntity> = mutex.withLock {
        messages.filter { it.sessionId == sessionId }.sortedBy { it.timestamp }
    }

    override suspend fun deleteMessage(id: String) = mutex.withLock {
        messages.removeAll { it.id == id }
        Unit
    }

    override suspend fun insertQuizResult(result: QuizResultEntity) = mutex.withLock {
        quizResults.add(result)
        Unit
    }

    override suspend fun getQuizResultsBySession(sessionId: String): List<QuizResultEntity> = mutex.withLock {
        quizResults.filter { it.sessionId == sessionId }
    }

    override suspend fun upsertSkill(skill: SkillEntity) = mutex.withLock {
        skills[skill.id] = skill
    }

    override suspend fun getSkillsByStudent(studentId: String): List<SkillEntity> = mutex.withLock {
        skills.values.filter { it.studentId == studentId }
    }

    override suspend fun insertDocument(document: DocumentEntity) = mutex.withLock {
        documents[document.id] = document
    }

    override suspend fun getDocument(id: String): DocumentEntity? = mutex.withLock {
        documents[id]
    }

    override suspend fun getAllDocuments(): List<DocumentEntity> = mutex.withLock {
        documents.values.toList()
    }

    override suspend fun deleteDocument(id: String) = mutex.withLock {
        documents.remove(id)
        Unit
    }
}
