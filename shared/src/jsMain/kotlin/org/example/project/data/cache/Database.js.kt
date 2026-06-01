package org.example.project.data.cache

actual class SqlDatabase(private val name: String) {
    private val storage = mutableMapOf<String, MutableList<Map<String, Any?>>>()

    actual fun execute(sql: String, bindings: List<Any?> ) {
        // JS in-memory no-op for now
    }

    actual fun query(sql: String, bindings: List<Any?>): List<Map<String, Any?>> {
        val tableName = extractTableName(sql) ?: return emptyList()
        return storage[tableName] ?: emptyList()
    }

    actual fun close() {}

    private fun extractTableName(sql: String): String? {
        val keywords = listOf("FROM", "INTO", "TABLE")
        val upper = sql.uppercase()
        for (kw in keywords) {
            val idx = upper.indexOf(kw)
            if (idx >= 0) {
                val after = sql.substring(idx + kw.length).trim().split(" ", "(", "\n")[0].trim('"', '`')
                return after
            }
        }
        return null
    }
}

actual fun createSqlDatabase(name: String): SqlDatabase = SqlDatabase(name)
