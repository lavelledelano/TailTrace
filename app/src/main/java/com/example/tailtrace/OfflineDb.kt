package com.example.tailtrace

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson

class OfflineDb(ctx: Context) : SQLiteOpenHelper(ctx, "tailtrace_offline.db", null, 1) {
    private val gson = Gson()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE cached_pets (id TEXT PRIMARY KEY, json TEXT NOT NULL)")
        db.execSQL("CREATE TABLE pending_pets (local_id INTEGER PRIMARY KEY AUTOINCREMENT, json TEXT NOT NULL)")
        db.execSQL("CREATE TABLE pending_sightings (local_id INTEGER PRIMARY KEY AUTOINCREMENT, json TEXT NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS cached_pets")
        db.execSQL("DROP TABLE IF EXISTS pending_pets")
        db.execSQL("DROP TABLE IF EXISTS pending_sightings")
        onCreate(db)
    }

    // ---- saved copy of the nearby pets (for reading offline) ----
    fun savePets(list: List<Pet>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("cached_pets", null, null)
            list.forEach { p ->
                val v = ContentValues()
                v.put("id", p.id)
                v.put("json", gson.toJson(p))
                db.insertWithOnConflict("cached_pets", null, v, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun loadPets(): List<Pet> {
        val out = mutableListOf<Pet>()
        readableDatabase.rawQuery("SELECT json FROM cached_pets", null).use { c ->
            while (c.moveToNext()) out.add(gson.fromJson(c.getString(0), Pet::class.java))
        }
        return out
    }

    // ---- queue of things created offline (waiting to sync) ----
    fun queuePet(p: NewPet) = insert("pending_pets", gson.toJson(p))
    fun queueSighting(s: NewSighting) = insert("pending_sightings", gson.toJson(s))

    private fun insert(table: String, json: String) {
        val v = ContentValues()
        v.put("json", json)
        writableDatabase.insert(table, null, v)
    }

    fun pendingPets(): List<Pair<Long, NewPet>> = readPending("pending_pets", NewPet::class.java)
    fun pendingSightings(): List<Pair<Long, NewSighting>> = readPending("pending_sightings", NewSighting::class.java)

    private fun <T> readPending(table: String, cls: Class<T>): List<Pair<Long, T>> {
        val out = mutableListOf<Pair<Long, T>>()
        readableDatabase.rawQuery("SELECT local_id, json FROM " + table + " ORDER BY local_id", null).use { c ->
            while (c.moveToNext()) out.add(c.getLong(0) to gson.fromJson(c.getString(1), cls))
        }
        return out
    }

    fun removePet(id: Long) { writableDatabase.delete("pending_pets", "local_id = ?", arrayOf(id.toString())) }
    fun removeSighting(id: Long) { writableDatabase.delete("pending_sightings", "local_id = ?", arrayOf(id.toString())) }

    fun pendingCount(): Int {
        var total = 0
        listOf("pending_pets", "pending_sightings").forEach { t ->
            readableDatabase.rawQuery("SELECT COUNT(*) FROM " + t, null).use { c ->
                if (c.moveToFirst()) total += c.getInt(0)
            }
        }
        return total
    }
}