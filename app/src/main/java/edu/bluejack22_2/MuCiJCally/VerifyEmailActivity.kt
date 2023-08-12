package edu.bluejack22_2.MuCiJCally

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ui.setupActionBarWithNavController
import edu.bluejack22_2.MuCiJCally.utility.PageNavigator
import edu.bluejack22_2.MuCiJCally.viewmodel.AccountViewModel
import kotlin.properties.Delegates

class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var backBtn: Button
    private lateinit var verifyButton: Button
    private lateinit var codeTxt: EditText
    private lateinit var verifyResend: TextView
    private lateinit var confirm_lbl: TextView

    private lateinit var accountViewModel: AccountViewModel

    private lateinit var email: String
    private lateinit var username: String
    private lateinit var password: String // Hashed
    private var code = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_email)

        accountViewModel = ViewModelProvider(this)[AccountViewModel::class.java]

        email = intent.getStringExtra("email")!!
        username = intent.getStringExtra("username")!!
        code = intent.getIntExtra("code", -1)
        password = intent.getStringExtra("password")!!

        verifyButton = findViewById(R.id.btnVerify)
        codeTxt = findViewById(R.id.txtVerifyCode)
        verifyResend = findViewById(R.id.verify_resend)
        confirm_lbl = findViewById(R.id.confirm_lbl)
        confirm_lbl.text = getString(R.string.lbl_verify_desc) + " " + email
        backBtn = findViewById(R.id.verify_back)

        handleEvents()
    }

    private var lastResendTime: Long = 0

    private fun handleEvents() {
        lastResendTime = System.currentTimeMillis()
        verifyResend.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - lastResendTime
            val timeLimit = 2 * 60 * 1000 // 2 minutes in milliseconds

            if (elapsedTime >= timeLimit) {
                code = accountViewModel.sendVerificationEmail(email, this)
                Toast.makeText(this, R.string.resent_code, Toast.LENGTH_LONG).show()
                lastResendTime = currentTime
            } else {
                val remainingTime = timeLimit - elapsedTime
                val remainingTimeSeconds = remainingTime / 1000
                val message = String.format(getString(R.string.please_wait), remainingTimeSeconds)
                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        backBtn.setOnClickListener {
            PageNavigator.switchPage(this, RegisterActivity::class.java)
        }


        verifyButton.setOnClickListener {
            if(code.toString() == codeTxt.text.toString()) {
                Toast.makeText(this, R.string.account_created, Toast.LENGTH_LONG).show()
                accountViewModel.saveNewAccount(username, password, email)
                PageNavigator.switchPage(this, LoginActivity::class.java)
            }
            else {
                Toast.makeText(this, R.string.code_invalid, Toast.LENGTH_LONG).show()
            }
        }
    }
}