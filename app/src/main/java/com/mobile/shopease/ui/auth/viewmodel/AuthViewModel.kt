package com.mobile.shopease.ui.auth.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.shopease.data.repository.AuthRepository
import kotlinx.coroutines.launch

// Represents the status of our UI
sealed class AuthState {// Represents the status of our UI. Represents all possible authentication states.

    object Idle : AuthState()// Default state: nothing happening yet

    object Loading : AuthState()//Authentication request is running.

    object Success : AuthState()//Authentication completed successfully.

    data class Error(val message: String) : AuthState()//Represents authentication failure => error message
}

class AuthViewModel : ViewModel() {//Creates authentication ViewModel. This class talks to repository and updates UI state

    private val repository = AuthRepository()

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    //Creates mutable authentication state. With initial state AuthState.Idle

    val authState: State<AuthState> = _authState
    //public read-only state: Exposes state safely to UI.
    //The UI can read and observe state changes, but it cannot modify the state directly.

    fun signUp(email: String, pass: String) {//Handles user registration.

        viewModelScope.launch {
            //Starts asynchronous coroutine. Needed because network requests take time

            _authState.value = AuthState.Loading
            //Show loading spinner

            try {//Attempts authentication safely.

                repository.signUp(email, pass)
                //Calls Supabase signup function.

                _authState.value = AuthState.Success
                //If signup succeeds: UI updates to success state

            } catch (e: Exception) {

                _authState.value = AuthState.Error(
                    e.localizedMessage ?: "Unknown Error"
                )
                // "?:" use actual error message if available,
                // otherwise use "Unknown Error"
            }
        }
    }

    fun signIn(email: String, pass: String) {//Handles user login.

        viewModelScope.launch {
            //Starts asynchronous coroutine for login request

            _authState.value = AuthState.Loading
            //Show loading spinner

            try {

                repository.signIn(email, pass)
                //Calls Supabase login function

                _authState.value = AuthState.Success
                //If login succeeds: update UI to success state

            } catch (e: Exception) {

                _authState.value = AuthState.Error(
                    e.localizedMessage ?: "Login Failed"
                )
                //Display login error message
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.signInWithGoogle()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.localizedMessage ?: "Google sign-in failed"
                )
            }
        }
    }

    fun signInWithFacebook() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.signInWithFacebook()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.localizedMessage ?: "Facebook sign-in failed"
                )
            }
        }
    }
}
