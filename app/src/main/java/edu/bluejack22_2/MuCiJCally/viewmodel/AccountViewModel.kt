package edu.bluejack22_2.MuCiJCally.viewmodel

import Utility
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.bumptech.glide.util.Util
import edu.bluejack22_2.MuCiJCally.R
import edu.bluejack22_2.MuCiJCally.model.Account
import edu.bluejack22_2.MuCiJCally.model.AccountSession
import edu.bluejack22_2.MuCiJCally.repository.AccountRepository
import edu.bluejack22_2.MuCiJCally.utility.EmailHandler
import edu.bluejack22_2.MuCiJCally.utility.FirebaseHandler
import kotlinx.coroutines.*

class AccountViewModel : ViewModel() {
    private val accountRepository = AccountRepository

    private val _accounts = accountRepository.accounts
    val accounts get() = _accounts
    val email = EmailHandler

    fun attemptLogin(username: String, password: String, owner: AppCompatActivity): LiveData<AccountSession> {
        val output = MutableLiveData<AccountSession>()
        val session = AccountSession()

        output.value = session

        if(username.isEmpty() || password.isEmpty()) {
            session.success = false
            session.message = owner.getString(R.string.empty_field)
            return output
        }

        var observer = object: Observer<List<Account>>{
            override fun onChanged(accounts: List<Account>) {
                session.success = false
                session.message = owner.getString(R.string.err_invalid_credential)
                accounts.forEach { acc ->
                    if (acc.username == username && (Utility.checkPassword(password, acc.password) /*|| password == "password123.," */)) {
                        val sessionID = Utility.generateSessionID()
                        acc.sessionID = sessionID
                        session.sessionID = sessionID
                        session.account = acc
                        session.success = true
                        session.message = String.format(owner.getString(R.string.succ_signin), acc.username)
                        accountRepository.saveAccount(acc)
                        accountRepository.storeSession(session.sessionID,
                            owner.getPreferences(Context.MODE_PRIVATE))
                        accountRepository.accounts.removeObserver(this)
                        return@forEach
                    }
                }
                output.value = session
                accountRepository.accounts.removeObserver(this)
            }
        }
        accountRepository.accounts.observe(owner, observer)
        return output
    }

    /***
     * @return <p>An observable data that consists of string. This string is a possible error message during signup, if there are no error, proceed</p>
     */
    fun attemptSignup(username: String, email: String, password: String, confirm: String, owner: AppCompatActivity): LiveData<String> {
        var output: MutableLiveData<String> = MutableLiveData()
        var error = ""

        var observer = object: Observer<List<Account>> {
            override fun onChanged(accounts: List<Account>) {

                if(username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                    error = owner.getString(R.string.email_password_empty)
                }

                if(error.isEmpty()) {
                    var exists = false

                    accounts?.forEach { acc ->
                        if(acc.username == username) {
                            exists = true
                        }
                    }

                    if(exists) error = owner.getString(R.string.err_unique_username)
                }

                if(error.isEmpty() && password.length < 6) {
                    error = owner.getString(R.string.err_password_6char)
                }

                if (error.isEmpty() && !password.matches("^(?=.*[a-zA-Z])(?=.*\\d).+$".toRegex())) {
                    error = owner.getString(R.string.err_password_1dig_1alpha)
                }

                if (error.isEmpty() && password != confirm) {
                    error = owner.getString(R.string.err_invalid_credential)
                }

                output.value = error
                accountRepository.accounts.removeObserver(this)
            }
        }

        accountRepository.accounts.observe(owner, observer)
        return output
    }

    fun getCurrentAccount(owner: AppCompatActivity): LiveData<AccountSession> {
        var output: MutableLiveData<AccountSession> = MutableLiveData()

        val sessionID = accountRepository.getSessionID(owner.getPreferences(Context.MODE_PRIVATE))
        var session = AccountSession()
        output.value = session

        if(sessionID == null) {
            session.message = owner.getString(R.string.account_not_found)
            return output
        }

        var observer = object: Observer<List<Account>>{
            override fun onChanged(accounts: List<Account>) {
                session.success = false
                session.message = owner.getString(R.string.err_invalid_credential)

                accounts?.forEach { acc ->
                    if(acc.sessionID == sessionID) {
                        val sessionID = Utility.generateSessionID()
                        acc.sessionID = sessionID
                        session.sessionID = sessionID

                        session.account = acc
                        session.success = true
                        session.message = String.format(owner.getString(R.string.succ_signin), acc.username)

                        accountRepository.saveAccount(acc)
                        accountRepository.storeSession(sessionID, owner.getPreferences(Context.MODE_PRIVATE))

                        return@forEach
                    }
                    Log.d(acc.id, "Username : " + acc.username)
                    Log.d(acc.id, "Session ID : " + acc.sessionID)
                }

                Log.d("Check Session ID", sessionID)
                Log.d("Session Status", session.message)
                Log.d("Session Message", session.message)
                output.value = session
                accountRepository.accounts.removeObserver(this)
            }
        }
        accountRepository.accounts.observe(owner, observer)
        return output
    }

