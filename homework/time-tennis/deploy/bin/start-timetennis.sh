nohup java -cp conf:classes:lib/* -Dspring.profiles.active=timetennis,timetennis-production com.lazyman.timetennis.TimeTennis &
echo $!>pid