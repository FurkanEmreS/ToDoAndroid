package com.xsoftware.todoapppractice

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.xsoftware.todoapppractice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Kullanıcı oturum açmışsa doğrudan NewTaskFragment'e yönlendir
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NewTaskFragment())
                .commitNow()
        } else {
            // Kullanıcı oturum açmamışsa LoginTabFragment'i göster
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, LoginTabFragment())
                    .commitNow()
            }
        }

        // Geri tuşuna basıldığında login ekranına dönmeyi engelle
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount == 0) {
                    finishAffinity() // Uygulamayı kapatır
                } else {
                    supportFragmentManager.popBackStack() // Fragmentları yönetir
                }
            }
        })
    }
}