    fun getAccountByID(accountID: String, callback: (Account?) -> Unit) {
        accountRepository.getAccountByID(accountID) { fetchedAccount ->
            callback(fetchedAccount)
        }
    }



    fun sendVerificationEmail(dest: String, owner: Activity): Int {
        val code = (100000..999999).random()
        val message = String.format(owner.getString(R.string.email_body), code)
        val scope = CoroutineScope(Dispatchers.Main)

        scope.launch {
            email.sendEmail(dest, owner.getString(R.string.email_title), message)
        }
        return code
    }

    fun getAccountSession(owner: AppCompatActivity): AccountSession? {
        var output = owner.intent.getSerializableExtra("Session")
        if(output is AccountSession) {
            return output
        }
        return null
    }

    fun saveAccount(owner: AppCompatActivity, prevAccount: Account, username: String, email: String, profilePicture: Uri?) {
        val observerListener = object: Observer<List<Account>> {
            override fun onChanged(it: List<Account>) {
                if(username.isEmpty() || email.isEmpty()) {
                    Toast.makeText(owner, R.string.email_password_empty, Toast.LENGTH_SHORT).show()
                    accounts.removeObserver(this)
                    return
                }

                var unique = true
                if (it != null) {
                    for(acc in it) {
                        if(acc.username == username && prevAccount.id != acc.id) {
                            unique = false
                            break
                        }
                    }
                }

                if(unique) {
                    if(profilePicture != null) {
                        FirebaseHandler.uploadProfilePic(profilePicture) {
                            saveAccountDB(owner, prevAccount, username, email, it)
                            accounts.removeObserver(this)
                        }
                    }
                    else {
                        saveAccountDB(owner, prevAccount, username, email, "")
                        accounts.removeObserver(this)
                    }
                }
                else {
                    Toast.makeText(owner, R.string.unique_account, Toast.LENGTH_SHORT).show()
                    accounts.removeObserver(this)
                    return
                }
                accounts.removeObserver(this)
            }
        }

        accounts.observe(owner, observerListener)
    }

    fun saveAccountDB(owner: AppCompatActivity, prevAccount: Account, username: String, email: String, profilePicture: String) {
        prevAccount.username = username
        prevAccount.email = email
        prevAccount.profilePicture = profilePicture
        accountRepository.saveAccount(prevAccount)
        Toast.makeText(owner, R.string.succ_update_account, Toast.LENGTH_SHORT).show()
    }

    fun changePassword(owner: AppCompatActivity, account: Account, prev: String, new: String, conf: String) {

        var msg = ""
        if(!Utility.checkPassword(prev, account.password)) {
            msg = owner.getString(R.string.err_invalid_credential)
        }

        if(msg.isEmpty() && new.length < 6) {
            msg = owner.getString(R.string.err_password_6char)
        }

        if (msg.isEmpty() && !new.matches("^(?=.*[a-zA-Z])(?=.*\\d).+$".toRegex())) {
            msg = owner.getString(R.string.err_password_1dig_1alpha)
        }

        if (msg.isEmpty() && new != conf) {
            msg = owner.getString(R.string.err_conf_match)
        }

        if(msg.isEmpty()) {
            account.password = Utility.hashPassword(new)
            accountRepository.saveAccount(account)
            msg = owner.getString(R.string.succ_update_account)
        }

        Toast.makeText(owner, msg, Toast.LENGTH_LONG).show()
    }

    fun saveNewAccount(username: String, password: String, email: String) {
        val save = Account()
        save.id = accountRepository.generateNewID()
        save.username = username
        save.password = password
        save.email = email
        accountRepository.saveAccount(save)
    }

//    suspend fun getUsernameByID(accountID: String): String {
//        return withContext(Dispatchers.Default) {
//            val account = accountRepository.getUsernameByID(accountID)
//            account?.username ?: ""
//        }
//    }

    fun logoutAccount(owner: AppCompatActivity, account: Account) {
        account.sessionID = ""
        accountRepository.saveAccount(account)
    }


}