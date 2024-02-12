package at.htlhl.chatnet.util

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Precision
fun preLoadImages(context: Context, imageUrls: String) {
    val request = ImageRequest.Builder(context)
        .precision(Precision.INEXACT)
        .data(imageUrls)
        .build()
    context.imageLoader.enqueue(request)
}