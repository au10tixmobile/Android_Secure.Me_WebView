package com.example.secureme

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.webkit.JavascriptInterface

class LocalStorageJavaScriptInterface(c: Context) {
    private val mContext: Context = c
    private val localStorageDBHelper: LocalStorage = LocalStorage.getInstance(mContext)
    private lateinit var database: SQLiteDatabase

    /**
     * This method allows to get an item for the given key
     * @param key : the key to look for in the local storage
     * @return the item having the given key
     */
    @JavascriptInterface
    fun getItem(key: String?): String? {
        var value: String? = null
        if (key != null) {
            database = localStorageDBHelper.readableDatabase
            val cursor: Cursor = database.query(
                LocalStorage.LOCALSTORAGE_TABLE_NAME,
                null,
                LocalStorage.LOCALSTORAGE_ID + " = ?", arrayOf(key), null, null, null
            )
            if (cursor.moveToFirst()) {
                value = cursor.getString(1)
            }
            cursor.close()
            database.close()
        }
        return value
    }

    /**
     * set the value for the given key, or create the set of datas if the key does not exist already.
     * @param key
     * @param value
     */
    @JavascriptInterface
    fun setItem(key: String?, value: String?) {
        if (key != null && value != null) {
            val oldValue = getItem(key)
            database = localStorageDBHelper.writableDatabase
            val values = ContentValues()
            values.put(LocalStorage.LOCALSTORAGE_ID, key)
            values.put(LocalStorage.LOCALSTORAGE_VALUE, value)
            if (oldValue != null) {
                database.update(
                    LocalStorage.LOCALSTORAGE_TABLE_NAME,
                    values,
                    LocalStorage.LOCALSTORAGE_ID + "='" + key + "'",
                    null
                )
            } else {
                database.insert(LocalStorage.LOCALSTORAGE_TABLE_NAME, null, values)
            }
            database.close()
        }
    }

    /**
     * removes the item corresponding to the given key
     * @param key
     */
    @JavascriptInterface
    fun removeItem(key: String?) {
        if (key != null) {
            database = localStorageDBHelper.writableDatabase
            database.delete(
                LocalStorage.LOCALSTORAGE_TABLE_NAME,
                LocalStorage.LOCALSTORAGE_ID + "='" + key + "'",
                null
            )
            database.close()
        }
    }

    /**
     * clears all the local storage.
     */
    @JavascriptInterface
    fun clear() {
        database = localStorageDBHelper.writableDatabase
        database.delete(LocalStorage.LOCALSTORAGE_TABLE_NAME, null, null)
        database.close()
    }

}
