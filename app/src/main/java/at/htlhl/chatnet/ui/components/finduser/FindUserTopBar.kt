package at.htlhl.chatnet.ui.components.finduser

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.navigation.Screens
import coil.compose.SubcomposeAsyncImage

@Composable
fun FindUserTopBar(
    navController: NavController,
    interactionSource: MutableInteractionSource,
    persons: List<FirebaseUser>,
    searchText: String,
    onClicked: () -> Unit,
    onTextChanged: (String) -> Unit
) {
    var search by rememberSaveable { mutableStateOf(true) }
    TopAppBar(backgroundColor = Color.White, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(
                    top = 8.dp,
                    end = 20.dp,
                    bottom = 10.dp
                )
                .fillMaxWidth()
        ) {
            IconButton(onClick = { navController.navigate(Screens.ChatsViewScreen.route) }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            BasicTextField(
                value = searchText,
                onValueChange = {
                   onTextChanged.invoke(it)
                    search = it.isEmpty()
                },
                interactionSource = interactionSource,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE9E9E9), RoundedCornerShape(12.dp)),
                textStyle = MaterialTheme.typography.body1.copy(color = Color.Black),
                cursorBrush = SolidColor(Color.Black),
                decorationBox = { innerTextField: @Composable () -> Unit ->
                    Text(
                        text = if (search) "Search" else "",
                        modifier = Modifier.padding(top = 8.4.dp, start = 48.dp),
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = Color(0xFFBDBDBD)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SubcomposeAsyncImage(
                            model = R.drawable.search_4_svgrepo_com,
                            contentDescription = "Search Icon",
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .size(26.dp),
                            colorFilter = ColorFilter.tint(
                                Color(0xFFBDBDBD)
                            )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 9.dp, top = 8.dp, end = 6.dp)
                                .height(30.dp)
                        ) {
                            innerTextField()
                        }
                    }
                })
        }
    }
    if (interactionSource.collectIsPressedAsState().value) {
        onClicked.invoke()
    }
}