package com.example.register

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object YouTubeHelper {

    fun openVideo(context: Context, videoId: String) {
        val webUri = Uri.parse("https://www.youtube.com/watch?v=$videoId")
        val appUri = Uri.parse("vnd.youtube:$videoId")

        val appIntent = Intent(Intent.ACTION_VIEW, appUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(appIntent)
        } catch (_: ActivityNotFoundException) {
            try {
                context.startActivity(webIntent)
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, R.string.youtube_open_error, Toast.LENGTH_LONG).show()
            }
        }
    }
}
