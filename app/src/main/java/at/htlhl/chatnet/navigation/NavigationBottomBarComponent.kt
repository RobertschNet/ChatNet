package at.htlhl.chatnet.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import at.htlhl.chatnet.data.BottomNavItem
import coil.compose.rememberAsyncImagePainter


@Composable
fun NavigationBottomBarComponent(
    isEnabled: Boolean,
    items: List<BottomNavItem>,
    navController: NavController,
    onItemClick: (BottomNavItem) -> Unit
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    if (isEnabled) {
        Card(
            shape = RectangleShape,
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(55.dp),
            elevation = 2.dp,
        ) {
            NavigationBar(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Spacer(modifier = Modifier.width(10.dp))
                items.forEach { item ->
                    val selected = item.route == backStackEntry.value?.destination?.route
                    NavigationBarItem(
                        icon = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (selected) {
                                    Image(
                                        painter = rememberAsyncImagePainter(item.icon),
                                        contentDescription = item.name,
                                        colorFilter = ColorFilter.tint(item.color),
                                        modifier = Modifier.size(38.dp),
                                    )
                                    Text(
                                        text = item.name,
                                        color = item.color,
                                        textAlign = TextAlign.Center,
                                        fontSize = 10.sp
                                    )
                                } else {
                                    Image(
                                        painter = rememberAsyncImagePainter(item.icon),
                                        contentDescription = item.name,
                                        modifier = Modifier.size(30.dp),
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                    )
                                }
                            }
                        },
                        selected = selected,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.background,
                        ),
                        onClick = {
                            onItemClick(item)
                        },
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}
