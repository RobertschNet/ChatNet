package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.ui.theme.shimmerEffect
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Locale

class ProfileView {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ProfileScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val userState = sharedViewModel.user.collectAsState()
        val userData: FirebaseUser = userState.value
        val context = LocalContext.current
        var usernameText by remember {
            mutableStateOf("")
        }
        var updateProfilePictureLoading by remember {
            mutableStateOf(false)
        }
        var updateProfilePictureException by remember {
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
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            content = {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 30.dp)
                                    .size(180.dp)
                            ) {
                                SubcomposeAsyncImage(
                                    model = userData.image,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .shimmerEffect()
                                )
                                Box(
                                    modifier = Modifier
                                        .size(50f.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF00A0E8),
                                                    Color(0xFF00A0E8)
                                                )
                                            )
                                        )
                                        .align(Alignment.BottomEnd)
                                ) {
                                    var rotationState by remember { mutableFloatStateOf(0f) }

                                    val rotation by animateFloatAsState(
                                        targetValue = rotationState,
                                        animationSpec = tween(
                                            durationMillis = 1000,
                                            easing = FastOutSlowInEasing
                                        ), label = ""
                                    )
                                    LaunchedEffect(updateProfilePictureLoading) {
                                        while (true) {
                                            if (updateProfilePictureLoading) {
                                                rotationState += -360f
                                                delay(1000)
                                            } else {
                                                break
                                            }
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Outlined.Cached,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clickable(enabled = !updateProfilePictureLoading) {
                                                updateProfilePictureException = false
                                                updateProfilePictureLoading = true
                                                multiplePhotoPickerLauncher.launch(
                                                    PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            }
                                            .align(Alignment.Center)
                                            .clip(CircleShape)
                                            .graphicsLayer(
                                                rotationZ = rotation,
                                            )
                                    )


                                }
                            }

                            if (updateProfilePictureException) {
                                Text(
                                    text = "An error occurred while updating your profile picture.",
                                    color = Color.Red,
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif,
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .padding(start = 20.dp, top = 20.dp)
                                    .clickable {
                                        modelBottomSheetState = true
                                        coroutineScope.launch {
                                            delay(500)
                                            focusRequester.requestFocus()
                                        }
                                    }
                                    .fillMaxWidth(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    tint = if (isSystemInDarkTheme()) Color.White else Color.Gray,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.Top)
                                        .padding(top = 5.dp)
                                        .size(30.dp)
                                )
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier.padding(start = 15.dp)
                                ) {
                                    Text(
                                        text = "UserName",
                                        color = Color.DarkGray,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Light,
                                        modifier = Modifier
                                            .padding(bottom = 3.dp)
                                    )
                                    Text(
                                        text = userData.username["mixedcase"].toString(),
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color.Black,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.SansSerif,
                                        modifier = Modifier
                                            .padding(bottom = 5.dp)
                                    )
                                    Text(
                                        text = "This is your public username. Other users will see this name when they view your profile.",
                                        color = Color.DarkGray,
                                        overflow = TextOverflow.Clip,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Default,
                                        fontWeight = FontWeight.Light,
                                        lineHeight = 16.sp,
                                        modifier = Modifier
                                            .padding(bottom = 10.dp, end = 20.dp)
                                    )

                                    Divider(
                                        color = Color.DarkGray,
                                        thickness = 0.3.dp,
                                        modifier = Modifier
                                            .padding(end = 20.dp)
                                            .align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .padding(start = 20.dp, top = 20.dp)
                                    .clickable {
                                        //TODO: Open TagView
                                    }
                                    .fillMaxWidth(),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    tint = if (isSystemInDarkTheme()) Color.White else Color.Gray,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.Top)
                                        .padding(top = 5.dp)
                                        .size(30.dp)
                                )
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier.padding(start = 15.dp)
                                ) {
                                    Text(
                                        text = "Status",
                                        color = Color.DarkGray,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Light,
                                        modifier = Modifier
                                            .padding(bottom = 3.dp)
                                    )
                                    Spacer(modifier = Modifier.height(40.dp)) //TODO: ADD TAGS
                                    Text(
                                        text = "This are your tags. Other users will see this tags when they view your profile.",
                                        color = Color.DarkGray,
                                        overflow = TextOverflow.Clip,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Default,
                                        fontWeight = FontWeight.Light,
                                        lineHeight = 16.sp,
                                        modifier = Modifier
                                            .padding(bottom = 10.dp, end = 20.dp)
                                    )

                                    Divider(
                                        color = Color.DarkGray,
                                        thickness = 0.3.dp,
                                        modifier = Modifier
                                            .padding(end = 20.dp)
                                            .align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .padding(start = 20.dp, top = 20.dp)
                                    .clickable {
                                        sharedViewModel.copyToClipboard(context, userData.email)
                                    }
                                    .fillMaxWidth(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    tint = if (isSystemInDarkTheme()) Color.White else Color.Gray,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.Top)
                                        .padding(top = 5.dp)
                                        .size(30.dp)
                                )
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier.padding(start = 15.dp)
                                ) {
                                    Text(
                                        text = "Email",
                                        color = Color.DarkGray,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Light,
                                        modifier = Modifier
                                            .padding(bottom = 3.dp)
                                    )
                                    Text(
                                        text = userData.email,
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = FontFamily.SansSerif,
                                        modifier = Modifier
                                            .padding(bottom = 5.dp)
                                    )
                                    Text(
                                        text = "This is your email. Other users will not see this email when they view your profile.",
                                        color = Color.DarkGray,
                                        overflow = TextOverflow.Clip,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Default,
                                        fontWeight = FontWeight.Light,
                                        lineHeight = 16.sp,
                                        modifier = Modifier
                                            .padding(bottom = 10.dp, end = 20.dp)
                                    )

                                    Divider(
                                        color = Color.DarkGray,
                                        thickness = 0.3.dp,
                                        modifier = Modifier
                                            .padding(end = 20.dp)
                                            .align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .padding(start = 20.dp, top = 20.dp)
                                    .clickable {
                                        sharedViewModel.updateOnlineStatus("offline")
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
                                    .fillMaxWidth(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwitchAccount,
                                    tint = if (isSystemInDarkTheme()) Color.White else Color.Gray,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.Top)
                                        .padding(top = 5.dp)
                                        .size(30.dp)
                                )
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier.padding(start = 15.dp)
                                ) {
                                    Text(
                                        text = "Switch Account",
                                        color = Color.DarkGray,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Light,
                                        modifier = Modifier
                                            .padding(bottom = 3.dp)
                                    )
                                    Text(
                                        text = "Signs you out of this account and let's you sign in with another.",
                                        color = Color.DarkGray,
                                        overflow = TextOverflow.Clip,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Default,
                                        fontWeight = FontWeight.Light,
                                        lineHeight = 16.sp,
                                        modifier = Modifier
                                            .padding(bottom = 10.dp, end = 20.dp)
                                    )

                                    Divider(
                                        color = Color.DarkGray,
                                        thickness = 0.3.dp,
                                        modifier = Modifier
                                            .padding(end = 20.dp)
                                            .align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .padding(start = 20.dp, top = 20.dp)
                                    .clickable {
                                        //TODO: Open Delete Account Dialog
                                    }
                                    .fillMaxWidth(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    tint = if (isSystemInDarkTheme()) Color.White else Color.Gray,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.Top)
                                        .padding(top = 5.dp)
                                        .size(30.dp)
                                )
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier.padding(start = 15.dp)
                                ) {
                                    Text(
                                        text = "Delete Account",
                                        color = Color.DarkGray,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Light,
                                        modifier = Modifier
                                            .padding(bottom = 3.dp)
                                    )
                                    Text(
                                        text = "Delete your account and all your data. This action is irreversible!",
                                        color = Color.DarkGray,
                                        overflow = TextOverflow.Clip,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Default,
                                        fontWeight = FontWeight.Light,
                                        lineHeight = 16.sp,
                                        modifier = Modifier
                                            .padding(bottom = 10.dp, end = 20.dp)
                                    )

                                    Divider(
                                        color = Color.DarkGray,
                                        thickness = 0.3.dp,
                                        modifier = Modifier
                                            .padding(end = 20.dp)
                                            .align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                if (modelBottomSheetState) {
                    ModalBottomSheet(
                        dragHandle = {},
                        shape = RectangleShape,
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        onDismissRequest = { modelBottomSheetState = false }) {
                        Column {
                            Text(
                                text = "Enter your new username",
                                color = Color.Black,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(top = 20.dp, start = 20.dp)
                            )
                            TextField(
                                value = usernameText,
                                singleLine = true,
                                trailingIcon = {
                                    Text(
                                        text = usernameText.length.toString(),
                                        color = Color.Gray,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Light,
                                        textAlign = TextAlign.Start,
                                        fontFamily = FontFamily.SansSerif,
                                    )
                                },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                                onValueChange = {
                                    usernameText = it
                                    checkIfUsernameExists(usernameText) { exists ->
                                        usernameAlreadyExists = exists
                                        usernameTextFieldColor =
                                            if (exists || !checkIfValueIsValid(usernameText)) {
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
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color.Black,
                                    lineHeight = 18.sp,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Light,
                                    fontFamily = FontFamily.SansSerif
                                ),
                                placeholder = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Text(
                                            text = userData.username["mixedcase"].toString(),
                                            color = Color.Gray,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Light,
                                            textAlign = TextAlign.Start,
                                            fontFamily = FontFamily.SansSerif,
                                        )
                                    }

                                },
                                colors = TextFieldDefaults.textFieldColors(
                                    textColor = Color.Black,
                                    backgroundColor = Color.Transparent,
                                    placeholderColor = Color.Black,
                                    focusedIndicatorColor = usernameTextFieldColor,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester = focusRequester)
                                    .padding(start = 20.dp, end = 40.dp)
                            )
                            if (!checkIfValueIsValid(usernameText)) {
                                Text(
                                    text = "Username is invalid.",
                                    color = Color.Red,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(
                                        start = 20.dp,
                                        end = 40.dp,
                                        top = 5.dp
                                    )
                                )
                            }
                            if (usernameAlreadyExists) {
                                Text(
                                    text = "Username already exists.",
                                    color = Color.Red,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(
                                        start = 20.dp,
                                        end = 40.dp,
                                        top = 5.dp
                                    )
                                )
                            }
                            if (changeUsernameException) {
                                Text(
                                    text = "An error occurred while changing your username.",
                                    color = Color.Red,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(
                                        start = 20.dp,
                                        end = 40.dp,
                                        top = 5.dp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "Cancel",
                                    textAlign = TextAlign.End,
                                    fontFamily = FontFamily.SansSerif,
                                    color = Color(0xFF00A0E8),
                                    modifier = Modifier
                                        .clickable {
                                            coroutineScope.launch {
                                                modelBottomSheetState = false
                                            }
                                        }
                                        .padding(end = 40.dp)
                                )
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .padding(end = 20.dp)
                                            .size(25.dp),
                                        color = Color(0xFF00A0E8)
                                    )
                                } else {
                                    Text(
                                        text = "Save",
                                        fontFamily = FontFamily.SansSerif,
                                        textAlign = TextAlign.End,
                                        color = if (usernameTextFieldColor != Color.Red) Color(
                                            0xFF00A0E8
                                        ) else Color.Gray,
                                        modifier = Modifier
                                            .clickable(enabled = !isLoading && usernameTextFieldColor != Color.Red) {
                                                isLoading = true
                                                checkIfUsernameExists(usernameText) { exists ->
                                                    if (!exists && checkIfValueIsValid(usernameText)) {
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
                                            .padding(end = 20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
        )
    }

    private fun checkIfValueIsValid(value: String): Boolean {
        return value.matches("^(?!.*[._-]{2})(?![._-])[a-zA-Z0-9._-]{1,30}(?<![._-])$".toRegex())
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
        val webpImageRef = storageRef.child("images/${webpByteArray.size}\"")

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

    private fun convertBitmapToWebP(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.WEBP, 50, outputStream)
        return outputStream.toByteArray()
    }

}