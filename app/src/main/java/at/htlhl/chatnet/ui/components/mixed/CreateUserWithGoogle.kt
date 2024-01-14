package at.htlhl.chatnet.ui.components.mixed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import at.htlhl.chatnet.data.AccountDataState
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

@Preview
@Composable
fun CreateUserWithGoogle(
    onClose: (String) -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var usernameTexFieldColor by remember { mutableStateOf(AccountDataState.Empty) }
    var usernameExists by remember { mutableStateOf(false) }
    val usernameColor =
        if (usernameTexFieldColor == AccountDataState.Empty) Color.Gray else if (usernameTexFieldColor == AccountDataState.Valid) MaterialTheme.colorScheme.primary else Color.Red
    Dialog(
        onDismissRequest = { onClose.invoke("") },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(20.dp))
                .width(250.dp)
                .height(300.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Account with Google",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = "In order to create an account with Google you have to create a username first.",
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    top = 10.dp,
                    bottom = 20.dp,
                    start = 10.dp,
                    end = 10.dp
                )
            )
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = usernameColor,
                    focusedBorderColor = usernameColor,
                    unfocusedBorderColor = usernameColor,
                    focusedLabelColor = usernameColor,
                    unfocusedLabelColor = usernameColor,
                ),
                value = username,
                supportingText = {
                    if (username.isNotEmpty()) {
                        Column {
                            if (usernameExists) {
                                Text(
                                    text = "Username already exists",
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Light,
                                    fontFamily = FontFamily.SansSerif,
                                )
                            } else if (!checkIfValueIsValid(username)) {
                                Text(
                                    text = "Username is invalid",
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Light,
                                    fontFamily = FontFamily.SansSerif,
                                )
                            }
                        }
                    }
                },
                onValueChange = { newValue ->
                    username = newValue
                    checkIfUsernameExists(
                        name = username,
                    ) { success ->
                        usernameExists = success
                        usernameTexFieldColor =
                            if (success || !checkIfValueIsValid(username)) {
                                AccountDataState.Invalid
                            } else {
                                AccountDataState.Valid
                            }
                        if (username.isEmpty()) {
                            usernameTexFieldColor = AccountDataState.Empty
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                label = { Text(text = "Username") },
            )
            Divider(
                thickness = 0.3f.dp,
                color = Color.LightGray,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClose.invoke(username) }
                ) {
                    Text(
                        text = "Create Account",
                        color = Color(0xFF00A0E8),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                    )
                }
            }
            Divider(
                thickness = 0.3f.dp,
                color = Color.LightGray,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClose.invoke("") }
            ) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                )
            }
        }
    }
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

private fun checkIfValueIsValid(value: String): Boolean {
    return value.matches("^(?!.*[._-]{2})(?![._-])[a-zA-Z0-9._-]{1,30}(?<![._-])$".toRegex())
}
