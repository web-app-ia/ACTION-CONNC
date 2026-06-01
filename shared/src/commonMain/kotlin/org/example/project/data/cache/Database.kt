package org.example.project.data.cache

expect class SqlDatabase {
    fun execute(sql: String, bindings: List<Any?> = emptyList())
    fun query(sql: String, bindings: List<Any?> = emptyList()): List<Map<String, Any?>>
    fun close()
}

expect fun createSqlDatabase(name: String): SqlDatabase
