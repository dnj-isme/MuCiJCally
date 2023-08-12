package edu.bluejack22_2.MuCiJCally

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import edu.bluejack22_2.MuCiJCally.model.AccountSession
import edu.bluejack22_2.MuCiJCally.others.Constants
import edu.bluejack22_2.MuCiJCally.others.Type
import edu.bluejack22_2.MuCiJCally.utility.FileHandler
import edu.bluejack22_2.MuCiJCally.utility.LayoutAssembler
import edu.bluejack22_2.MuCiJCally.utility.PageNavigator
import edu.bluejack22_2.MuCiJCally.viewmodel.AccountViewModel

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    private lateinit var currentAccountSession: AccountSession
    private lateinit var usernameBeforeChanged: String
    private lateinit var accountViewModel: AccountViewModel

    private var profilePic: Uri? = null

    private lateinit var profileImg: ImageView

    private lateinit var usernameTxt: EditText
    private lateinit var emailTxt: EditText

    private lateinit var oldPassTxt: EditText
    private lateinit var newPassTxt: EditText
    private lateinit var confPassTxt: EditText

    private lateinit var changePicture: Button
    private lateinit var updateProfile: Button
    private lateinit var savePassword: Button

    private lateinit var logoutBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        LayoutAssembler(this, R.id.header_profile).initHeader()
        LayoutAssembler(this, R.id.footer_profile).initFooter()
        LayoutAssembler(this, R.id.lpc_profile_activity).initLastPlayedComponent()

        accountViewModel = ViewModelProvider(this)[AccountViewModel::class.java]

        profileImg = findViewById(R.id.img_profile_pic)
        usernameTxt = findViewById(R.id.txt_profile_username)
        emailTxt = findViewById(R.id.txt_profile_email)

        oldPassTxt = findViewById(R.id.txt_profile_password_old)
        newPassTxt = findViewById(R.id.txt_profile_password_new)
        confPassTxt = findViewById(R.id.txt_profile_password_confirm)

        changePicture = findViewById(R.id.btn_profile_change_pic)
        updateProfile = findViewById(R.id.btn_profile_update)
        savePassword = findViewById(R.id.btn_profile_change_password)

        logoutBtn = findViewById(R.id.logout_btn)

        currentAccountSession = intent.getSerializableExtra("Session") as AccountSession

        usernameBeforeChanged = currentAccountSession.account?.username ?: "NULL"
        usernameTxt.setText(usernameBeforeChanged)
        emailTxt.setText(currentAccountSession.account?.email ?: "NULL")

        if(currentAccountSession.account?.profilePicture?.isNotEmpty() == true) {
            Glide.with(this)
                .load(currentAccountSession.account!!.profilePicture)
                .into(profileImg)
        }

        handleEvents()
    }

    private fun handleEvents() {
        val handler = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                updateProfile.isEnabled = true
            }
        }
        usernameTxt.addTextChangedListener(handler)
        emailTxt.addTextChangedListener(handler)

        changePicture.setOnClickListener {
            FileHandler.openFileSystemForImageFile(this, Type.PROFILE_PIC)
        }

        updateProfile.setOnClickListener {
            accountViewModel.saveAccount(
                this,
                currentAccountSession.account!!,
                usernameTxt.text.toString(),
                emailTxt.text.toString(),
                profilePic
            )
        }

        savePassword.setOnClickListener {
            accountViewModel.changePassword(
                this,
                currentAccountSession.account!!,
                oldPassTxt.text.toString(),
                newPassTxt.text.toString(),
                confPassTxt.text.toString()
            )
        }

        logoutBtn.setOnClickListener {
            accountViewModel.logoutAccount(this, currentAccountSession.account!!)
            intent.removeExtra("Session")
            PageNavigator.resetBackTo(this, LoginActivity::class.java)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.REQUEST_CODE_IMAGE_CHOOSER_PROFILE_PIC) {
            profilePic = data?.data
            updateProfile.isEnabled = true
            profileImg.setImageURI(data?.data)
        }
    }
}