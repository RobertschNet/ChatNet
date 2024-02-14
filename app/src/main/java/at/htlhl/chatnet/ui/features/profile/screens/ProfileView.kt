package at.htlhl.chatnet.ui.features.profile.screens

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.CurrentTab
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.TextFieldTypeState
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.dialogs.DeleteAccountDialog
import at.htlhl.chatnet.ui.features.mixed.TabsTopBar
import at.htlhl.chatnet.ui.features.profile.components.ProfileChangeUsernameComponent
import at.htlhl.chatnet.ui.features.profile.components.ProfileDeleteAccountComponent
import at.htlhl.chatnet.ui.features.profile.components.ProfileProfilePictureComponent
import at.htlhl.chatnet.ui.features.profile.components.ProfileSwitchAccountComponent
import at.htlhl.chatnet.ui.features.profile.components.ProfileUserEmailComponent
import at.htlhl.chatnet.ui.features.profile.components.ProfileUserTagListComponent
import at.htlhl.chatnet.ui.features.profile.components.ProfileUsernameComponent
import at.htlhl.chatnet.ui.features.profile.viewmodels.ProfileViewModel
import at.htlhl.chatnet.util.checkIfValueIsValid
import at.htlhl.chatnet.util.convertBitmapToWebP
import at.htlhl.chatnet.util.copyToClipboard
import at.htlhl.chatnet.util.getPersonTagsList
import at.htlhl.chatnet.util.uploadWebPImage
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileView {

    @Composable
    fun ProfileScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val profileViewModel = viewModel<ProfileViewModel>()
        val dropInState by sharedViewModel.dropInState
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(
            color = MaterialTheme.colorScheme.background, darkIcons = !isSystemInDarkTheme()
        )
        val userDataState by sharedViewModel.user.collectAsState()
        val userData: FirebaseUser = userDataState
        val context = LocalContext.current
        val filteredTags = getPersonTagsList(personData = userData)
        var usernameText by remember { mutableStateOf("") }
        var updateProfilePictureLoading by remember { mutableStateOf(false) }
        var updateProfilePictureException by remember { mutableStateOf(false) }
        var deleteAccountDialog by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        var changeUsernameException by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        var modelSheetState by remember { mutableStateOf(false) }
        var usernameTextFieldColor by remember { mutableStateOf(Color.Red) }
        var usernameAlreadyExists by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        val googleSignInOptions = remember {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(R.string.web_client_id.toString()).requestEmail().build()
        }

        val multiplePhotoPickerLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(),
                onResult = { uri ->
                    if (uri == null) {
                        updateProfilePictureLoading = false
                        return@rememberLauncherForActivityResult
                    }
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val webpByteArray = convertBitmapToWebP(bitmap)
                    uploadWebPImage(webpByteArray = webpByteArray,
                        saveLocation = "users/",
                        onUploadSuccess = { downloadUrl ->
                            profileViewModel.updateUserProfilePicture(downloadUrl) { success ->
                                updateProfilePictureLoading = false
                                updateProfilePictureException = !success
                            }
                        },
                        onUploadError = {
                            updateProfilePictureLoading = false
                            updateProfilePictureException = true
                        })
                })
        Scaffold(
            backgroundColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            topBar = {
                TabsTopBar(
                    tab = CurrentTab.PROFILE,
                    dropInState = dropInState,
                    onUpdateSearchValue = {
                        sharedViewModel.searchValue.value = it
                    })
            },
            content = { paddingValues ->
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .fillMaxSize()
                ) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        ProfileProfilePictureComponent(userData = userData,
                            updateProfilePictureLoading = updateProfilePictureLoading,
                            updateProfilePictureException = updateProfilePictureException,
                            onProfilePictureClicked = {
                                sharedViewModel.updatePublicUser(newFriend = userData,
                                    onComplete = {
                                        navController.navigate(Screens.ProfilePictureView.route)
                                    })
                            },
                            onChangeProfilePictureClicked = {
                                updateProfilePictureException = false
                                updateProfilePictureLoading = true
                                multiplePhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            })
                        Spacer(modifier = Modifier.height(10.dp))
                        ProfileUsernameComponent(userData = userData, onUsernameClicked = {
                            modelSheetState = true
                            coroutineScope.launch {
                                delay(500)
                                focusRequester.requestFocus()
                            }
                        })
                        Spacer(modifier = Modifier.height(10.dp))
                        ProfileUserTagListComponent(
                            filteredTags = filteredTags,
                            onTagListClicked = {
                                navController.navigate(Screens.TagSelectScreen.route)
                            })
                        Spacer(modifier = Modifier.height(10.dp))
                        ProfileUserEmailComponent(userData = userData, onEmailClicked = {
                            copyToClipboard(
                                context = context, label = "Email", text = userData.email
                            )
                        })
                        Spacer(modifier = Modifier.height(10.dp))
                        ProfileSwitchAccountComponent(onSwitchAccount = {
                            profileViewModel.logout(context, googleSignInOptions)
                            sharedViewModel.reset()
                            navController.navigate(Screens.LoginFlow.route) {
                                popUpTo(Screens.MainFlow.route) {
                                    inclusive = true
                                }
                            }
                        })
                        Spacer(modifier = Modifier.height(10.dp))
                        ProfileDeleteAccountComponent(onDeleteAccountClicked = {
                            deleteAccountDialog = true
                        })
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                if (deleteAccountDialog) {
                    DeleteAccountDialog { deleteClicked ->
                        if (deleteClicked) {
                            profileViewModel.deleteUserAccount(onSuccess = {
                                navController.navigate(Screens.LoginFlow.route) {
                                    popUpTo(Screens.MainFlow.route) {
                                        inclusive = true
                                    }
                                }
                            }, onFailure = {
                                Toast.makeText(
                                    context, "Error deleting account", Toast.LENGTH_SHORT
                                ).show()
                            })
                        }
                        deleteAccountDialog = false
                    }
                }
                if (modelSheetState) {
                    ProfileChangeUsernameComponent(userData = userData,
                        usernameText = usernameText,
                        usernameTextFieldColor = usernameTextFieldColor,
                        isLoading = isLoading,
                        focusRequester = focusRequester,
                        usernameAlreadyExists = usernameAlreadyExists,
                        changeUsernameException = changeUsernameException,
                        usernameIsValid = checkIfValueIsValid(
                            type = TextFieldTypeState.USERNAME, value = usernameText
                        ),
                        onDismissModelSheet = {
                            modelSheetState = false
                        },
                        onUsernameValueChange = { changedText ->
                            usernameText = changedText
                            profileViewModel.checkIfUsernameExists(usernameText) { exists ->
                                usernameAlreadyExists = exists
                                usernameTextFieldColor = if (exists || !checkIfValueIsValid(
                                        type = TextFieldTypeState.USERNAME, value = usernameText
                                    )
                                ) {
                                    Color.Red
                                } else {
                                    Color(0xFF00A0E8)
                                }
                                if (usernameText.isEmpty()) {
                                    usernameTextFieldColor = Color.Red
                                }
                            }
                            if (usernameText.isEmpty()) {
                                usernameTextFieldColor = Color.Red
                            }
                            changeUsernameException = false
                        },
                        onSaveUsernamePressed = {
                            isLoading = true
                            profileViewModel.checkIfUsernameExists(usernameText) { exists ->
                                if (!exists && checkIfValueIsValid(
                                        type = TextFieldTypeState.USERNAME, value = usernameText
                                    )
                                ) {
                                    profileViewModel.updateProfileUsername(
                                        userName = usernameText,
                                        callback = { success ->
                                            isLoading = false
                                            if (success) {
                                                usernameText = ""
                                                usernameTextFieldColor = Color.Red
                                                modelSheetState = false
                                            } else {
                                                changeUsernameException = true
                                            }
                                        })
                                } else {
                                    isLoading = false
                                    changeUsernameException = true
                                }
                            }
                        })
                }
            },
        )
    }
}