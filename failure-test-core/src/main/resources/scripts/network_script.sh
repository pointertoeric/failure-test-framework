ip=`hostname -I| tr -s " " |cut -d " " -f 1`
v=`ip addr | grep $ip | tr -s " " |cut -d " " -f 8`
sudo tc qdisc del dev $v root &
sudo tc qdisc add dev $v root netem {{ operator }} {{ param }} &
sleep {{ timeInSec }}
sudo tc qdisc del dev $v root netem