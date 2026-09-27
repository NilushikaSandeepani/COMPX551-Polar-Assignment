package com.example.polar.ui.page

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polar.R
import com.example.polar.data.db.AppDatabase
import com.example.polar.data.entity.User
import com.example.polar.ui.theme.FieldGrey
import com.example.polar.ui.theme.Orange
import com.example.polar.ui.theme.PolarTheme
import com.example.polar.ui.theme.WorkSans
import kotlinx.coroutines.launch

class SignPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolarTheme {
                SignScreen()
            }
        }
    }
}

@Composable
fun SignScreen() {
    val context = LocalContext.current
    val userDao = remember { AppDatabase.getDatabase(context).userDao() }
    // Room functions are suspend functions, so they need a coroutine
    val scope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    // true = sign in, false = sign up
    var isSignIn by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Picture in the top right corner
        Image(
            painter = painterResource(id = R.drawable.chest_press_machine_full_solid_white_background),
            contentDescription = "Gym machine",
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopEnd,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxWidth()
                .height(256.dp)
        )
        // White gradient on top of the picture so it fades into the background
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxWidth()
                .height(256.dp)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.5f to Color.White.copy(alpha = 0.5f),
                        1.0f to Color.White
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(96.dp))

            // Orange logo box
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Orange, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isSignIn) "Sign In To Polar" else "Sign Up To Polar",
                color = Color.Black,
                fontSize = 30.sp,
                fontFamily = WorkSans,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Let's track your heart rate with Polar",
                color = Color.DarkGray,
                fontSize = 16.sp,
                fontFamily = WorkSans
            )

            Spacer(modifier = Modifier.height(40.dp))

            // First and last name are only needed when signing up
            if (!isSignIn) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel("First Name")
                        TextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            placeholder = { Text("First name") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = fieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel("Last Name")
                        TextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            placeholder = { Text("Last name") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = fieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Username
            FieldLabel("Username")
            TextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("Enter your username") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password
            FieldLabel("Password")
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Enter your password") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Text(
                        text = if (showPassword) "Hide" else "Show",
                        color = Color.Gray,
                        modifier = Modifier
                            .clickable { showPassword = !showPassword }
                            .padding(12.dp)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (!isSignIn && (firstName.isBlank() || lastName.isBlank())) {
                        Toast.makeText(context, "Please enter your first and last name", Toast.LENGTH_SHORT).show()
                    } else if (username.isBlank()) {
                        Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
                    } else if (password.length < 6) {
                        Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    } else {
                        scope.launch {
                            val user = userDao.findByUsername(username)
                            if (isSignIn) {
                                if (user != null && user.password == password) {
                                    Toast.makeText(context, "Welcome back, ${user.firstName}!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(context, MainPage::class.java)
                                    intent.putExtra("firstName", user.firstName)
                                    intent.putExtra("lastName", user.lastName)
                                    intent.putExtra("username", user.username)
                                    context.startActivity(intent)
                                    // Close the sign in page so back button doesn't return here
                                    (context as Activity).finish()
                                } else {
                                    Toast.makeText(context, "Wrong username or password", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                if (user != null) {
                                    Toast.makeText(context, "This username is already taken", Toast.LENGTH_SHORT).show()
                                } else {
                                    userDao.insert(
                                        User(
                                            firstName = firstName,
                                            lastName = lastName,
                                            username = username,
                                            password = password
                                        )
                                    )
                                    Toast.makeText(context, "Sign up success, please sign in", Toast.LENGTH_SHORT).show()
                                    isSignIn = true
                                    password = ""
                                }
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF111111),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(text = if (isSignIn) "Sign In  →" else "Sign Up  →", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Switch between sign in and sign up
            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    text = if (isSignIn) "Don't have an account? " else "Already have an account? ",
                    color = Color.Gray
                )
                Text(
                    text = if (isSignIn) "Sign Up." else "Sign In.",
                    color = Orange,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        isSignIn = !isSignIn
                        password = ""
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FieldLabel(text: String) {
    Text(
        text = text,
        color = Color.Black,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

// Grey box with no underline, like the design
@Composable
fun fieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = FieldGrey,
    unfocusedContainerColor = FieldGrey,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    cursorColor = Orange
)
