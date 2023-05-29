package ru.mirea.shumikhin.firebaseauth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import ru.mirea.shumikhin.firebaseauth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var binding: ActivityMainBinding

    // START declare_auth
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        mAuth = FirebaseAuth.getInstance()
        setContentView(binding.root)

        binding.btnSignIn.setOnClickListener {
            signIn(binding.etvEmail.text.toString(), binding.etvPassword.text.toString())
        }
        binding.verifyEmailButton.setOnClickListener {
            sendEmailVerification()
        }
        binding.btnCreateAccount.setOnClickListener {
            createAccount(binding.etvEmail.text.toString(), binding.etvPassword.text.toString())
        }
        binding.btnSignOut.setOnClickListener {
            signOut()
        }
    }

    // [START on_start_check_user]
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            binding.statusTextView.text = getString(
                R.string.emailpassword_status_fmt,
                user.email,
                user.isEmailVerified
            )
            binding.detailTextView.text = getString(R.string.firebase_status_fmt, user.uid)
            binding.emailPasswordButtons.visibility = View.GONE
            binding.emailPasswordFields.visibility = View.GONE
            binding.signedInButtons.visibility = View.VISIBLE
            binding.verifyEmailButton.isEnabled = !user.isEmailVerified
        } else {
            binding.statusTextView.setText(R.string.signed_out)
            binding.detailTextView.text = null
            binding.emailPasswordButtons.visibility = View.VISIBLE
            binding.emailPasswordFields.visibility = View.VISIBLE
            binding.signedInButtons.visibility = View.GONE
        }
    }

    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:$email")
        if (!validateForm()) {
            return
        }
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
            this
        ) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "createUserWithEmail:success")
                val user = mAuth.currentUser
                updateUI(user)
            } else {
                // If sign in fails, display a message to the user.
                Log.w(
                    TAG, "createUserWithEmail:failure",
                    task.exception
                )
                Toast.makeText(this@MainActivity, "Authentication failed.", Toast.LENGTH_SHORT)
                    .show()
                updateUI(null)
            }
        }
// [END create_user_with_email]
    }

    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:$email")
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
            this
        ) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithEmail:success")
                val user = mAuth.currentUser
                updateUI(user)
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithEmail:failure", task.exception)
                Toast.makeText(this@MainActivity, "Authenticationfailed.", Toast.LENGTH_SHORT)
                    .show()
                updateUI(null)
            }
            // [START_EXCLUDE]
            if (!task.isSuccessful) {
                binding.statusTextView.setText(R.string.auth_failed)
            }

            // [END_EXCLUDE]
        }
        // [END sign_in_with_email]
    }

    private fun signOut() {
        mAuth.signOut()
        updateUI(null)
    }

    private fun sendEmailVerification() {
// Disable button
        binding.verifyEmailButton.isEnabled = false
        // Send verification email
// [START send_email_verification]
        val user = mAuth.currentUser ?: return
        user.sendEmailVerification().addOnCompleteListener(
            this
        ) { task ->
            // [START_EXCLUDE]
            // Re-enable button
            binding.verifyEmailButton.isEnabled = true
            if (task.isSuccessful) {
                Toast.makeText(
                    this@MainActivity,
                    "Verification email sent to " + user!!.email,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Log.e(TAG, "sendEmailVerification", task.exception)
                Toast.makeText(
                    this@MainActivity,
                    "Failed to send verification email.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // [END_EXCLUDE]
        }
// [END send_email_verification]
    }

    private fun validateForm(): Boolean {
        return binding.etvEmail.text.toString() != "" && binding.etvPassword.text.toString() != ""
    }
}