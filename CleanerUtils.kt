package com.example.unknowncleaner

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony

object CleanerUtils {

    private fun isNumberInContacts(context: Context, number: String?): Boolean {
        if (number.isNullOrEmpty()) return false
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return false
    }

    fun cleanUnknownCalls(context: Context): Int {
        var deletedCount = 0
        val uri = CallLog.Calls.CONTENT_URI
        val projection = arrayOf(CallLog.Calls._ID, CallLog.Calls.NUMBER)
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.let {
                val idIndex = it.getColumnIndex(CallLog.Calls._ID)
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                while (it.moveToNext()) {
                    val id = it.getString(idIndex)
                    val number = it.getString(numberIndex)
                    if (!isNumberInContacts(context, number)) {
                        val deleteUri = ContentUris.withAppendedId(CallLog.Calls.CONTENT_URI, id.toLong())
                        val deleted = context.contentResolver.delete(deleteUri, null, null)
                        deletedCount += deleted
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return deletedCount
    }

    fun cleanUnknownSms(context: Context): Int {
        var deletedCount = 0
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS)
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.let {
                val idIndex = it.getColumnIndex(Telephony.Sms._ID)
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                while (it.moveToNext()) {
                    val id = it.getString(idIndex)
                    val address = it.getString(addressIndex)
                    if (!isNumberInContacts(context, address)) {
                        val deleteUri = ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, id.toLong())
                        val deleted = context.contentResolver.delete(deleteUri, null, null)
                        deletedCount += deleted
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return deletedCount
    }
}
