package com.vocaby.application.core.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.net.toFile

object Parser {
    fun parseFileExtensionFromUri(uri: Uri, contentResolver: ContentResolver): String {
        var extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))

        if (extension == null) {
            if (uri.scheme.equals("content")) {
                val cursor = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                cursor.use { c ->
                    if (c != null && c.moveToFirst()) {
                        val index = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index == -1) c.getColumnIndex("_data")

                        val name = c.getString(index)
                        extension = name.substringAfterLast(".", "")
                    }
                }
            } else if (uri.scheme.equals("file")) {
                extension = uri.toFile().extension
            }
        }

        return extension?.lowercase() ?: ""
    }
}