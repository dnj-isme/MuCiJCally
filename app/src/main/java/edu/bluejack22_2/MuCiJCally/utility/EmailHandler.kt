package edu.bluejack22_2.MuCiJCally.utility

import android.util.Log
import edu.bluejack22_2.MuCiJCally.utility.private.Credential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailHandler {
    val credential = Credential
    lateinit var session: Session

    private val props = Properties()
    private var ready = false

    init {
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.port"] = "587"
        initSession()
    }

    fun initSession() {
        session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(credential.email, credential.password)
            }
        })
    }

    suspend fun sendEmail(target: String, subject: String, body: String) {
        if(session == null) {
            initSession();
        }

        withContext(Dispatchers.IO) {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(credential.email))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(target))
            message.subject = subject
            message.setText(body)

            Transport.send(message)
        }
    }
//    suspend fun sendEmail(target: String, subject: String, body: String) {
//        if(ready) {
//            withContext(Dispatchers.IO) {
//                val message = MimeMessage(session)
//                message.setFrom(InternetAddress(credential.email))
//                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(target))
//                message.subject = subject
//                message.setText(body)
//
//                Transport.send(message)
//            }
//        }
//        else {
//            Log.d("DEBUG STUFF", "Send Email is not ready!")
//        }
//    }
}