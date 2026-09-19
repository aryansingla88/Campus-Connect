package com.example.campusconnect.feature.auth.ui


import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.R
import com.example.campusconnect.feature.auth.viewmodel.RegisterViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

/*
below structure is called a modifier chain-
Modifier
.background()
.padding()
.clickable()*/

/*
STATEFLOW

Use for:

username
password
loading
current screen
verified state

SHAREDFLOW
Use for:

popups
snackbars
toasts
navigation
one-time messages */

// Shared field styling so every text field on this screen matches the login card
private val registerFieldShape = RoundedCornerShape(16.dp)

@Composable
private fun registerFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFFF5F6FA),
    unfocusedContainerColor = Color(0xFFF5F6FA),
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    disabledContainerColor = Color(0xFFF5F6FA),
    disabledBorderColor = Color.Transparent,
    disabledTextColor = Color.Black
)

@Composable
fun RegisterScreen(

    onRegisterSuccess: () -> Unit,

    onNavigateToLogin: () -> Unit,
    //This composable needs a RegisterViewModel type object and if nobody passes one then, create one manually from android's ViewModel system
    viewModel: RegisterViewModel = viewModel()
) {
    val context =
        LocalContext.current

    val username by viewModel.username.collectAsState()

    val rollNumber by viewModel.rollNumber.collectAsState()
    val emailVerified by
    viewModel.emailVerified
        .collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()

    val realName by viewModel.realName.collectAsState()

    val courses by viewModel.courses.collectAsState()

    val selectedCourse by
    viewModel.selectedCourse.collectAsState()

    val admissionYear by viewModel.admissionYear.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val dob by viewModel.dob.collectAsState()


    val registerSuccess by viewModel.registerSuccess.collectAsState()
    var passwordVisible by remember {
        mutableStateOf(false)
    }

    var confirmPasswordVisible by remember {
        mutableStateOf(false)
    }
    val shakeOffset = remember {

        Animatable(0f)
    }

    val scope = rememberCoroutineScope()
    var showBanner by remember {

        mutableStateOf(false)
    }

    var bannerMessage by remember {

        mutableStateOf("")
    }


    LaunchedEffect(registerSuccess) {

        if (registerSuccess) {
            onRegisterSuccess()
        }
    }
    LaunchedEffect(Unit) {

        viewModel.messageEvent.collect {

            bannerMessage = it

            showBanner = true
        }
    }

    Box(
        modifier = Modifier
    ) {

        // ───────── BACKGROUND IMAGE ─────────
        Image(
            painter = painterResource(R.drawable.login_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )


        FloatingMessageBanner(

            visible = showBanner,

            message = bannerMessage,

            onDismiss = {

                showBanner = false
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            // ---- White rounded card, matching the login screen ----
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Register",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Create your Campus Connect account",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    //RegisterTextField is a user defined function defined below
                    RegisterTextField(
                        value = username,
                        placeholder = "Username",
                        //onUsernameChange function has been defined inside the viewmodel class
                        onValueChange = viewModel::onUsernameChange
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    RollNumberField(
                        value = rollNumber,
                        onValueChange = viewModel::onRollNumberChange
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    if (!emailVerified) {

                        Text(

                            text = "Verify Email",

                            color = Color(0xFFFF7A3D),

                            fontSize = 12.sp,

                            modifier = Modifier
                                .graphicsLayer {

                                    translationX = shakeOffset.value
                                }
                                .clickable {
                                    Log.d("GOOGLE_FLOW", "Verify Email clicked")
                                    if (rollNumber.isBlank()) {

                                        scope.launch {

                                            repeat(4) {

                                                shakeOffset.animateTo(
                                                    20f,
                                                    tween(40)
                                                )

                                                shakeOffset.animateTo(
                                                    -20f,
                                                    tween(40)
                                                )
                                            }

                                            shakeOffset.animateTo(
                                                0f,
                                                tween(40)
                                            )
                                        }

                                        return@clickable
                                    }
                                    Log.d("GOOGLE_FLOW", "2. Roll number valid, calling ViewModel")
                                    viewModel.verifyGoogleEmail(context)
                                }
                                .padding(top = 6.dp)
                        )

                    }
                    if (emailVerified) {

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(

                            text = "✓ Email Verified",

                            color = Color(0xFF2E7D32)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    //RegisterPasswordField is a user defined function defined below
                    RegisterPasswordField(
                        value = password,
                        placeholder = "Password",
                        passwordVisible = passwordVisible,

                        onVisibilityChange = {
                            passwordVisible = !passwordVisible
                        },

                        onValueChange = viewModel::onPasswordChange
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    RegisterPasswordField(
                        value = confirmPassword,
                        placeholder = "Confirm Password",
                        passwordVisible = confirmPasswordVisible,

                        onVisibilityChange = {
                            confirmPasswordVisible =
                                !confirmPasswordVisible
                        },

                        onValueChange = viewModel::onConfirmPasswordChange
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    RegisterTextField(
                        value = realName,
                        placeholder = "Real Name",
                        onValueChange = viewModel::onRealNameChange
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DropdownField(
                        value =
                            selectedCourse?.programName
                                ?: selectedCourse?.degree
                                ?: "",

                        placeholder = "Select Course",

                        options =
                            courses.map { course ->
                                course.programName
                                    ?: course.degree
                            },

                        onSelected = { selectedName ->

                            courses
                                .firstOrNull { course ->
                                    (course.programName
                                        ?: course.degree) == selectedName
                                }
                                ?.let { course ->
                                    viewModel.onCourseChange(course)
                                }
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DropdownField(
                        value = admissionYear,
                        placeholder = "Select Admission Year",
                        options = listOf(
                            "2026",
                            "2025",
                            "2024",
                            "2023"
                        ),
                        onSelected = viewModel::onAdmissionYearChange
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    DropdownField(
                        value = gender,
                        placeholder = "Select Gender",

                        options = listOf(
                            "Male",
                            "Female",
                            "Others"
                        ),

                        onSelected = viewModel::onGenderChange
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DobField(
                        value = dob,
                        onDateSelected = viewModel::onDobChange
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.register()
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),

                        shape = RoundedCornerShape(26.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B35)
                        )
                    ) {

                        Text(
                            text = "Register",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        Text(
                            text = "Already have an account? ",
                            color = Color(0xFF444444),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Login",
                            color = Color(0xFFFF6B35),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            /*
                            we can make any text clickable by using modifier
                             */
                            modifier = Modifier.clickable {
                                onNavigateToLogin()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


@Composable
fun RegisterTextField(
    // parameterName : parameterType
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {

    OutlinedTextField(
        value = value,

        onValueChange = onValueChange,
        //a placeholder defines the text that acts as suggest text.
        placeholder = {
            Text(
                text = placeholder,
                color = Color.Gray
            )
        },

        singleLine = true,

        shape = registerFieldShape,

        colors = registerFieldColors(),

        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    )
}

@Composable
fun RegisterPasswordField(

    value: String,

    placeholder: String,

    passwordVisible: Boolean,

    onVisibilityChange: () -> Unit,

    onValueChange: (String) -> Unit
) {

    OutlinedTextField(
        value = value,

        onValueChange = onValueChange,

        placeholder = {
            Text(
                text = placeholder,
                color = Color.Gray
            )
        },

        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color.Gray
            )
        },

        singleLine = true,

        visualTransformation =

            if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),

        trailingIcon = {

            IconButton(
                onClick = onVisibilityChange
            ) {

                Icon(
                    imageVector =

                        if (passwordVisible)
                            Icons.Default.Visibility
                        else
                            Icons.Default.VisibilityOff,

                    contentDescription =
                        "Password Visibility",

                    tint = Color.Gray
                )
            }
        },

        shape = registerFieldShape,

        colors = registerFieldColors(),

        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    )
}
@Composable
fun RollNumberField(
    value: String,
    onValueChange: (String) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),

        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedTextField(
            value = value,

            onValueChange = onValueChange,

            placeholder = {
                Text("Roll Number", color = Color.Gray)
            },

            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },

            singleLine = true,

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),

            shape = registerFieldShape,

            colors = registerFieldColors(),

            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "@nitkkr.ac.in",
            color = Color(0xFF616161),
            fontSize = 13.sp
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun DropdownField(

    value: String,

    placeholder: String,

    options: List<String>,

    onSelected: (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    ExposedDropdownMenuBox(

        expanded = expanded,

        onExpandedChange = {
            expanded = !expanded
        }

    ) {

        OutlinedTextField(

            value = value,

            onValueChange = {},

            readOnly = true,

            placeholder = {
                Text(placeholder, color = Color.Gray)
            },

            trailingIcon = {
                ExposedDropdownMenuDefaults
                    .TrailingIcon(expanded)
            },

            shape = registerFieldShape,

            colors = registerFieldColors(),

            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(

            expanded = expanded,

            onDismissRequest = {
                expanded = false
            }

        ) {

            options.forEach { item ->

                DropdownMenuItem(

                    text = {
                        Text(item)
                    },

                    onClick = {

                        onSelected(item)

                        expanded = false
                    }
                )
            }
        }
    }
}
@Composable
fun DobField(

    value: String,

    onDateSelected: (String) -> Unit
) {

    val context = LocalContext.current

    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)

    val month = calendar.get(Calendar.MONTH)

    val day = calendar.get(Calendar.DAY_OF_MONTH)
    /*
    Syntax:

    Box {

    OutlinedTextField(...)
    }

    means:

    “Place/render the OutlinedTextField INSIDE the Box container.”
     */
    Box(

        modifier = Modifier
            .fillMaxWidth()

            .clickable {

                DatePickerDialog(

                    context,

                    { _, selectedYear,
                      selectedMonth,
                      selectedDay ->

                        val date =

                            "$selectedDay/" +
                                    "${selectedMonth + 1}/" +
                                    "$selectedYear"

                        onDateSelected(date)
                    },

                    year,
                    month,
                    day

                ).show()
            }
    ) {

        OutlinedTextField(

            value = value,

            onValueChange = {},

            readOnly = true,

            enabled = false,

            placeholder = {
                Text("Select DOB", color = Color.Gray)
            },

            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },

            shape = registerFieldShape,

            colors = registerFieldColors(),

            modifier = Modifier
                .fillMaxWidth()
        )
    }
}
@Composable
fun FloatingMessageBanner(

    visible: Boolean,

    message: String,

    onDismiss: () -> Unit
) {

    if (!visible) return

    var progress by remember(message) {

        mutableStateOf(1f)
    }

    LaunchedEffect(message) {

        progress = 1f

        for (i in 100 downTo 0) {

            progress = i / 100f

            delay(30)
        }

        onDismiss()
    }
    /*
I am using Popup() here because normal composables still belong to the
screen layout hierarchy, whereas Popup creates a separate overlay layer
above the screen content. This makes the banner behave like a true
floating notification instead of a normal stacked UI element.
*/

    Popup(

        alignment = Alignment.TopCenter
    ) {

        Card(


            modifier = Modifier
                .width(300.dp)
                .padding(top = 70.dp),

            shape = RoundedCornerShape(20.dp),

            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),

            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp
            )
        ) {

            Column {

                Text(

                    text = message,

                    color = Color.Black,

                    modifier = Modifier
                        .padding(16.dp)
                )

                Box(

                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .background(
                            Color(0xFFFF6B35)
                        )
                )
            }
        }
    }
}