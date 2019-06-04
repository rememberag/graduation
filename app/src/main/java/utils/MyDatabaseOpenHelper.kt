package utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class MyDatabaseOpenHelper private constructor(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "MyDatabase", null, 1) {
    init {
        instance = this
    }

    companion object {
        private var instance: MyDatabaseOpenHelper? = null

        @Synchronized
        fun getInstance(ctx: Context) = instance ?: MyDatabaseOpenHelper(ctx.applicationContext)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Here you create tables
        db.createTable("Songs", true,
            "id" to INTEGER + PRIMARY_KEY + UNIQUE + AUTOINCREMENT,
            "singer" to TEXT,
            "songName" to TEXT,
            "path" to TEXT,
            "duration" to INTEGER,
            "size" to INTEGER,
            "albumId" to TEXT)
        db.createTable("PlayList", true,
            "id" to INTEGER + PRIMARY_KEY + UNIQUE + AUTOINCREMENT,
            "name" to TEXT)
        db.createTable("Relation", true,
            "id" to INTEGER + PRIMARY_KEY + UNIQUE + AUTOINCREMENT,
            "songId" to INTEGER,
            "listId" to INTEGER)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Here you can upgrade tables, as usual
        db.dropTable("User", true)
    }
}

// Access property for Context
val Context.database: MyDatabaseOpenHelper
    get() = MyDatabaseOpenHelper.getInstance(this)
