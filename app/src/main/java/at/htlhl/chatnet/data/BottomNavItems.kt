package at.htlhl.chatnet.data

import androidx.compose.ui.graphics.Color

data class BottomNavItems(
    val name: String, // name of the item
    val route: String, // route the item leads to
    val icon: Int, // icon of the item
    val color: Color // color of the item
)
