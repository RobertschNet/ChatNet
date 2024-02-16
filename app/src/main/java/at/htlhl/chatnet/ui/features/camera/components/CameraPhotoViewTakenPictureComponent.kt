package at.htlhl.chatnet.ui.features.camera.components

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp

@Composable
fun CameraPhotoViewTakenPictureComponent(
    paddingValues:PaddingValues,
    bitmap: Bitmap,
    onSaveImageToGalleryClicked: () -> Unit,
    onRetakePhotoClicked: () -> Unit,
) {
    Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
        Spacer(modifier = Modifier.height(5.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    onRetakePhotoClicked()

                }, modifier = Modifier.padding(start = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    tint = Color.White,
                    contentDescription = null
                )
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            IconButton(
                onClick = {
                    onSaveImageToGalleryClicked()
                }, modifier = Modifier.padding(end = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    tint = Color.White,
                    contentDescription = null
                )
            }
        }
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Camera Photo",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )

    }
}