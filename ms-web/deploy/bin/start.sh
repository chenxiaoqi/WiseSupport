nohup java -cp conf:classes:lib/* -d spring.profiles.active=production com.wisesupport.WiseSupport &
echo $!>pid