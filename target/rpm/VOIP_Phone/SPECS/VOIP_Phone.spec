Name: VOIP_Phone
Version: 1.0.0
Release: 0
Summary: VOIP_Phone
License: 2021, JamesJ
Group: Service
autoprov: yes
autoreq: no
BuildRoot: /Users/jamesj/VOIP_Phone/target/rpm/VOIP_Phone/buildroot

%description
VOIP_Phone Module

%files
%defattr(644,voip_phone,voip_phone,755)
%dir  /home/voip_phone/
%dir  /home/voip_phone/voip_phone/
%dir %attr(755,voip_phone,voip_phone) /home/voip_phone/voip_phone/lib/
 /home/voip_phone/voip_phone/lib//voip_phone-jar-with-dependencies.jar
%dir %attr(755,voip_phone,voip_phone) /home/voip_phone/voip_phone/bin/
%attr(755,voip_phone,voip_phone) /home/voip_phone/voip_phone/bin//run.sh
%config(noreplace)  /home/voip_phone/voip_phone/config/
%dir %attr(755,voip_phone,voip_phone) /home/voip_phone/voip_phone/logs/
