package com.codealive.socialmediaintegration

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codealive.socialmediaintegration.databinding.ActivityMainBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding?=null
    private val RC_SIGN_IN: Int = 1
    private val EMAIL="email"
    private lateinit var gso:GoogleSignInOptions
    private lateinit var name:String
    private lateinit var email:String
    private lateinit var firstName:String
    private lateinit var lastName:String
    private lateinit var url:String
    private lateinit var googleApiClient:GoogleApiClient
    private lateinit var callbackManager: CallbackManager
    private var loginSuccess:String="Login success!!"
    private var loginFailure:String="Login failure!!"
    private var loginCancelled:String="Login cancelled!!"
    private var SHARED_PREFER:String="sharedpreference"
    private var FB_LOGIN="fb_login"
    private var GMAIL_LOGIN="gmail_login"

    @SuppressLint("PackageManagerGetSignatures")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val fbSettings:SharedPreferences=getSharedPreferences(FB_LOGIN,0)
        val gmailSettings:SharedPreferences=getSharedPreferences(GMAIL_LOGIN,0)

        if(fbSettings.getString("fb_logged","").toString() == "facebook"){
            startActivity(Intent(this@MainActivity,ProfileFbActivity::class.java))
            finish()
        }
        else if(gmailSettings.getString("gmail_logged","").toString() == "google"){
            startActivity(Intent(this@MainActivity,ProfileGoogleActivity::class.java))
            finish()
        }

        requestSignIn()

        binding?.loginWithGoogle?.setOnClickListener { signIn() }


        binding?.loginWithFacebook?.setOnClickListener {

            LoginManager.getInstance().logInWithReadPermissions(this,listOf(EMAIL))
            callbackManager=CallbackManager.Factory.create()

            LoginManager.getInstance().registerCallback(callbackManager,object : FacebookCallback<LoginResult>{
                override fun onCancel() {
                    Toast.makeText(this@MainActivity, loginCancelled, Toast.LENGTH_LONG).show()
                }
                override fun onError(error: FacebookException) {
                    Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_LONG).show()
                }
                override fun onSuccess(result: LoginResult) {
                    val parameters=Bundle()
                    parameters.putString("fields","email,name,id,picture.type(large),first_name,last_name")
                    val graphRequest=GraphRequest.newMeRequest(result.accessToken){obj,_->
                        try {
                            if (obj != null) {
                                if (obj.has("id")){
                                    name=obj.getString("name").toString()
                                    email=obj.getString("email").toString()
                                    firstName=obj.getString("first_name").toString()
                                    lastName=obj.getString("last_name").toString()
                                    url=JSONObject(obj.getString("picture")).getJSONObject("data").getString("url").toString()

                                    val settings:SharedPreferences=getSharedPreferences(FB_LOGIN,0)
                                    val editor:SharedPreferences.Editor=settings.edit()
                                    editor.putString("fb_logged","facebook")
                                    editor.apply()

                                    sendFacebookData()

                                        Toast.makeText(this@MainActivity,loginSuccess,Toast.LENGTH_SHORT).show()
                                        val intent=Intent(this@MainActivity,ProfileFbActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                        }catch (ex:JSONException){
                            ex.printStackTrace()
                        }
                    }
                    graphRequest.parameters=parameters
                    graphRequest.executeAsync()
                }
            })
        }
    }

    private fun sendFacebookData() {
        val sharedPreferences:SharedPreferences=getSharedPreferences(SHARED_PREFER, MODE_PRIVATE)
        val editor:SharedPreferences.Editor=sharedPreferences.edit()
        editor.putString("name",name)
        editor.putString("first_name",firstName)
        editor.putString("last_name",lastName)
        editor.putString("email",email)
        editor.putString("photoUrl",url)
        editor.apply()
    }

    private fun requestSignIn(){
        gso=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleApiClient=GoogleApiClient.Builder(this)
            .enableAutoManage(this) {}
            .addApi(Auth.GOOGLE_SIGN_IN_API,gso).build()
    }

    private fun signIn(){
        val signInIntent=Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent,RC_SIGN_IN)
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==RC_SIGN_IN){
            val task=GoogleSignIn.getSignedInAccountFromIntent(data)
            try
            {
                val userAccount=task.getResult(ApiException::class.java)
                googleSignInAuth(userAccount)

            }catch (e:ApiException){
                Toast.makeText(this@MainActivity,loginCancelled,Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
            callbackManager.onActivityResult(requestCode, resultCode, data)
    }


    private fun googleSignInAuth(account:GoogleSignInAccount){

        try {

            val settings: SharedPreferences = getSharedPreferences(GMAIL_LOGIN, 0)
            val editor: SharedPreferences.Editor = settings.edit()
            editor.putString("gmail_logged", "google")
            editor.apply()

            Toast.makeText(this@MainActivity,loginSuccess,Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@MainActivity,ProfileGoogleActivity::class.java))
            finish()

        }catch(e:ApiException){
            Toast.makeText(this@MainActivity,loginFailure,Toast.LENGTH_SHORT).show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }

}