package edu.bluejack22_2.MuCiJCally.repository

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import edu.bluejack22_2.MuCiJCally.model.Account
import edu.bluejack22_2.MuCiJCally.utility.FirebaseHandler
import kotlinx.coroutines.suspendCancellableCoroutine

object AccountRepository {
    private val firebase = FirebaseHandler

    var accounts: LiveData<List<Account>>

    init {
        val output = MutableLiveData<List<Account>>()

        firebase.accountRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val accounts = mutableListOf<Account>()
                for (accountSnapshot in snapshot.children) {
                    val account = accountSnapshot.getValue(Account::class.java)
                    account?.let {
                        accounts.add(it)
                    }
                }
                output.value = accounts
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(error.message, error.details)
            }
        })
        accounts = output
    }

    fun getAccountByID(accountID: String, callback: (Account?) -> Unit) {
        firebase.accountRef.orderByChild("id").equalTo(accountID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var account: Account? = null
                    for (snapshot in dataSnapshot.children) {
                        account = snapshot.getValue(Account::class.java)
                    }
                    callback(account)
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Failed to fetch data from server!")
                }
            })
    }

//    suspend fun getUsernameByID(accountID: String): Account? = suspendCancellableCoroutine { continuation ->
//        firebase.accountRef.orderByChild("id").equalTo(accountID)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(dataSnapshot: DataSnapshot) {
//                    var account: Account? = null
//                    for (snapshot in dataSnapshot.children) {
//                        account = snapshot.getValue(Account::class.java)
//                    }
//                    continuation.resume(account, null)
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    println("Failed to fetch data from server!")
//                    continuation.resume(null, null)
//                }
//            })
//    }


    fun storeSession(id: String, preferences: SharedPreferences) {
        if (preferences == null) return
        with(preferences.edit()) {
            Log.d("Storing Session Token", id)
            putString("Session Token", id)
            apply()
        }
    }

    fun getSessionID(preferences: SharedPreferences): String? {
        var output: String? = null
        output = preferences.getString("Session Token", null)
        if (output != null) {
            Log.d("Receive Session Token", output)
        }
        else {
            Log.d("Receive Session Token", "NULL")
        }
        return output
    }

    fun generateNewID(): String {
        return firebase.accountRef.push().key!!
    }

    fun saveAccount(account: Account) {
        val id = account.id
        firebase.accountRef.child(id).setValue(account)
    }
}