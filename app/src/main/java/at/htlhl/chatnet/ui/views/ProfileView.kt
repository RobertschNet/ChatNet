package at.htlhl.chatnet.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.htlhl.chatnet.R
import at.htlhl.chatnet.data.FirebaseUsers
import at.htlhl.chatnet.viewmodels.SharedViewModel
import at.htlhl.chatnet.navigation.Screens
import coil.compose.SubcomposeAsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

class ProfileView {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun ProfileScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val userState = sharedViewModel.user.collectAsState()
        val userData: FirebaseUsers = userState.value
        val context = LocalContext.current
        var otto: String by remember { mutableStateOf(userData.username["mixedcase"].toString()) }
        val scope = rememberCoroutineScope()
        val coroutineScope = rememberCoroutineScope()
        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
        )
        val googleSignInOptions = remember {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(R.string.web_client_id.toString())
                .requestEmail()
                .build()
        }
        val logout = {
            scope.launch {
                val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
                googleSignInClient.signOut()
                    .addOnCompleteListener {}
            }
        }
        BottomSheetScaffold(
            sheetContent = {
                Column(modifier = Modifier.background(Color.White)) {
                    Text(
                        text = "Enter your new username",
                        color = Color.Black,
                        modifier = Modifier.padding(top = 20.dp, start = 20.dp)
                    )
                    TextField(
                        value = otto,
                        onValueChange = { otto = it },
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = Color.Black,
                            backgroundColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFF00A0E8),
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 40.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Cancel",
                            textAlign = TextAlign.End,
                            color = Color(0xFF00A0E8),
                            modifier = Modifier
                                .clickable {
                                    coroutineScope.launch {
                                        sharedViewModel.bottomBarState.value = false
                                    }
                                }
                                .padding(top = 20.dp, end = 40.dp)
                        )
                        Text(
                            text = "Save",
                            textAlign = TextAlign.End,
                            color = Color(0xFF00A0E8),
                            modifier = Modifier
                                .clickable {
                                    coroutineScope.launch {
                                        bottomSheetScaffoldState.bottomSheetState.collapse()
                                    }
                                }
                                .padding(top = 20.dp, end = 20.dp)
                        )
                    }
                }
            },
            scaffoldState = bottomSheetScaffoldState,
            sheetPeekHeight = 0.dp,
            sheetBackgroundColor = Color.Black,
            sheetShape = RoundedCornerShape(0.dp)
        ) {
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
                            .clip(CircleShape),
                        loading = {
                            CircularProgressIndicator()
                        }
                    )
                    Box(
                        modifier = Modifier
                            .size(50f.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF00A0E8), Color(0xFF00A0E8))
                                )
                            )
                            .align(Alignment.BottomEnd)
                    ) {
                        Icon(imageVector = Icons.Outlined.Cached,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    // TODO: Change profile picture
                                }
                                .align(Alignment.Center)
                                .clip(CircleShape)
                        )
                    }

                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 15.dp, top = 5.dp)
                            .size(30.dp)
                    )
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(start = 15.dp)
                    ) {
                        Text(
                            text = "UserName",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .padding(bottom = 3.dp)
                        )
                        Text(
                            text = userData.username["mixedcase"].toString(),
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .padding(bottom = 5.dp)
                                .clickable { coroutineScope.launch { bottomSheetScaffoldState.bottomSheetState.expand() } }

                        )
                        Text(
                            text = "This is your public username. Other users will see this name when they view your profile.",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Light,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(end = 20.dp)
                        )
                    }
                }
                Divider(
                    color = Color.DarkGray,
                    thickness = 0.3.dp,
                    modifier = Modifier
                        .padding(start = 50.dp, end = 50.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 15.dp, top = 5.dp)
                            .size(30.dp)
                    )
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(start = 15.dp)
                    ) {
                        Text(
                            text = "Status",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                        Text(
                            text = userData.status,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                        Text(
                            text = "This is your public status. Other users will see this status when they view your profile.",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Light,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(end = 20.dp)
                        )
                    }

                }
                Divider(
                    color = Color.DarkGray,
                    thickness = 0.3.dp,
                    modifier = Modifier
                        .padding(start = 50.dp, end = 50.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.Mail,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 15.dp, top = 5.dp)
                            .size(30.dp)
                    )
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(start = 15.dp)
                    ) {
                        Text(
                            text = "Email",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                        Text(
                            text = "tobias.brandl2005@gmail.com", // Later userData.email
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                }
                Button(
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black,
                        containerColor = Color.White
                    ),
                    onClick = {
                        sharedViewModel.updateOnlineStatus("offline")
                        logout()
                        sharedViewModel.reset()
                        sharedViewModel.resetMatchedUser()
                        sharedViewModel.auth.signOut()
                        sharedViewModel.gpsState.value = true
                        navController.navigate(Screens.LoginScreen.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(text = "Sign Out", color = Color.Cyan, modifier = Modifier.padding(7.dp))
                }
            }
        }
    }
}