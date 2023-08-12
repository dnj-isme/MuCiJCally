package edu.bluejack22_2.MuCiJCally.utility

import Utility
import android.accounts.AccountAuthenticatorResponse
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import edu.bluejack22_2.MuCiJCally.model.Account
import edu.bluejack22_2.MuCiJCally.model.AccountSession
import edu.bluejack22_2.MuCiJCally.repository.AccountRepository
import edu.bluejack22_2.MuCiJCally.viewmodel.AccountViewModel
import java.io.Serializable
import java.util.Objects

object PageNavigator {

    fun switchPage(classFrom:AppCompatActivity, classTo:Class<*>) {
        var intent = Intent(classFrom, classTo)
        intent.putExtra("Session", classFrom.intent.getSerializableExtra("Session"))
        classFrom.startActivity(intent)
    }

    fun switchPage(classFrom: AppCompatActivity, classTo: Class<*>, extras: Serializable) {
        var intent = Intent(classFrom, classTo)
        intent.putExtra("extras", extras)
        intent.putExtra("Session", classFrom.intent.getSerializableExtra("Session"))
        classFrom.startActivity(intent)
    }

    fun resetBackTo(classFrom: AppCompatActivity, classTo: Class<*>) {
        var intent = Intent(classFrom, classTo)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("Session", classFrom.intent.getSerializableExtra("Session"))
        classFrom.startActivity(intent)
    }
}