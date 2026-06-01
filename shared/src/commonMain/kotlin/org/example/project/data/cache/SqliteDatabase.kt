package org.example.project.data.cache

class SqliteDatabase(private val name: String = "tutoriaiad") : LocalDatabase {
    private val db = createSqlDatabase(name)

    init {
        createTables()
    }

    private fun createTables() {
        db.execute("""
            CREATE TABLE IF NOT EXISTS students (
                id TEXT PRIMARY KEY, name TEXT, level TEXT,
                study_time_minutes INTEGER DEFAULT 0,
                total_quizzes_taken INTEGER DEFAULT 0,
                average_score REAL DEFAULT 0.0
            )
        """)
        db.execute("""
            CREATE TABLE IF NOT EXISTS sessions (
                id TEXT PRIMARY KEY, student_id TEXT, start_time INTEGER,
                end_time INTEGER, subject TEXT, message_count INTEGER DEFAULT 0
            )
        """)
        db.execute("""
            CREATE TABLE IF NOT EXISTS messages (
                id TEXT PRIMARY KEY, session_id TEXT, role TEXT, content TEXT,
                timestamp INTEGER, source_file TEXT, file_type TEXT,
                page_number INTEGER, local_uri TEXT, mcp_widget_data TEXT
            )
        """)
        db.execute("""
            CREATE TABLE IF NOT EXISTS quiz_results (
                id TEXT PRIMARY KEY, session_id TEXT, question TEXT,
                options TEXT, correct_answer_index INTEGER,
                selected_answer_index INTEGER, is_correct INTEGER DEFAULT 0,
                timestamp INTEGER
            )
        """)
        db.execute("""
            CREATE TABLE IF NOT EXISTS skills (
                id TEXT PRIMARY KEY, student_id TEXT, name TEXT,
                category TEXT, proficiency REAL DEFAULT 0.0, last_updated INTEGER
            )
        """)
        db.execute("""
            CREATE TABLE IF NOT EXISTS documents (
                id TEXT PRIMARY KEY, file_name TEXT, file_type TEXT,
                local_uri TEXT, page_count INTEGER, indexed_at INTEGER
            )
        """)
    }

    override suspend fun insertStudent(student: StudentEntity) {
        db.execute("INSERT OR REPLACE INTO students VALUES (?,?,?,?,?,?)",
            listOf(student.id, student.name, student.level, student.studyTimeMinutes,
                student.totalQuizzesTaken, student.averageScore))
    }

    override suspend fun getStudent(id: String): StudentEntity? {
        val rows = db.query("SELECT * FROM students WHERE id = ?", listOf(id))
        if (rows.isEmpty()) return null
        val r = rows.first()
        return StudentEntity(
            id = str(r, "id") ?: "", name = str(r, "name") ?: "", level = str(r, "level") ?: "",
            studyTimeMinutes = lng(r, "study_time_minutes"),
            totalQuizzesTaken = int(r, "total_quizzes_taken"),
            averageScore = dbl(r, "average_score"))
    }

    override suspend fun getAllStudents(): List<StudentEntity> {
        return db.query("SELECT * FROM students").map { r ->
            StudentEntity(id = str(r, "id") ?: "", name = str(r, "name") ?: "", level = str(r, "level") ?: "",
                studyTimeMinutes = lng(r, "study_time_minutes"),
                totalQuizzesTaken = int(r, "total_quizzes_taken"),
                averageScore = dbl(r, "average_score"))
        }
    }

    override suspend fun updateStudent(student: StudentEntity) = insertStudent(student)

    override suspend fun insertSession(session: StudySessionEntity) {
        db.execute("INSERT OR REPLACE INTO sessions VALUES (?,?,?,?,?,?)",
            listOf(session.id, session.studentId, session.startTime,
                session.endTime, session.subject, session.messageCount))
    }

    override suspend fun getSession(id: String): StudySessionEntity? {
        val rows = db.query("SELECT * FROM sessions WHERE id = ?", listOf(id))
        if (rows.isEmpty()) return null
        val r = rows.first()
        return StudySessionEntity(id = str(r, "id") ?: "", studentId = str(r, "student_id") ?: "",
            startTime = lng(r, "start_time"), endTime = lng(r, "end_time"),
            subject = str(r, "subject"), messageCount = int(r, "message_count"))
    }

    override suspend fun getSessionsByStudent(studentId: String): List<StudySessionEntity> {
        return db.query("SELECT * FROM sessions WHERE student_id = ? ORDER BY start_time DESC", listOf(studentId)).map { r ->
            StudySessionEntity(id = str(r, "id") ?: "", studentId = str(r, "student_id") ?: "",
                startTime = lng(r, "start_time"), endTime = lng(r, "end_time"),
                subject = str(r, "subject"), messageCount = int(r, "message_count"))
        }
    }

    override suspend fun updateSession(session: StudySessionEntity) = insertSession(session)

