package at.htlhl.chatnet.util

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest

fun loadImage(context: Context, imageUrl: String) {
    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .build()
    context.imageLoader.enqueue(request)
}