package com.example.prject_gaza.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object PdfUtils {
    fun openPdf(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendEmail(context: Context, uri: Uri) {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Case Report")
            putExtra(Intent.EXTRA_TEXT, "Please find the attached case report.")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send email..."))
        } catch (e: Exception) {
            Toast.makeText(context, "Email client not found", Toast.LENGTH_SHORT).show()
        }
    }
}