package edu.bluejack22_2.MuCiJCally

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import edu.bluejack22_2.MuCiJCally.others.Constants
import edu.bluejack22_2.MuCiJCally.others.Type
import edu.bluejack22_2.MuCiJCally.utility.FileHandler
import edu.bluejack22_2.MuCiJCally.utility.LayoutAssembler
import edu.bluejack22_2.MuCiJCally.utility.PageNavigator
import edu.bluejack22_2.MuCiJCally.utility.Validator
import edu.bluejack22_2.MuCiJCally.viewmodel.AccountViewModel
import edu.bluejack22_2.MuCiJCally.viewmodel.MusicViewModel
import javax.inject.Inject

@AndroidEntryPoint
class UploadMusicActivity : AppCompatActivity() {

    private lateinit var musicTitleEditText: EditText
    private lateinit var coverImageButton: Button
    private lateinit var musicFileButton: Button
    private lateinit var uploadMusicButton: Button
    private lateinit var backButton: Button
    private var selectedSong: Uri? = null
    private var selectedCover: Uri? = null
    private lateinit var accountViewModel:AccountViewModel
    private var currentUserID:String? = null

    private lateinit var musicViewModel:MusicViewModel

    private fun init() {
        accountViewModel = ViewModelProvider(this)[AccountViewModel::class.java]
        musicViewModel = ViewModelProvider(this)[MusicViewModel::class.java]
        val session = accountViewModel.getAccountSession(this)
        if (session != null) {
            currentUserID = session.account!!.username
        }
        musicTitleEditText = findViewById(R.id.music_title_edtext)
        coverImageButton = findViewById(R.id.upload_image_btn_music)
        musicFileButton = findViewById(R.id.upload_file_btn_music)
        uploadMusicButton = findViewById(R.id.btn_upload_music)
        backButton = findViewById(R.id.btn_back_create_playlist)
        setAction()
    }

    private fun setAction() {
        coverImageButton.setOnClickListener { FileHandler.openFileSystemForImageFile(this,Type.MUSIC) }
        musicFileButton.setOnClickListener { FileHandler.openFileSystemForMusicFile(this) }
        uploadMusicButton.setOnClickListener {
            val musicTitle:String = musicTitleEditText.text.toString()
            if (!Validator.validateMusicForm(musicTitle, selectedSong, selectedCover)) {
                Toast.makeText(this, R.string.empty_field, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            musicViewModel.uploadMusic(musicTitle, selectedSong!!, selectedCover!!, currentUserID!!)
            clear()
            PageNavigator.switchPage(this, HomeActivity::class.java)
            Toast.makeText(this, R.string.music_uploaded, Toast.LENGTH_LONG).show()
        }
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun clear() {
        musicTitleEditText.text.clear()
        selectedSong = null
        selectedCover = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_music)
        LayoutAssembler(this, R.id.footer_home).initFooter()
        LayoutAssembler(this, R.id.cMusic_top_logo).initHeader()
        LayoutAssembler(this, R.id.lpc_upload_music).initLastPlayedComponent()
        init()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.REQUEST_CODE_IMAGE_CHOOSER_MUSIC && resultCode == Activity.RESULT_OK) {
            selectedCover = data?.data
            Toast.makeText(this, getString(R.string.upload_success) , Toast.LENGTH_LONG).show()
        }

        if (requestCode == Constants.REQUEST_CODE_MUSIC_CHOOSER_MUSIC && resultCode == Activity.RESULT_OK) {
            selectedSong = data?.data
            Toast.makeText(this, getString(R.string.upload_success) , Toast.LENGTH_LONG).show()
        }
    }
}