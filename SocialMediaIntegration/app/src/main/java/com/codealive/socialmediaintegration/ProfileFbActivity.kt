package com.codealive.socialmediaintegration

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.codealive.socialmediaintegration.databinding.ActivityProfileFbBinding
import com.facebook.AccessToken
import com.facebook.AccessTokenTracker
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.squareup.picasso.Picasso

class ProfileFbActivity : AppCompatActivity() {

    private var binding:ActivityProfileFbBinding?=null
    private lateinit var sharedPreferences:SharedPreferences
    private var SHARED_PREFER="sharedpreference"
    private var FB_LOGIN="fb_login"
    private var savedUserName="name"
    private var savedFirstName="first_name"
    private var savedLastName="last_name"
    private var savedEmail="email"
    private var savedPhotoUrl="photoUrl"
    private lateinit var userName:String
    private lateinit var firstName:String
    private lateinit var lastName:String
    private lateinit var userEmail:String
    private lateinit var userPhoto:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityProfileFbBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.facebookProfileToolbar)
        supportActionBar?.title="Facebook Profile"

        FacebookSdk.sdkInitialize(applicationContext)

        sharedPreferences=getSharedPreferences(SHARED_PREFER, MODE_PRIVATE)
        userName= sharedPreferences.getString(savedUserName,"").toString()
        firstName=sharedPreferences.getString(savedFirstName,"").toString()
        lastName=sharedPreferences.getString(savedLastName,"").toString()
        userEmail=sharedPreferences.getString(savedEmail,"").toString()
        userPhoto=sharedPreferences.getString(savedPhotoUrl,"").toString()

        Picasso.get().load(userPhoto).placeholder(R.drawable.userr).into(binding?.profileImgFb)

        binding?.userNameFb?.text=userName
        binding?.nameFb?.text=firstName
        binding?.emailfb?.text=userEmail
        binding?.lastNameFb?.text=lastName

        binding?.fbLogout?.setOnClickListener {
                showAlertDialog()
        }
        binding?.emailfb?.isSelected = true
    }

    private fun showAlertDialog(){
        val builder= AlertDialog.Builder(this)
        builder.setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("LOG OUT"){ dialog,which->

                val settings:SharedPreferences = getSharedPreferences(FB_LOGIN, 0)
                val editor:SharedPreferences.Editor = settings.edit()
                editor.remove("fb_logged")
                editor.clear()
                editor.apply()

                val editor1 : SharedPreferences.Editor= sharedPreferences.edit()
                editor1.clear()
                editor1.apply()

                LoginManager.getInstance().logOut()

                val intent = Intent(this@ProfileFbActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel"){ dialog,which->
                dialog.dismiss()
            }
        val alertDialog: AlertDialog =builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private var accessTokenTracker: AccessTokenTracker = object : AccessTokenTracker() {
        override fun onCurrentAccessTokenChanged(
            oldAccessToken: AccessToken?,
            currentAccessToken: AccessToken?
        ) {
            if (currentAccessToken == null) {
               binding?.nameFb?.text=""
                binding?.emailfb?.text=""
                binding?.lastNameFb?.text=""
                binding?.userNameFb?.text=""
                binding?.profileImgFb?.setImageResource(0)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        accessTokenTracker.stopTracking()
    }
}