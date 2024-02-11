package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.TagElement
import at.htlhl.chatnet.data.tags
import at.htlhl.chatnet.ui.features.mixed.TagElement
import at.htlhl.chatnet.viewmodels.SharedViewModel

class TagSelectView {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun TagSelectScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        val userDataState = sharedViewModel.user.collectAsState()
        val userData: FirebaseUser = userDataState.value
        val filteredTags = tags.filter { tag -> userData.tags.contains(tag.name) && tag.category != "Empty" }
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val context = LocalContext.current
        var selectedTags by remember { mutableStateOf(filteredTags) }
        Scaffold(
            backgroundColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    backgroundColor = MaterialTheme.colorScheme.background,
                    elevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable { navController.navigateUp() }
                        )
                        Text(
                            text = "Select Tags",
                            fontWeight = FontWeight.Medium,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
            },
            content = {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    content = {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 10.dp)
                            ) {
                                Text(
                                    text = "Selected Tags: ${selectedTags.size}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    when {
                                        selectedTags.size >= 2 -> {
                                            TagElement(
                                                element = selectedTags[0].name,
                                                color = selectedTags[0].color,
                                                icon = selectedTags[0].icon,
                                                smallSize = false
                                            )
                                            TagElement(
                                                element = selectedTags[1].name,
                                                color = selectedTags[1].color,
                                                icon = selectedTags[1].icon,
                                                smallSize = false
                                            )
                                        }

                                        selectedTags.size == 1 -> {
                                            TagElement(
                                                element = selectedTags[0].name,
                                                color = selectedTags[0].color,
                                                icon = selectedTags[0].icon,
                                                smallSize = false
                                            )
                                        }

                                        else -> {
                                            TagElement(
                                                element = "No Tags",
                                                color = Color.Gray,
                                                icon = Icons.Default.RemoveCircleOutline,
                                                smallSize = false
                                            )
                                        }
                                    }
                                }
                                if (selectedTags.size > 2) {
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        when {
                                            selectedTags.size >= 4 -> {
                                                TagElement(
                                                    element = selectedTags[2].name,
                                                    color = selectedTags[2].color,
                                                    icon = selectedTags[2].icon,
                                                    smallSize = false
                                                )
                                                TagElement(
                                                    element = selectedTags[3].name,
                                                    color = selectedTags[3].color,
                                                    icon = selectedTags[3].icon,
                                                    smallSize = false
                                                )
                                            }

                                            selectedTags.size == 3 -> {
                                                TagElement(
                                                    element = selectedTags[2].name,
                                                    color = selectedTags[2].color,
                                                    icon = selectedTags[2].icon,
                                                    smallSize = false
                                                )
                                            }
                                        }
                                    }
                                }
                                if (selectedTags.size == 5) {
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        TagElement(
                                            element = selectedTags[4].name,
                                            color = selectedTags[4].color,
                                            icon = selectedTags[4].icon,
                                            smallSize = false
                                        )
                                    }
                                }
                                Text(
                                    text = "Choose up to 5 tags that represent you and help others get to know you better.",
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .padding(top = 10.dp)
                                        .width((screenWidth * 2) / 3)
                                )
                                Button(
                                    modifier = Modifier.padding(top = 5.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFF00A0E8
                                        ), contentColor = Color.White
                                    ),
                                    onClick = {
                                        sharedViewModel.updateUserTags(selectedTags.map { it.name })
                                        navController.navigateUp()
                                    }
                                ) {
                                    Text(
                                        text = "Save Tags",
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                }
                            }
                        }

                        item {
                            TagCategory(
                                context,
                                tags.filter { it.category == "Leisure" },
                                selectedTags
                            ) { selectedTags = it }
                        }
                        item {
                            TagCategory(
                                context,
                                tags.filter { it.category == "Sports" },
                                selectedTags
                            ) { selectedTags = it }
                        }
                        item {
                            TagCategory(
                                context,
                                tags.filter { it.category == "Lifestyle" },
                                selectedTags
                            ) { selectedTags = it }
                        }
                        item {
                            TagCategory(
                                context,
                                tags.filter { it.category == "Interests" },
                                selectedTags
                            ) { selectedTags = it }
                        }
                        item {
                            TagCategory(
                                context,
                                tags.filter { it.category == "Pets" },
                                selectedTags
                            ) { selectedTags = it }
                        }
                    }
                )
            }
        )
    }

    @Composable
    fun TagCategory(
        context: Context,
        tags: List<TagElement>,
        selectedTags: List<TagElement>,
        onTagSelectionChanged: (List<TagElement>) -> Unit
    ) {
        Column {
            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                text = tags.firstOrNull()?.category ?: "",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                fontSize = 16.sp,
                modifier = Modifier.padding(10.dp)
            )
            tags.forEach { tag ->
                TagItem(tag = tag, isSelected = selectedTags.contains(tag)) {
                    val updatedSelection = if (selectedTags.contains(tag)) {
                        selectedTags - tag
                    } else {
                        if (selectedTags.size < 5) {
                            selectedTags + tag
                        } else {
                            Toast.makeText(
                                context,
                                "You can only select up to 5 tags",
                                Toast.LENGTH_SHORT
                            ).show()
                            selectedTags
                        }
                    }
                    onTagSelectionChanged(updatedSelection)
                }
            }
        }
    }

    @Composable
    fun TagItem(tag: TagElement, isSelected: Boolean, onTagClick: () -> Unit) {
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onTagClick)
        ) {
            Spacer(modifier = Modifier.width(20.dp))
            Row(
                modifier = Modifier
                    .background(
                        color = if (isSelected) {
                            tag.color.copy(alpha = 0.3f)
                        } else {
                            Color.Transparent
                        }, shape = RoundedCornerShape(24.dp)
                    )
                    .padding(10.dp)
            ) {
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