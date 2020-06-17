nohup java -cp conf:classes:lib/* -Dspring.profiles.active=production com.lazyman.timetennis.TimeTennis &
echo $!>pid