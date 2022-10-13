package com.codealive.socialmediaintegration

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.codealive.socialmediaintegration.databinding.ActivityProfileGoogleBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.squareup.picasso.Picasso

class ProfileGoogleActivity : AppCompatActivity() {
    private var binding:ActivityProfileGoogleBinding?=null
    private lateinit var gso:GoogleSignInOptions
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var userName:String
    private lateinit var emailId:String
    private lateinit var photo:String
    private var EmailName="userName"
    private var Email="emailId"
    private var PhotoUrl="photo"
    private var SHARED="sharedpreference"
    private var GMAIL_LOGIN="gmail_login"
    private var GMAIL_SAVE="gmail_save"
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityProfileGoogleBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val settings_save=getSharedPreferences(GMAIL_LOGIN,0)

        if(settings_save.getString("gmail_save","").toString() == "gmail_saved"){
            sharedPreferences=getSharedPreferences(SHARED, MODE_PRIVATE)
            binding?.userName?.text  =(sharedPreferences.getString(EmailName,""))
            binding?.name?.text=sharedPreferences.getString(EmailName,"")
            binding?.email?.text =(sharedPreferences.getString(Email,""))
            Picasso.get().load(sharedPreferences.getString(PhotoUrl,"")).placeholder(R.drawable.userr).into(binding?.profileImg)
        }


        gso= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleApiClient=GoogleApiClient.Builder(this)
            .enableAutoManage(this){}
            .addApi(Auth.GOOGLE_SIGN_IN_API,gso).build()

        binding?.btngoogle?.setOnClickListener {
            showAlertDialog()
        }

        binding?.email?.isSelected=true

        val account: GoogleSignInAccount? =GoogleSignIn.getLastSignedInAccount(this)

        if(account!=null){
            setSupportActionBar(binding?.googleProfileToolbar)
            userName= account.displayName.toString()
            emailId=account.email.toString()
            photo = if(account.photoUrl!=null){
                account.photoUrl.toString()
            }else{
                R.drawable.userr.toString()
            }
        }

        binding?.userName?.text=userName
        binding?.email?.text=emailId
        binding?.name?.text=userName
        sendEmail()
        Picasso.get().load(photo).placeholder(R.drawable.userr).into(binding?.profileImg)
    }

    private fun showAlertDialog(){
        val builder=AlertDialog.Builder(this)
        builder.setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("LOG OUT"){ _,_->
                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback {
                    if (it.isSuccess){
                        val settings_save:SharedPreferences=getSharedPreferences(GMAIL_LOGIN,0)
                        val editor_save:SharedPreferences.Editor=settings_save.edit()
                        editor_save.remove("gmail_saved")
                        editor_save.clear()
                        editor_save.apply()

                        val settings:SharedPreferences=getSharedPreferences(GMAIL_LOGIN,0)
                        val editor:SharedPreferences.Editor=settings.edit()
                        editor.remove("gmail_logged")
                        editor.clear()
                        editor.apply()

                        val editor1:SharedPreferences.Editor=sharedPreferences.edit()
                        editor1.clear()
                        editor1.apply()

                        startActivity(Intent(this@ProfileGoogleActivity, MainActivity::class.java))
                        finish()
                    }else{
                        Toast.makeText(this@ProfileGoogleActivity,"failed!!!",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel"){ dialog,which->
                dialog.dismiss()
            }
        val alertDialog:AlertDialog=builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun sendEmail(){
        val settings:SharedPreferences=getSharedPreferences(GMAIL_SAVE,0)
        val editor_save:SharedPreferences.Editor=settings.edit()
        editor_save.putString("gmail_saved","gmail_saved")
        editor_save.apply()

        sharedPreferences = getSharedPreferences(SHARED, MODE_PRIVATE)
        val editor:SharedPreferences.Editor = sharedPreferences.edit()

        editor.putString(EmailName,userName)
        editor.putString(Email,emailId)
        editor.putString(PhotoUrl,photo)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }
    }