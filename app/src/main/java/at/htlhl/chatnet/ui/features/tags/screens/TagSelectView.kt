package at.htlhl.chatnet.ui.features.tags.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.TagCategoryState
import at.htlhl.chatnet.data.tags
import at.htlhl.chatnet.ui.features.mixed.TagElement
import at.htlhl.chatnet.ui.features.tags.components.TagCategorySection
import at.htlhl.chatnet.ui.features.tags.viewmodels.TagSelectViewModel
import at.htlhl.chatnet.util.getPersonTagsList
import at.htlhl.chatnet.viewmodels.SharedViewModel

class TagSelectView {
    @Composable
    fun TagSelectScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        val tagSelectViewModel = viewModel<TagSelectViewModel>()
        val userDataState by sharedViewModel.user.collectAsState()
        val userData: FirebaseUser = userDataState
        val filteredTags = getPersonTagsList(personData = userData)
        val selectedTagsList = filteredTags.filter { tag -> tag.category != TagCategoryState.EMPTY }
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val context = LocalContext.current
        var selectedTags by remember { mutableStateOf(selectedTagsList) }
        Scaffold(backgroundColor = MaterialTheme.colorScheme.background,
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
                        Icon(imageVector = Icons.Default.ArrowBack,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable { navController.navigateUp() })
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
            content = { paddingValues ->
                LazyColumn(modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()), content = {
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
                            Button(modifier = Modifier.padding(top = 5.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF00A0E8
                                    ), contentColor = Color.White
                                ),
                                onClick = {
                                    tagSelectViewModel.updateUserTagList(userData = userData,
                                        tags = selectedTags.map { it.name.lowercase() })
                                    navController.navigateUp()
                                }) {
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
                        TagCategorySection(context = context,
                            tags = tags.filter { it.category == TagCategoryState.LEISURE },
                            selectedTags = selectedTags,
                            onTagSelectionChanged = { selectedTags = it })
                    }
                    item {
                        TagCategorySection(context = context,
                            tags = tags.filter { it.category == TagCategoryState.SPORTS },
                            selectedTags = selectedTags,
                            onTagSelectionChanged = { selectedTags = it })
                    }
                    item {
                        TagCategorySection(context = context,
                            tags = tags.filter { it.category == TagCategoryState.LIFESTYLE },
                            selectedTags = selectedTags,
                            onTagSelectionChanged = { selectedTags = it })
                    }
                    item {
                        TagCategorySection(context = context,
                            tags = tags.filter { it.category == TagCategoryState.INTERESTS },
                            selectedTags = selectedTags,
                            onTagSelectionChanged = { selectedTags = it })
                    }
                    item {
                        TagCategorySection(context = context,
                            tags = tags.filter { it.category == TagCategoryState.PETS },
                            selectedTags = selectedTags,
                            onTagSelectionChanged = { selectedTags = it })
                    }
                })
            })
    }
}