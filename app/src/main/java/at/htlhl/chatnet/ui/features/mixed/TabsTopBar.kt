package at.htlhl.chatnet.ui.features.mixed

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.htlhl.chatnet.data.CurrentTab
import at.htlhl.chatnet.data.FirebaseUser

@Composable
fun TabsTopBar(
    tab: CurrentTab,
    friendRequests: List<FirebaseUser> = emptyList(),
    dropInState: Boolean,
    onActionClicked: () -> Unit = {},
    onUpdateSearchValue: (String) -> Unit,
) {
    val isSearchMode = remember { mutableStateOf(false) }
    TopAppBar(
        backgroundColor = MaterialTheme.colorScheme.background,
        elevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!isSearchMode.value) {
            TabsTopBarContent(tab = tab,
                dropInState = dropInState,
                friendRequests = friendRequests,
                onActivateSearchMode = { isSearchMode.value = true },
                onActionClicked = { onActionClicked() })
        } else {
            TabsTopBarSearchBarComponent(onUpdateSearchValue = { onUpdateSearchValue(it) },
                onDeactivateSearchMode = { isSearchMode.value = false })
        }
    }
}