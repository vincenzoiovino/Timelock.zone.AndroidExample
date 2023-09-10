# Timelock.zone.AndroidExample
This repo contains an example of an Android App that may use the timelock.zone service to encrypt messages to the future and decrypt them.
See also [tlcs-c](https://github.com/aragonzkresearch/tlcs-c/) and [TLCS Usage]((https://github.com/aragonzkresearch/tlcs-c/blob/main/examples/howtoencrypt.md)).

The app is fully working except that the ''Timelock.java'' has to be modified to retrieve real TLCS keys from the timelock.zone service. Currently, the class uses embedded keys.
