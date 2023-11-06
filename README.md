# Timelock.zone.AndroidExample
This repo contains an example of an Android App that can use the [timelock.zone](https://www.timelock.zone) service to encrypt messages to the future and decrypt them. Timelock.zone is a public service built on [drand](https://drand.love) that publishes public keys for future timeframes whose corresponding secret keys will be released at the correpsonding time.
See also [tlcs-c](https://github.com/aragonzkresearch/tlcs-c/), [tlcs-rust](https://github.com/aragonzkresearch/tlcs-rust/), [timelock.fs](https://github.com/vincenzoiovino/timelock.fs) and [TLCS Usage](https://github.com/aragonzkresearch/tlcs-c/blob/main/examples/howtoencrypt.md).

## Installation
You can download an `apk` from [here](https://github.com/vincenzoiovino/Timelock.zone.AndroidExample/blob/master/timelock.zone.apk). You may need to enable installation of apps from [untrusted sources](https://www.wikihow.com/Allow-Apps-from-Unknown-Sources-on-Android).
The App supports `Android` link schema that allow you to share `timelock.zone` ciphertexts over the socials and to open the corresponding links in the App. To make such links to work you may need to the App settings, click on `Set as defaul`  and then enable `Open supported links` and restart `Android`.

## Dependencies
Add as dependencies ``spongycastle 1.54.0.0``, ``json-simple-1.1.jar`` and [``timelock.zone.jar``](https://github.com/vincenzoiovino/TimelockJavaAPI/tree/main). 
The dependency section in `build.gradle.kts` should look like:

```bash
dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(files("libs\\timelock.zone.jar"))
    implementation(files("libs\\json-simple-1.1.jar"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.madgag.spongycastle:core:1.54.0.0")
    implementation ("com.madgag.spongycastle:prov:1.54.0.0")
    implementation("com.madgag.spongycastle:pkix:1.54.0.0")
    implementation( "com.madgag.spongycastle:pg:1.54.0.0")

}
```

## Screenshots
(The following screenshots are taken from an outdated version.)

<img src="screenshotlock1.jpg" width="30%" height="30%" />
<br>
<img src="screenshotlock2.jpg" width="30%" height="30%" />
<br>
<img src="screenshotlock3.jpg" width="30%" height="30%" />
<br>
<img src="screenshotlock4.jpg" width="30%" height="30%" />
<br>
<img src="screenshotlock5.jpg" width="30%" height="30%" />


