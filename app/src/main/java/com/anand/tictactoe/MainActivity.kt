package com.anand.tictactoe

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var subText: TextView
    private lateinit var gameIcon: TextView
    private lateinit var loadingDots: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var navHostFragment: View

    private var dotsAnimator: ValueAnimator? = null
    private val animationHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        initializeViews()
        startWelcomeSequence()
    }

    private fun initializeViews() {
        welcomeText = findViewById(R.id.welcomeText)
        subText = findViewById(R.id.subText)
        gameIcon = findViewById(R.id.gameIcon)
        loadingDots = findViewById(R.id.loadingDots)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        navHostFragment = findViewById(R.id.nav_host_fragment)

        // Initially hide all elements
        setInitialVisibility()
    }

    private fun setInitialVisibility() {
        // Welcome screen elements - initially invisible
        welcomeText.alpha = 0f
        welcomeText.scaleX = 0.3f
        welcomeText.scaleY = 0.3f

        subText.alpha = 0f
        subText.translationY = 100f

        gameIcon.alpha = 0f
        gameIcon.scaleX = 0.1f
        gameIcon.scaleY = 0.1f
        gameIcon.rotation = -180f

        loadingDots.alpha = 0f

        // Navigation elements - initially hidden
        bottomNavigation.visibility = View.INVISIBLE
        bottomNavigation.translationY = 200f
        navHostFragment.visibility = View.INVISIBLE
        navHostFragment.alpha = 0f
    }

    private fun startWelcomeSequence() {
        // Phase 1: Show game icon with bounce (0-800ms)
        animateGameIcon()

        // Phase 2: Show welcome text with scale (600-1400ms)
        animationHandler.postDelayed({ animateWelcomeText() }, 600)

        // Phase 3: Show subtitle with slide up (1200-2000ms)
        animationHandler.postDelayed({ animateSubText() }, 1200)

        // Phase 4: Show loading animation (1800-2800ms)
        animationHandler.postDelayed({ animateLoadingDots() }, 1800)

        // Phase 5: Transition to navigation (2800ms)
        animationHandler.postDelayed({ transitionToNavigation() }, 5800)
    }

    private fun animateGameIcon() {
        val scaleUpX = ObjectAnimator.ofFloat(gameIcon, "scaleX", 0.1f, 1.2f, 1f).apply {
            duration = 800
            interpolator = BounceInterpolator()
        }

        val scaleUpY = ObjectAnimator.ofFloat(gameIcon, "scaleY", 0.1f, 1.2f, 1f).apply {
            duration = 800
            interpolator = BounceInterpolator()
        }

        val fadeIn = ObjectAnimator.ofFloat(gameIcon, "alpha", 0f, 1f).apply {
            duration = 600
        }

        val rotation = ObjectAnimator.ofFloat(gameIcon, "rotation", -180f, 0f).apply {
            duration = 800
            interpolator = OvershootInterpolator()
        }

        AnimatorSet().apply {
            playTogether(scaleUpX, scaleUpY, fadeIn, rotation)
            start()
        }
    }

    private fun animateWelcomeText() {
        val fadeIn = ObjectAnimator.ofFloat(welcomeText, "alpha", 0f, 1f).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        val scaleX = ObjectAnimator.ofFloat(welcomeText, "scaleX", 0.3f, 1.1f, 1f).apply {
            duration = 800
            interpolator = OvershootInterpolator()
        }

        val scaleY = ObjectAnimator.ofFloat(welcomeText, "scaleY", 0.3f, 1.1f, 1f).apply {
            duration = 800
            interpolator = OvershootInterpolator()
        }

        // Add subtle pulse effect
        val pulse = ObjectAnimator.ofFloat(welcomeText, "scaleX", 1f, 1.05f, 1f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            startDelay = 800
        }

        AnimatorSet().apply {
            playTogether(fadeIn, scaleX, scaleY)
            start()
        }

        pulse.start()
    }

    private fun animateSubText() {
        val fadeIn = ObjectAnimator.ofFloat(subText, "alpha", 0f, 1f).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        val slideUp = ObjectAnimator.ofFloat(subText, "translationY", 100f, -20f, 0f).apply {
            duration = 800
            interpolator = OvershootInterpolator()
        }

        AnimatorSet().apply {
            playTogether(fadeIn, slideUp)
            start()
        }
    }

    private fun animateLoadingDots() {
        val fadeIn = ObjectAnimator.ofFloat(loadingDots, "alpha", 0f, 1f).apply {
            duration = 400
        }
        fadeIn.start()

        // Animated dots
        dotsAnimator = ValueAnimator.ofInt(0, 3).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animator ->
                val dots = when (animator.animatedValue as Int) {
                    0 -> "Loading"
                    1 -> "Loading."
                    2 -> "Loading.."
                    else -> "Loading..."
                }
                loadingDots.text = dots
            }
        }
        dotsAnimator?.start()
    }

    private fun transitionToNavigation() {
        // Stop loading animation
        dotsAnimator?.cancel()

        // Phase 1: Fade out welcome elements (0-600ms)
        fadeOutWelcomeElements()

        // Phase 2: Slide in navigation (400-1000ms)
        animationHandler.postDelayed({ slideInNavigation() }, 400)

        // Phase 3: Setup navigation controller (1000ms)
        animationHandler.postDelayed({ setupNavigation() }, 1000)
    }

    private fun fadeOutWelcomeElements() {
        val welcomeFadeOut = ObjectAnimator.ofFloat(welcomeText, "alpha", 1f, 0f).apply {
            duration = 600
        }

        val subFadeOut = ObjectAnimator.ofFloat(subText, "alpha", 1f, 0f).apply {
            duration = 600
        }

        val iconFadeOut = ObjectAnimator.ofFloat(gameIcon, "alpha", 1f, 0f).apply {
            duration = 600
        }

        val loadingFadeOut = ObjectAnimator.ofFloat(loadingDots, "alpha", 1f, 0f).apply {
            duration = 400
        }

        // Add scale down effect for welcome text
        val scaleDown = ObjectAnimator.ofFloat(welcomeText, "scaleX", 1f, 0.8f).apply {
            duration = 600
        }

        AnimatorSet().apply {
            playTogether(welcomeFadeOut, subFadeOut, iconFadeOut, loadingFadeOut, scaleDown)
            start()
        }
    }

    private fun slideInNavigation() {
        // Make navigation elements visible
        navHostFragment.visibility = View.VISIBLE
        bottomNavigation.visibility = View.VISIBLE

        // Animate navigation container
        val navFadeIn = ObjectAnimator.ofFloat(navHostFragment, "alpha", 0f, 1f).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        val navSlideUp = ObjectAnimator.ofFloat(navHostFragment, "translationY", 100f, 0f).apply {
            duration = 800
            interpolator = OvershootInterpolator()
        }

        // Animate bottom navigation
        val bottomNavSlideUp = ObjectAnimator.ofFloat(bottomNavigation, "translationY", 200f, 0f).apply {
            duration = 800
            interpolator = OvershootInterpolator()
        }

        val bottomNavFadeIn = ObjectAnimator.ofFloat(bottomNavigation, "alpha", 0f, 1f).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        AnimatorSet().apply {
            playTogether(navFadeIn, navSlideUp, bottomNavSlideUp, bottomNavFadeIn)
            start()
        }
    }

    private fun setupNavigation() {
        try {
            val navController = findNavController(R.id.nav_host_fragment)
            bottomNavigation.setupWithNavController(navController)

            // Set default selection to Basic Mode (updated ID)
            bottomNavigation.selectedItemId = R.id.basicmode

            // Hide welcome elements completely
            hideWelcomeElements()

        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: just hide welcome elements
            hideWelcomeElements()
        }
    }

    private fun hideWelcomeElements() {
        welcomeText.visibility = View.GONE
        subText.visibility = View.GONE
        gameIcon.visibility = View.GONE
        loadingDots.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
         dotsAnimator?.cancel()
        animationHandler.removeCallbacksAndMessages(null)
    }


}