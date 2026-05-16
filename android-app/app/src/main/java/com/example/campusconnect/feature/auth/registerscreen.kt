package com.example.campusconnect.feature.auth


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import android.app.DatePickerDialog
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.delay
import java.util.Calendar
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
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
@Composable
fun RegisterScreen(

    onRegisterSuccess: () -> Unit,

    onNavigateToLogin: () -> Unit,
    //This composable needs a RegisterViewModel type object and if nobody passes one then, create one manually from android's ViewModel system
    viewModel: RegisterViewModel = viewModel()
) {

    val username by viewModel.username.collectAsState()

    val rollNumber by viewModel.rollNumber.collectAsState()
    val emailVerified by
    viewModel.emailVerified
        .collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()

    val realName by viewModel.realName.collectAsState()
    val course by viewModel.course.collectAsState()
    val year by viewModel.year.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val dob by viewModel.dob.collectAsState()


    val registerSuccess by viewModel.registerSuccess.collectAsState()
    var passwordVisible by remember {
        mutableStateOf(false)
    }

    var confirmPasswordVisible by remember {
        mutableStateOf(false)
    }
    //OTP dialog box
    var showOtpDialog by remember {

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
    var otperror by remember {

        mutableStateOf("")
    }

    var otpExpired by remember {

        mutableStateOf(false)
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
            .fillMaxSize()
            .background(
                Color(0xFFD6EBFF)
            )
    ) {
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
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Register",
                fontSize = 28.sp,
                color = Color(0xFF0D47A1)
            )

            Spacer(modifier = Modifier.height(30.dp))
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

                    color = Color(0xFF2E7D32),

                    fontSize = 12.sp,

                    modifier = Modifier
                        .graphicsLayer {

                            translationX = shakeOffset.value
                        }
                        .clickable {
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

                            viewModel.sendOtp()

                            showOtpDialog = true
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
            Spacer(modifier=Modifier.height(10.dp))
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

            Spacer(modifier = Modifier.height(24.dp))

            RegisterTextField(
                value = realName,
                placeholder = "Real Name",
                onValueChange = viewModel::onRealNameChange
            )

            Spacer(modifier = Modifier.height(14.dp))

            RegisterTextField(
                value = course,
                placeholder = "Course",
                onValueChange = viewModel::onCourseChange
            )

            Spacer(modifier = Modifier.height(14.dp))

            DropdownField(
                value = year,
                placeholder = "Select Year",

                options = listOf(
                    "1st",
                    "2nd",
                    "3rd",
                    "4th"
                ),

                onSelected = viewModel::onYearChange
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
                    .height(50.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BCD4)
                )
            ) {

                Text(
                    text = "Register",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Already have an account? Login",
                color = Color(0xFF9E9E9E),
                /*
                we can make any text clickable by using modifier
                 */
                modifier = Modifier.clickable {
                    onNavigateToLogin()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))}

            Spacer(modifier = Modifier.height(40.dp))
        }
        if (showOtpDialog) {

            OtpVerificationDialog(

                onDismiss = {

                    showOtpDialog = false
                },

                onVerify = { otp ->

                    viewModel.verifyOtp(otp)


                }
            )
            if (otperror.isNotEmpty()) {

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(

                    text = otperror,

                    color = Color.Red,

                    fontSize = 13.sp
                )
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

        shape = RoundedCornerShape(12.dp),

        colors = OutlinedTextFieldDefaults.colors(

            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,

            focusedBorderColor = Color.Blue,
            unfocusedBorderColor = Color.Transparent,

            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),

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
                        "Password Visibility"
                )
            }
        },

        shape = RoundedCornerShape(12.dp),

        colors = OutlinedTextFieldDefaults.colors(

            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,

            focusedBorderColor = Color.Blue,
            unfocusedBorderColor = Color.Transparent,

            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),

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
                Text("Roll Number")
            },

            singleLine = true,

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),

            shape = RoundedCornerShape(12.dp),

            colors = OutlinedTextFieldDefaults.colors(

                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,

                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = Color.Transparent,

                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),

            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "@nitkkr.ac.in",
            color = Color(0xFF616161)
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
                Text(placeholder)
            },

            trailingIcon = {
                ExposedDropdownMenuDefaults
                    .TrailingIcon(expanded)
            },

            shape = RoundedCornerShape(12.dp),

            colors = OutlinedTextFieldDefaults.colors(

                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,

                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = Color.Transparent
            ),

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

    val context = androidx.compose.ui.platform
        .LocalContext.current

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
                Text("Select DOB")
            },

            shape = RoundedCornerShape(12.dp),

            colors = OutlinedTextFieldDefaults.colors(

                disabledContainerColor = Color.White,

                disabledBorderColor = Color.Transparent,

                disabledTextColor = Color.Black
            ),

            modifier = Modifier
                .fillMaxWidth()
        )
    }
}
@Composable
fun OtpVerificationDialog(

    onDismiss: () -> Unit,

    onVerify: (String) -> VerifyOtpResult
) {

    var otp by remember {

        mutableStateOf("")
    }

    var otpError by remember {

        mutableStateOf("")
    }

    var otpExpired by remember {

        mutableStateOf(false)
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {

            Text("Verify Email")
        },

        text = {

            Column {

                Text(
                    "Enter OTP sent to your email"
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(

                    value = otp,

                    onValueChange = {
                        otp = it
                    },

                    placeholder = {
                        Text("Enter OTP")
                    },

                    singleLine = true
                )

                if (otpError.isNotEmpty()) {

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(

                        text = otpError,

                        color = Color.Red,

                        fontSize = 13.sp
                    )
                }
            }
        },

        confirmButton = {

            Button(

                enabled = !otpExpired,

                onClick = {

                    val result = onVerify(otp)

                    when (result) {

                        is VerifyOtpResult.Success -> {

                            onDismiss()
                        }

                        is VerifyOtpResult.Failure -> {

                            otpError =

                                "Invalid OTP. " +
                                        "${result.attemptsLeft} attempts left"
                        }

                        is VerifyOtpResult.Expired -> {

                            otpExpired = true

                            otpError =
                                "OTP expired. Request new OTP"
                        }
                    }
                }

            ) {

                Text("Verify")
            }
        },

        dismissButton = {

            TextButton(

                onClick = onDismiss
            ) {

                Text("Cancel")
            }
        }
    )
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
                            Color(0xFF2E7D32)
                        )
                )
            }
        }
    }
}