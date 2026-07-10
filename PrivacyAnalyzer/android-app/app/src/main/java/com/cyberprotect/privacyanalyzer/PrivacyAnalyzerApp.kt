package com.cyberprotect.privacyanalyzer

import android.app.Application

/** Application entry point. No DI framework — the app is small enough that a
 *  single ViewModel + StateFlow is simpler and easier to maintain than Hilt. */
class PrivacyAnalyzerApp : Application()
