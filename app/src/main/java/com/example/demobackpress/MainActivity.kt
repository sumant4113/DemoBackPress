package com.example.demobackpress

import android.graphics.Rect
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demobackpress.ui.theme.DemoBackPressTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TranslateScreen(onBackPressedDispatcher = onBackPressedDispatcher)
        }
    }
}

@Composable
fun TranslateScreen(onBackPressedDispatcher: OnBackPressedDispatcher) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    var liveTranslation by remember { mutableStateOf("") }
    val history by remember { mutableStateOf(mutableListOf<String>()) }
    var isFocused by remember { mutableStateOf(false) }
    var shouldSaveHistory by remember { mutableStateOf(false) } // Flag to control history saving

    // Detect keyboard visibility to clear focus when keyboard hides
    val view = LocalView.current
    val rootView = view.rootView
    DisposableEffect(Unit) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.height
            val keypadHeight = screenHeight - rect.bottom
            if (keypadHeight < screenHeight * 0.15 && isFocused) {
                focusManager.clearFocus()
                if (inputText.text.isNotEmpty() && !history.contains(liveTranslation)) {
                    shouldSaveHistory = true
                }
            }
        }
        rootView.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    // Handle back press to clear focus and save history
// Handle back press to clear focus, save history, and exit
    DisposableEffect(Unit) {
        val callback = object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFocused) {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    if (inputText.text.isNotEmpty() && !history.contains(liveTranslation)) {
                        history.add(liveTranslation)
                    }
                }
                // Allow the activity to finish (exit the screen)
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(callback)
        onDispose {
            callback.remove()
        }
    }

    // Save to history when shouldSaveHistory is triggered
    LaunchedEffect(shouldSaveHistory) {
        if (shouldSaveHistory && inputText.text.isNotEmpty() && !history.contains(liveTranslation)) {
            history.add(liveTranslation)
            shouldSaveHistory = false // Reset the flag after saving
        }
    }

    // Outer Box to handle taps outside without ripple effect
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = {
                    if (isFocused) {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        if (inputText.text.isNotEmpty() && !history.contains(liveTranslation)) {
                            shouldSaveHistory = true
                        }
                    }
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .background(Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input field
// Replace OutlinedTextField with BasicTextField
// BasicTextField with IME action "Done"
            BasicTextField(
                value = inputText,
                onValueChange = { newText ->
                    inputText = newText
                    liveTranslation = newText.text.uppercase() // Simulated live translation
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        if (!focusState.isFocused && shouldSaveHistory) {
                            // Ensure history is saved only once when focus is lost
                            shouldSaveHistory = false // Reset flag after processing
                        }
                    },
                textStyle = TextStyle(fontSize = 16.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (inputText.text.isNotEmpty() && !history.contains(liveTranslation)) {
                            history.add(liveTranslation)
                        }
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        if (inputText.text.isEmpty()) {
                            Text(
                                text = "Enter text to translate",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // Live Translation Display
            Text(
                text = "Live Translation: $liveTranslation",
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            // History Display
            Text(
                text = "Translation History:",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            history.forEach { translatedText ->
                Text(
                    text = "- $translatedText",
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun TranslateScreen1(onBackPressedDispatcher: OnBackPressedDispatcher) {
    val focusManager = LocalFocusManager.current
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    var liveTranslation by remember { mutableStateOf("") }
    val history by remember { mutableStateOf(mutableListOf<String>()) }
    var isFocused by remember { mutableStateOf(false) }

    // Handle back press
    DisposableEffect(Unit) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFocused) {
                    focusManager.clearFocus()
                }
            }
        }
        onBackPressedDispatcher.addCallback(callback)
        onDispose {
            callback.remove()
        }
    }

    // Box to handle outside clicks and lose focus
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = {
                    if (isFocused) {
                        focusManager.clearFocus()
                    }
                }
            )
            .background(Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // EditText for input
            OutlinedTextField(
                value = inputText,
                onValueChange = { newText ->
                    inputText = newText
                    liveTranslation = newText.text.uppercase() // Simulated live translation
                },
                label = { Text("Enter text to translate") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        if (!focusState.isFocused && inputText.text.isNotEmpty()) {
                            // Save to history only when focus is lost and text exists
                            if (history.none { it == liveTranslation }) {
                                history.add(liveTranslation)
                            }
                        }
                    },
                shape = RoundedCornerShape(8.dp)
            )

            // Display live translation
            Text(
                text = "Live Translation: $liveTranslation",
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Display history
            Text(
                text = "Translation History:",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            history.forEach { translatedText ->
                Text(
                    text = "- $translatedText",
                    fontSize = 16.sp
                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun TranslateScreenPreview() {
//    // For preview, we can't pass onBackPressedDispatcher, so use a dummy implementation
//    TranslateScreen(FakeBackPressedDispatcher())
//}

// Dummy class for preview
//class FakeBackPressedDispatcher : androidx.activity.OnBackPressedDispatcher() {
//    override fun addCallback(callback: OnBackPressedCallback) {}
//}

//@Preview(showBackground = true)
//@Composable
//fun TranslateScreenPreview() {
//    TranslateScreen(FakeBackPressedDispatcher())
//}