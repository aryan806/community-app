package com.communityheroai.ui.screens // FIXED: 19

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.communityheroai.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    val context = LocalContext.current // FIXED: 2
    
    val googleSignInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> // FIXED: 1
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { viewModel.signInWithGoogle(it) }
            } catch (e: ApiException) {
                // Ignore or log
            }
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) onLoginSuccess()
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("Community Hero AI", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Report civic issues. Make a difference.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(48.dp))

            if (!isOtpSent) {
                OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") }, leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, focusedLabelColor = MaterialTheme.colorScheme.primary))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.sendPhoneOtp(phoneNumber, context as Activity) { isOtpSent = true } }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), enabled = phoneNumber.isNotBlank() && !isLoading) { // FIXED: 2
                    if (isLoading) { CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) } else { Text("Send OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                }
            } else {
                OutlinedTextField(value = otpCode, onValueChange = { otpCode = it }, label = { Text("6-digit OTP") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, focusedLabelColor = MaterialTheme.colorScheme.primary))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.verifyOtp(otpCode) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), enabled = otpCode.length == 6 && !isLoading) {
                    if (isLoading) { CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) } else { Text("Verify & Login", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
                Text(" OR ", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
            }
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { 
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id)) // FIXED: 1
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                }, 
                modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp)
            ) { // FIXED: 1
                Text("Continue with Google", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }

            AnimatedVisibility(visible = error != null) {
                Text(text = error ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}
