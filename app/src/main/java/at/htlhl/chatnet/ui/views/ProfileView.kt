package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.graphics.Bitmap
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
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.tags
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.components.dialogs.DeleteAccountDialog
import at.htlhl.chatnet.ui.components.mixed.TabsTopBar
import at.htlhl.chatnet.ui.components.profile.ProfileChangeUsernameElement
import at.htlhl.chatnet.ui.components.profile.ProfileDeleteAccountElement
import at.htlhl.chatnet.ui.components.profile.ProfileEmailElement
import at.htlhl.chatnet.ui.components.profile.ProfileProfilePictureElement
import at.htlhl.chatnet.ui.components.profile.ProfileSwitchAccountElement
import at.htlhl.chatnet.ui.components.profile.ProfileTagInfoElement
import at.htlhl.chatnet.ui.components.profile.ProfileUsernameElement
import at.htlhl.chatnet.ui.components.profile.profileCheckIfUsernameIsValid
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Locale

class ProfileView {

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun ProfileScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(color = MaterialTheme.colorScheme.background, darkIcons = !isSystemInDarkTheme())
        val userState = sharedViewModel.user.collectAsState()
        val userData: FirebaseUser = userState.value
        val context = LocalContext.current
        val filteredTags =
            if (userData.tags.isEmpty()) tags.filter { tag -> tag.category == "Empty" } else tags.filter { tag ->
                userData.tags.contains(tag.name)
            }
        var usernameText by remember {
            mutableStateOf("")
        }
        var updateProfilePictureLoading by remember {
            mutableStateOf(false)
        }
        var updateProfilePictureException by remember {
            mutableStateOf(false)
        }
        var deleteAccountDialog by remember {
            mutableStateOf(false)
        }
        var isLoading by remember {
            mutableStateOf(false)
        }
        var changeUsernameException by remember {
            mutableStateOf(false)
        }
        val coroutineScope = rememberCoroutineScope()
        var modelBottomSheetState by remember {
            mutableStateOf(false)
        }
        var usernameTextFieldColor by remember {
            mutableStateOf(Color.Red)
        }
        var usernameAlreadyExists by remember {
            mutableStateOf(false)
        }
        val focusRequester = remember { FocusRequester() }
        val googleSignInOptions = remember {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(R.string.web_client_id.toString())
                .requestEmail()
                .build()
        }
        val logout = {
            coroutineScope.launch {
                val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
                googleSignInClient.signOut()
                    .addOnCompleteListener {}
            }
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
                        webpByteArray,
                        { downloadUrl ->
                            sharedViewModel.updateUserProfilePicture(downloadUrl) {
                                updateProfilePictureLoading = false
                                updateProfilePictureException = !it
                            }
                        },
                        {
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
                    tab = "Profile",
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
                                sharedViewModel.updateOnlineStatus(false)
                                logout()
                                sharedViewModel.reset()
                                sharedViewModel.auth.signOut()
                                sharedViewModel.gpsState.value = true
                                navController.navigate("LoginFlow") {
                                    popUpTo("MainFlow") {
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
                            checkIfUsernameExists(usernameText) { exists ->
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
                            checkIfUsernameExists(usernameText) { exists ->
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


    private fun checkIfUsernameExists(
        name: String,
        callback: (Boolean) -> Unit
    ) {
        val query = FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("username.lowercase", name.lowercase(Locale.ROOT))
            .limit(1)
        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    val usernameField = documentSnapshot.get("username.lowercase")
                    if (usernameField is String) {
                        println("Retrieved value: $usernameField")
                        callback(true)
                    } else {
                        println("Field 'username.value' is not a String")
                        callback(false)
                    }
                } else {
                    println("Document not found")
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                println("Error retrieving document: ${exception.message}")
                callback(false)
            }
    }

    private fun uploadWebPImage(
        webpByteArray: ByteArray,
        onUploadSuccess: (String) -> Unit,
        onUploadError: () -> Unit
    ) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val webpImageRef = storageRef.child("users/${Timestamp.now().seconds}.webp")

        webpImageRef.putBytes(webpByteArray)
            .addOnSuccessListener {
                webpImageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    onUploadSuccess.invoke(downloadUrl.toString())
                }.addOnFailureListener {
                    onUploadError.invoke()
                }
            }
            .addOnFailureListener {
                onUploadError.invoke()
            }
    }

    @Suppress("DEPRECATION")
    private fun convertBitmapToWebP(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.WEBP, 50, outputStream)
        return outputStream.toByteArray()
    }

}