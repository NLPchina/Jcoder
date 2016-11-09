start()
{
  nohup java -jar jcoder-1.6.war -f=jcoder.conf &
  echo $! >jcoder.pid
}
stop()
{
  kill  `cat jcoder.pid`
}


case $1 in
"restart")
   stop
   start
;;
"start")
   start
;;
"stop")
   stop
;;
*) echo "only accept params start|stop|restart" ;;
esac
