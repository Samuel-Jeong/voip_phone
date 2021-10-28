#!/bin/sh

#java -jar -Dlogback.configurationFile=/Users/voip_phone/config/logback.xml /Users/voip_phone/lib/voip_phone.jar UMediaPhoneMain /Users/voip_phone/config/user_conf.ini /Users/voip_phone/contact/contact.txt
java -jar /Users/voip_phone/lib/voip_phone.jar 0 /Users/voip_phone/config/user_conf.ini /Users/voip_phone/contact/contact.txt