    override suspend fun insertMessage(message: ChatMessageEntity) {
        db.execute("""INSERT OR REPLACE INTO messages VALUES (?,?,?,?,?,?,?,?,?,?)""",
            listOf(message.id, message.sessionId, message.role, message.content,
                message.timestamp, message.sourceFile, message.fileType,
                message.pageNumber, message.localUri, message.mcpWidgetData))
    }

    override suspend fun getMessagesBySession(sessionId: String): List<ChatMessageEntity> {
        return db.query("SELECT * FROM messages WHERE session_id = ? ORDER BY timestamp", listOf(sessionId)).map { r ->
            ChatMessageEntity(id = str(r, "id") ?: "", sessionId = str(r, "session_id") ?: "",
                role = str(r, "role") ?: "", content = str(r, "content") ?: "",
                timestamp = lng(r, "timestamp"), sourceFile = str(r, "source_file"),
                fileType = str(r, "file_type"), pageNumber = int(r, "page_number"),
                localUri = str(r, "local_uri"), mcpWidgetData = str(r, "mcp_widget_data"))
        }
    }

    override suspend fun deleteMessage(id: String) {
        db.execute("DELETE FROM messages WHERE id = ?", listOf(id))
    }

    override suspend fun insertQuizResult(result: QuizResultEntity) {
        db.execute("INSERT OR REPLACE INTO quiz_results VALUES (?,?,?,?,?,?,?,?)",
            listOf(result.id, result.sessionId, result.question, result.options,
                result.correctAnswerIndex, result.selectedAnswerIndex,
                if (result.isCorrect) 1 else 0, result.timestamp))
    }

    override suspend fun getQuizResultsBySession(sessionId: String): List<QuizResultEntity> {
        return db.query("SELECT * FROM quiz_results WHERE session_id = ?", listOf(sessionId)).map { r ->
            QuizResultEntity(id = str(r, "id") ?: "", sessionId = str(r, "session_id") ?: "",
                question = str(r, "question") ?: "", options = str(r, "options") ?: "",
                correctAnswerIndex = int(r, "correct_answer_index"),
                selectedAnswerIndex = int(r, "selected_answer_index"),
                isCorrect = int(r, "is_correct") == 1,
                timestamp = lng(r, "timestamp"))
        }
    }

    override suspend fun upsertSkill(skill: SkillEntity) {
        db.execute("INSERT OR REPLACE INTO skills VALUES (?,?,?,?,?,?)",
            listOf(skill.id, skill.studentId, skill.name, skill.category,
                skill.proficiency, skill.lastUpdated))
    }

    override suspend fun getSkillsByStudent(studentId: String): List<SkillEntity> {
        return db.query("SELECT * FROM skills WHERE student_id = ?", listOf(studentId)).map { r ->
            SkillEntity(id = str(r, "id") ?: "", studentId = str(r, "student_id") ?: "",
                name = str(r, "name") ?: "", category = str(r, "category") ?: "",
                proficiency = dbl(r, "proficiency"), lastUpdated = lng(r, "last_updated"))
        }
    }

    override suspend fun insertDocument(document: DocumentEntity) {
        db.execute("INSERT OR REPLACE INTO documents VALUES (?,?,?,?,?,?)",
            listOf(document.id, document.fileName, document.fileType,
                document.localUri, document.pageCount, document.indexedAt))
    }

    override suspend fun getDocument(id: String): DocumentEntity? {
        val rows = db.query("SELECT * FROM documents WHERE id = ?", listOf(id))
        if (rows.isEmpty()) return null
        val r = rows.first()
        return DocumentEntity(id = str(r, "id") ?: "", fileName = str(r, "file_name") ?: "",
            fileType = str(r, "file_type") ?: "", localUri = str(r, "local_uri") ?: "",
            pageCount = int(r, "page_count"), indexedAt = lng(r, "indexed_at"))
    }

    override suspend fun getAllDocuments(): List<DocumentEntity> {
        return db.query("SELECT * FROM documents ORDER BY indexed_at DESC").map { r ->
            DocumentEntity(id = str(r, "id") ?: "", fileName = str(r, "file_name") ?: "",
                fileType = str(r, "file_type") ?: "", localUri = str(r, "local_uri") ?: "",
                pageCount = int(r, "page_count"), indexedAt = lng(r, "indexed_at"))
        }
    }

    override suspend fun deleteDocument(id: String) {
        db.execute("DELETE FROM documents WHERE id = ?", listOf(id))
    }

    fun close() { db.close() }

    private fun str(row: Map<String, Any?>, col: String): String? = row[col] as? String
    private fun lng(row: Map<String, Any?>, col: String): Long = (row[col] as? Number)?.toLong() ?: 0L
    private fun int(row: Map<String, Any?>, col: String): Int = (row[col] as? Number)?.toInt() ?: 0
    private fun dbl(row: Map<String, Any?>, col: String): Double = (row[col] as? Number)?.toDouble() ?: 0.0
}
