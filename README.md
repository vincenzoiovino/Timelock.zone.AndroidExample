# Timelock.zone.AndroidExample
This repo contains an example of an Android App that can use the [timelock.zone](https://www.timelock.zone) service to encrypt messages to the future and decrypt them.
See also [tlcs-c](https://github.com/aragonzkresearch/tlcs-c/) and [TLCS Usage](https://github.com/aragonzkresearch/tlcs-c/blob/main/examples/howtoencrypt.md).

The app is fully working except that the [Timelock.java](https://github.com/vincenzoiovino/Timelock.zone.AndroidExample/blob/master/app/src/main/java/com/example/timelockzone/Timelock.java) class has to be modified to retrieve real TLCS keys from the timelock.zone service. The code computes all necessary information to do that but currently the class ignores them and uses embedded keys.

You can download an `apk` from [here](https://github.com/vincenzoiovino/Timelock.zone.AndroidExample/blob/master/timelock.zone.apk). You may need to enable installation of apps from [untrusted sources](https://www.wikihow.com/Allow-Apps-from-Unknown-Sources-on-Android).
## Screenshots
<img src="screenshotlock1.jpg" width="30%" height="30%" />
<br>
<img src="screenshotlock2.jpg" width="30%" height="30%" />
<br>
<img src="screenshotlock3.jpg" width="30%" height="30%" />
<br>
<img src="screenshotlock4.jpg" width="30%" height="30%" />

