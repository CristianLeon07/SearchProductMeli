package com.example.pruebameli.presentation.auth


import android.content.Context
import android.content.Intent
import android.net.Uri

object AuthLoginLauncher {

    /**
     * Abre el navegador (o app compatible) con la URL de autorización.
     * Vive fuera del ViewModel porque usa Android framework (Context/Intent).
     */
    fun open(context: Context, url: Uri) {
        val i = Intent(Intent.ACTION_VIEW, url).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(i)
    }
}
