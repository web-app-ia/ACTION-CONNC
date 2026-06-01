package org.example.project.data.cache

import java.io.File
import java.sql.DriverManager

actual class SqlDatabase(private val connection: java.sql.Connection) {
    actual fun execute(sql: String, bindings: List<Any?>) {
        connection.prepareStatement(sql).use { stmt ->
            bindings.forEachIndexed { i, v -> stmt.setObject(i + 1, v) }
            stmt.execute()
        }
    }

    actual fun query(sql: String, bindings: List<Any?>): List<Map<String, Any?>> {
        connection.prepareStatement(sql).use { stmt ->
            bindings.forEachIndexed { i, v -> stmt.setObject(i + 1, v) }
            stmt.executeQuery().use { rs ->
                val meta = rs.metaData
                val cols = (1..meta.columnCount).map { meta.getColumnName(it) }
                val rows = mutableListOf<Map<String, Any?>>()
                while (rs.next()) {
                    rows.add(cols.associateWith { rs.getObject(it) })
                }
                return rows
            }
        }
    }

    actual fun close() { connection.close() }
}

actual fun createSqlDatabase(name: String): SqlDatabase {
    val dbDir = File(System.getProperty("user.home"), ".tutoriaiad")
    dbDir.mkdirs()
    val url = "jdbc:sqlite:${File(dbDir, "$name.db").absolutePath}"
    val conn = DriverManager.getConnection(url)
    return SqlDatabase(conn)
}
