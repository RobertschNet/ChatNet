package at.htlhl.chatnet.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import at.chatnet.R
import coil.compose.SubcomposeAsyncImage

@Composable
fun OptionsDialog(offset: Offset?, onClose: (String) -> Unit) {
    offset?.let {
        DropdownMenu(
            expanded = true,
            properties = PopupProperties(focusable = false),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .height(130.dp)
                .width(120.dp),
            onDismissRequest = {
                onClose.invoke("closed")
            },
            offset = DpOffset(it.x.dp, it.y.dp),
        ) {
            DropdownMenuItem(
                onClick = {
                    onClose.invoke("generate")
                },
                contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp),
                modifier = Modifier.height(40.dp)

            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Generate",
                        fontWeight = FontWeight.Normal,
                        color= MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(start = 10.dp)

                    )
                    SubcomposeAsyncImage(
                        model = R.drawable.brain_illustration_12_svgrepo_com,
                        contentDescription = null,
                        colorFilter= ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .size(25.dp),

                    )
                }
            }
            DropdownMenuItem(
                onClick = {
                    onClose.invoke("copy")
                },
                contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Copy",
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        color= MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(start = 10.dp)

                    )
                    Icon(
                        imageVector = Icons.Outlined.FileCopy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(start = 42.dp)
                            .size(25.dp)
                    )
                }
            }
            DropdownMenuItem(
                onClick = {
                    onClose.invoke("delete")
                },
                contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp),
                modifier = Modifier.height(40.dp)

            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Delete",
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        tint = Color.Red,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 35.dp, end = 5.dp)
                            .size(25.dp)
                    )
                }
            }
        }
    }
}