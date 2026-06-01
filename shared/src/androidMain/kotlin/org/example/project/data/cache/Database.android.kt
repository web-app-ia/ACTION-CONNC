package org.example.project.data.cache

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private var appContext: Context? = null

fun initAndroidSqlite(context: Context) { appContext = context }

actual class SqlDatabase(private val db: SQLiteDatabase) {
    actual fun execute(sql: String, bindings: List<Any?>) {
        db.execSQL(sql, bindings.toTypedArray())
    }

    actual fun query(sql: String, bindings: List<Any?>): List<Map<String, Any?>> {
        val cursor = db.rawQuery(sql, bindings.map { it?.toString() }.toTypedArray())
        val rows = mutableListOf<Map<String, Any?>>()
        cursor.use { c ->
            val cols = c.columnNames
            while (c.moveToNext()) {
                rows.add(cols.associate { name ->
                    val idx = c.getColumnIndex(name)
                    name to if (idx >= 0) c.getString(idx) else null
                })
            }
        }
        return rows
    }

    actual fun close() { db.close() }
}

actual fun createSqlDatabase(name: String): SqlDatabase {
    val ctx = appContext ?: throw IllegalStateException("Call initAndroidSqlite(context) first")
    val helper = object : SQLiteOpenHelper(ctx, "$name.db", null, 1) {
        override fun onCreate(d: SQLiteDatabase) {}
        override fun onUpgrade(d: SQLiteDatabase, o: Int, n: Int) {}
    }
    return SqlDatabase(helper.writableDatabase)
}
