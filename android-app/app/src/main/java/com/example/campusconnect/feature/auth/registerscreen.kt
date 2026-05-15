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
import java.util.Calendar
/*
below thing is called a modifier chain-
Modifier
.background()
.padding()
.clickable()*/
@Composable
fun RegisterScreen(

    onRegisterSuccess: () -> Unit,

    onNavigateToLogin: () -> Unit,
    //This composable needs a RegisterViewModel type object and if nobody passes one then, create one manually from android's ViewModel system
    viewModel: RegisterViewModel = viewModel()
) {

    val username by viewModel.username.collectAsState()
    val rollNumber by viewModel.rollNumber.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()

    val realName by viewModel.realName.collectAsState()
    val course by viewModel.course.collectAsState()
    val year by viewModel.year.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val dob by viewModel.dob.collectAsState()

    val warning by viewModel.warning.collectAsState()
    val registerSuccess by viewModel.registerSuccess.collectAsState()
    var passwordVisible by remember {
        mutableStateOf(false)
    }

    var confirmPasswordVisible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(registerSuccess) {

        if (registerSuccess) {
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFD6EBFF)
            )
    ) {

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

            Spacer(modifier = Modifier.height(14.dp))
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

            Spacer(modifier = Modifier.height(12.dp))

            if (warning.isNotEmpty()) {
               /* Because:

                val warning by viewModel.warning.collectAsState()

                made warning a Compose-observed state.

                So when warning changes:

                _warning.value = "Invalid password"

                Compose automatically recomposes the composable.*/

                Text(
                    text = warning,
                    color = Color(0xFFFFE6E6),
                    fontSize = 14.sp
                )
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