# SHATTERWAKE — privacy information (0.1.0-alpha)

## Current technical behavior

SHATTERWAKE works offline and does not request the INTERNET permission. The application does not contain advertisements, analytics/telemetry SDKs, account registration, cloud sync, leaderboards, purchases or tracking.

Locally on the device, Android SharedPreferences stores: best score, permanently unlocked technologies, preferred control mode, sound setting, vibration setting. These values are not sent anywhere by the app. During gameplay the app processes touch coordinates to steer the ship and attack; it does not permanently log those touches. The application's `allowBackup` flag is disabled for this prototype.

A vibration permission is requested only to supply optional haptic hit feedback. Sound effects are synthesised locally; there are no microphone or recording permissions. No personal profile, email or precise device location is collected.

## Before commercial publication

The distributor must provide their real legal entity, suitable contact details, final privacy notice in the languages/regions of distribution and required store disclosures. Recheck the actual packaged application and network behavior, store developer-account services, crash reporting, payment services, support channels and law applicable in sales territories. A build or store listing may process customer information **outside** this game; those data practices are not represented by this prototype's notice.
