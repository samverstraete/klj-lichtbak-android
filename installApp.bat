::WIN BATCH SCRIPT

:: CHANGE THESE
set app_package=net.samverstraete.kljledcontroller
set MAIN_ACTIVITY=MainActivity

set ADB="C:\android\sdk\platform-tools\adb.exe"
::ADB_SH="%ADB% shell" # this script assumes using `adb root`. for `adb su` see `Caveats`

set apk_host=.\app\build\outputs\apk\debug\app-debug.apk
set apk_name=%app_package%

:: Delete previous APK
del %apk_host%

:: Compile the APK: you can adapt this for production build, flavors, etc.
call gradlew assembleDebug


%ADB% push %apk_host% /data/local/tmp/%apk_name% 
%ADB% root
%ADB% shell su -c pm install -t -r "/data/local/tmp/%apk_name%"
	

:: Stop the app
::%ADB% shell pm force-stop %app_package%

:: Re execute the app
::%ADB% shell pm start -n \"%app_package%/%app_package%.%MAIN_ACTIVITY%\" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER