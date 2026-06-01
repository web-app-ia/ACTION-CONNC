package org.example.project.data.cache

interface LocalDatabase {
    suspend fun insertStudent(student: StudentEntity)
    suspend fun getStudent(id: String): StudentEntity?
    suspend fun getAllStudents(): List<StudentEntity>
    suspend fun updateStudent(student: StudentEntity)

    suspend fun insertSession(session: StudySessionEntity)
    suspend fun getSession(id: String): StudySessionEntity?
    suspend fun getSessionsByStudent(studentId: String): List<StudySessionEntity>
    suspend fun updateSession(session: StudySessionEntity)

    suspend fun insertMessage(message: ChatMessageEntity)
    suspend fun getMessagesBySession(sessionId: String): List<ChatMessageEntity>
    suspend fun deleteMessage(id: String)

    suspend fun insertQuizResult(result: QuizResultEntity)
    suspend fun getQuizResultsBySession(sessionId: String): List<QuizResultEntity>

    suspend fun upsertSkill(skill: SkillEntity)
    suspend fun getSkillsByStudent(studentId: String): List<SkillEntity>

    suspend fun insertDocument(document: DocumentEntity)
    suspend fun getDocument(id: String): DocumentEntity?
    suspend fun getAllDocuments(): List<DocumentEntity>
    suspend fun deleteDocument(id: String)
}
