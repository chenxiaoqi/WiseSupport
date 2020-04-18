nohup java -cp conf:classes:lib/* -d spring.profiles.active=production com.lazyman.homework.webcrawler.WebCrawler &
echo $!>pid