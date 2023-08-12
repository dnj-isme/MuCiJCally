package edu.bluejack22_2.MuCiJCally

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import edu.bluejack22_2.MuCiJCally.utility.EmailHandler
import edu.bluejack22_2.MuCiJCally.utility.LayoutAssembler
import edu.bluejack22_2.MuCiJCally.viewmodel.AccountViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: AccountViewModel
    private lateinit var usernameTxt: EditText
    private lateinit var emailTxt: EditText
    private lateinit var passwordTxt: EditText
    private lateinit var confirmTxt: EditText

    private lateinit var signinBtn: Button
    private lateinit var signupBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        LayoutAssembler(this, R.id.register_top_logo).initHeader()
        EmailHandler

        usernameTxt = findViewById(R.id.txt_login_username)
        emailTxt = findViewById(R.id.txt_register_email)
        passwordTxt = findViewById(R.id.txt_register_password)
        confirmTxt = findViewById(R.id.txt_register_confirm)

        signinBtn = findViewById(R.id.btn_register_sign_in)
        signupBtn = findViewById(R.id.btn_register_sign_up)

        val email = intent.getStringExtra("email")
        val username = intent.getStringExtra("username")

        if (email != null) emailTxt.setText(email)
        if (username != null) usernameTxt.setText(username)

        viewModel = ViewModelProvider(this)[AccountViewModel::class.java]
        handleEvents()
    }

    private fun handleEvents() {
        signinBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        signupBtn.setOnClickListener {
            var username = usernameTxt.text.toString()
            var email = emailTxt.text.toString()
            var password = passwordTxt.text.toString()
            var confirm = confirmTxt.text.toString()

            viewModel.attemptSignup(username, email, password, confirm, this).observe(this) {
                if (it == null) Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_LONG).show()
                else if (it.isNotEmpty()) Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                else {
                    val code = viewModel.sendVerificationEmail(email, this)

                    var next = Intent(this, VerifyEmailActivity::class.java)
                    next.putExtra("email", email)
                    next.putExtra("username", username)
                    next.putExtra("password", Utility.hashPassword(password))
                    next.putExtra("code", code)
                    startActivity(next)
                }
            }
        }
    }
}