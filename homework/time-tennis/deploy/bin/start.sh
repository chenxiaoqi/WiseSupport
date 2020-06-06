nohup java -cp conf:classes:lib/* -d spring.profiles.active=production com.lazyman.timetennis.TimeTennis &
echo $!>pid