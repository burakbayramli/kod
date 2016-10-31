~/Downloads/android-sdk-linux_x86/tools/zipalign -v 4 ../../bin/Istanbulbus-unsigned.apk ../../bin/Istanbulbus-unsigned-aligned.apk
/usr/lib/jvm/java-6-sun/bin/jarsigner -verbose -keystore my-release-key.keystore ../../bin/Istanbulbus-unsigned-aligned.apk alias_name
mv ../../bin/Istanbulbus-unsigned-aligned.apk ../../bin/Istanbulbus.apk
