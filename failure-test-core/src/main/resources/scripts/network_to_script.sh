ip=`hostname -I| tr -s " " |cut -d " " -f 1`
v=`ip addr | grep $ip | tr -s " " |cut -d " " -f 8`
sudo tc qdisc del dev $v root &
sleep 1
sudo tc qdisc add dev $v root handle 1: prio &
sleep 1
sudo tc qdisc add dev $v parent 1:3 handle 30: netem {{ operator }} {{ param }} &
sleep 1
{% for ip in ipList %}
sudo tc filter add dev $v protocol ip parent 1:0 prio 3 u32 match ip dst {{ ip }} flowid 1:3 &
{% endfor %}
sleep {{ timeInSec }}
sudo tc qdisc del dev $v root