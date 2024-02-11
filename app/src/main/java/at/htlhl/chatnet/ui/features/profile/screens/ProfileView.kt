package at.htlhl.chatnet.ui.features.profile.screens

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import at.htlhl.chatnet.data.tags
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.dialogs.DeleteAccountDialog
import at.htlhl.chatnet.ui.features.mixed.TabsTopBar
import at.htlhl.chatnet.ui.features.profile.ProfileChangeUsernameElement
import at.htlhl.chatnet.ui.features.profile.ProfileDeleteAccountElement
import at.htlhl.chatnet.ui.features.profile.ProfileEmailElement
import at.htlhl.chatnet.ui.features.profile.ProfileProfilePictureElement
import at.htlhl.chatnet.ui.features.profile.ProfileSwitchAccountElement
import at.htlhl.chatnet.ui.features.profile.ProfileTagInfoElement
import at.htlhl.chatnet.ui.features.profile.ProfileUsernameElement
import at.htlhl.chatnet.ui.features.profile.profileCheckIfUsernameIsValid
import at.htlhl.chatnet.ui.features.profile.viewmodels.ProfileViewModel
import at.htlhl.chatnet.util.convertBitmapToWebP
import at.htlhl.chatnet.util.uploadWebPImage
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileView {

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun ProfileScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val profileViewModel = viewModel<ProfileViewModel>()
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(
            color = MaterialTheme.colorScheme.background,
            darkIcons = !isSystemInDarkTheme()
        )
        val userState = sharedViewModel.user.collectAsState()
        val userData: FirebaseUser = userState.value
        val context = LocalContext.current
        val filteredTags = if (userData.tags.isEmpty()) tags.filter { tag -> tag.category == "Empty" } else tags.filter { tag -> userData.tags.contains(tag.name) }
        var usernameText by remember { mutableStateOf("") }
        var updateProfilePictureLoading by remember { mutableStateOf(false) }
        var updateProfilePictureException by remember { mutableStateOf(false) }
        var deleteAccountDialog by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        var changeUsernameException by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        var modelBottomSheetState by remember { mutableStateOf(false) }
        var usernameTextFieldColor by remember { mutableStateOf(Color.Red) }
        var usernameAlreadyExists by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        val googleSignInOptions = remember {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(R.string.web_client_id.toString())
                .requestEmail()
                .build()
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
                    uploadWebPImage(
                        webpByteArray = webpByteArray,
                        saveLocation = "users/",
                        onUploadSuccess = { downloadUrl ->
                            profileViewModel.updateUserProfilePicture(downloadUrl) {success ->
                                updateProfilePictureLoading = false
                                updateProfilePictureException = !success
                            }
                        },
                        onUploadError = {
                            updateProfilePictureLoading = false
                            updateProfilePictureException = true
                        }
                    )
                }
            )
        Scaffold(
            backgroundColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            topBar = {
                TabsTopBar(
                    tab = CurrentTab.PROFILE,
                    availableUsers = listOf(FirebaseUser()),
                    sharedViewModel = sharedViewModel
                )
            },
            content = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ProfileProfilePictureElement(
                                userData = userData,
                                updateProfilePictureLoading = updateProfilePictureLoading,
                                updateProfilePictureException = updateProfilePictureException,
                                onImageClicked = {
                                    navController.navigate(Screens.ProfilePictureView.route)
                                },
                                onProfileChangeClicked = {
                                    updateProfilePictureException = false
                                    updateProfilePictureLoading = true
                                    multiplePhotoPickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                            )
                            ProfileUsernameElement(userData = userData) {
                                modelBottomSheetState = true
                                coroutineScope.launch {
                                    delay(500)
                                    focusRequester.requestFocus()
                                }
                            }

                            ProfileTagInfoElement(filteredTags = filteredTags) {
                                navController.navigate(Screens.TagSelectScreen.route)
                            }

                            ProfileEmailElement(userData = userData) {
                                sharedViewModel.copyToClipboard(context, userData.email)
                            }
                            ProfileSwitchAccountElement {
                                profileViewModel.logout(context, googleSignInOptions)
                                sharedViewModel.reset()
                                navController.navigate(Screens.LoginFlow.route) {
                                    popUpTo(Screens.MainFlow.route) {
                                        inclusive = true
                                    }
                                }
                            }
                            ProfileDeleteAccountElement {
                                deleteAccountDialog = true
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                if (deleteAccountDialog) {
                    DeleteAccountDialog {
                        if (it == "deleted") {
                            //TODO: Implement Delete Account
                        } else {
                            deleteAccountDialog = false
                        }
                    }
                }
                if (modelBottomSheetState) {
                    ProfileChangeUsernameElement(
                        userData = userData,
                        usernameText = usernameText,
                        usernameTextFieldColor = usernameTextFieldColor,
                        isLoading = isLoading,
                        focusRequester = focusRequester,
                        usernameAlreadyExists = usernameAlreadyExists,
                        changeUsernameException = changeUsernameException,
                        onDismissModelBottomSheet = {
                            modelBottomSheetState = false
                        },
                        onValueChange = {
                            usernameText = it
                          profileViewModel.checkIfUsernameExists(usernameText) { exists ->
                                usernameAlreadyExists = exists
                                usernameTextFieldColor =
                                    if (exists || !profileCheckIfUsernameIsValid(usernameText)) {
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
                        onSavePressed = {
                            isLoading = true
                           profileViewModel.checkIfUsernameExists(usernameText) { exists ->
                                if (!exists && profileCheckIfUsernameIsValid(usernameText)) {
                                    sharedViewModel.updateUsername(
                                        usernameText,
                                    ) {
                                        isLoading = false
                                        if (it) {
                                            usernameText = ""
                                            usernameTextFieldColor = Color.Red
                                            modelBottomSheetState = false
                                        } else {
                                            changeUsernameException = true
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    changeUsernameException = true
                                }
                            }
                        }
                    )
                }
            },
        )
    }
}