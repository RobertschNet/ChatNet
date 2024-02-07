package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.TagElement
import at.htlhl.chatnet.data.tags
import at.htlhl.chatnet.viewmodels.SharedViewModel

class TagSelectView {

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun TagSelectScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        val userDataState = sharedViewModel.user.collectAsState()
        val userData: FirebaseUser = userDataState.value
        Log.println(Log.INFO, "TagSelectView", "User tags: ${userData.tags}")
        val filteredTags = tags.filter { tag -> userData.tags.contains(tag.name) }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    backgroundColor = Color.White,
                    elevation = 2.dp,
                    title = {
                        Text(
                            "Select Tags",
                            color = Color.Black,
                            modifier = Modifier.clickable { navController.navigateUp() })
                    }
                )
            },
            content = {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = {
                        items(filteredTags) { tag ->
                            TagItem(tag = tag)
                        }
                    }
                )
            }
        )
    }


    @Composable
    fun TagItem(tag: TagElement) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable {}) {
            Spacer(modifier = Modifier.width(20.dp))
            Row(modifier = Modifier
                .background(tag.color.copy(alpha = 0.1f))
                .padding(10.dp)) {
                Icon(
                    imageVector = tag.icon,
                    contentDescription = null,
                    tint = tag.color,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = tag.name,
                    color = tag.color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

        }
    }

}