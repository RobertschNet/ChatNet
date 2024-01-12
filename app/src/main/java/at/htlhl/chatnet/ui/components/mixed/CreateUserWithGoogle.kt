package at.htlhl.chatnet.ui.components.mixed

import android.content.Context
import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

@Preview
@Composable
fun CreateUserWithGoogle(
    onClose: (String) -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current
    var usernameTexFieldColor by remember { mutableStateOf(Color.Gray) }
    Dialog(
        onDismissRequest = { onClose.invoke("") },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(20.dp))
                .width(250.dp)
                .height(360.dp),
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
                    cursorColor = usernameTexFieldColor,
                    focusedBorderColor = usernameTexFieldColor,
                    unfocusedBorderColor = usernameTexFieldColor,
                    focusedLabelColor = usernameTexFieldColor,
                    unfocusedLabelColor = usernameTexFieldColor,
                ),
                value = email,
                onValueChange = { newValue ->
                    email = newValue
                    if (email.isEmpty()) {
                        usernameTexFieldColor = Color.Gray
                    } else {
                        checkIfUsernameExists(
                            name = email,
                            contextForToast = context
                        ) { success ->
                            usernameTexFieldColor = if (success) {
                                Color.Red
                            } else {
                                Color.Green
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, bottom = 20.dp),
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
                Button(
                    onClick = {
                        checkIfUsernameExists(email, context) {
                            if (it) {
                                usernameTexFieldColor = Color.Red
                                Toast.makeText(
                                    context,
                                    "Username already exists",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onClose.invoke(email)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text(
                        text = "Create Account",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF00A0E8)
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
    contextForToast: Context,
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
                    Toast.makeText(
                        contextForToast,
                        "Username already exists",
                        Toast.LENGTH_SHORT
                    ).show()
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