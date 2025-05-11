package quran.shakir

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.File

object MyDb {


    private var _db: SQLiteDatabase? = null;
    fun db(): SQLiteDatabase {
        if (_db != null) return _db!!
        val dbFile = File(appInstance.filesDir, "trans_malayalam_db_v2")
        if (!dbFile.exists()) {
            dbFile.createNewFile() // Create the file if it doesn't exist
        }
        val db = SQLiteDatabase.openDatabase(
            dbFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE
        )

        // Create table
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS trans_malayalam_table (fileName TEXT, page INTEGER, content TEXT)")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            db.execSQL(
                """
        CREATE TABLE IF NOT EXISTS currentStateTable (
            fileName TEXT NOT NULL UNIQUE,
            page INTEGER NOT NULL DEFAULT 0,
            isTranslation INTEGER NOT NULL DEFAULT 0,
            scrollState INTEGER NOT NULL DEFAULT 0
        )
        """.trimIndent()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }




        println("dbbbbbbbbb  open.")
        _db = db;
        return db
    }

    fun closeDatabase() {
        try {
            _db?.let {
                if (it.isOpen) {
                    it.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _db = null
        println("dbbbbbbbbb  close.")
    }


    fun setPageState(fileName: String, page: Int, isTranslation: Boolean, scrollState: Int) {
        val sql = """INSERT OR REPLACE INTO currentStateTable (fileName, page, isTranslation, scrollState) VALUES (?, ?, ?, ?)"""
        try {
            db().execSQL(sql, arrayOf(fileName, page, if(isTranslation) 1 else 0, scrollState))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPageState(fileName: String): PageState? {
        val sql = "SELECT page, isTranslation, scrollState FROM currentStateTable WHERE fileName = ?"
        var cursor: Cursor? = null

        return try {
            cursor = db().rawQuery(sql, arrayOf(fileName))
            if (cursor.moveToFirst()) {
                val page = cursor.getInt(cursor.getColumnIndexOrThrow("page"))
                val isTranslation = cursor.getInt(cursor.getColumnIndexOrThrow("isTranslation")) == 1
                val scrollState = cursor.getInt(cursor.getColumnIndexOrThrow("scrollState"))
                PageState(page, isTranslation, scrollState)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            cursor?.close()
        }
    }






}


data class PageState(
    val page: Int,
    val isTranslation: Boolean,
    val scrollState: Int
)


