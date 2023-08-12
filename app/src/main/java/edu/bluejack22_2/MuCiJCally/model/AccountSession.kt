package edu.bluejack22_2.MuCiJCally.model

class AccountSession: java.io.Serializable {
    var account: Account? = null
    var sessionID: String = ""
    var success: Boolean = false
    var message: String = ""
}

//package edu.bluejack22_2.MuCiJCally.model
//
//import android.os.Parcel
//import android.os.Parcelable
//
//class AccountSession() : java.io.Serializable, Parcelable {
//    var account: Account? = null
//    var sessionID: String = ""
//    var success: Boolean = false
//    var message: String = ""
//
//    constructor(parcel: Parcel) : this() {
//        sessionID = parcel.readString().toString()
//        success = parcel.readByte() != 0.toByte()
//        message = parcel.readString().toString()
//    }
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeString(sessionID)
//        parcel.writeByte(if (success) 1 else 0)
//        parcel.writeString(message)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<AccountSession> {
//        override fun createFromParcel(parcel: Parcel): AccountSession {
//            return AccountSession(parcel)
//        }
//
//        override fun newArray(size: Int): Array<AccountSession?> {
//            return arrayOfNulls(size)
//        }
//    }
//}