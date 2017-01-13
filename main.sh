#!/bin/bash
echo "Preparing environment..."

# create separate network for the replica set 
CLUSTER=my-mongo-cluster
if [[ ! $(docker network ls -q -f name=$CLUSTER) = "" ]]; then
  echo "separate network for mongo already exists"
else
  echo "creating a new network $CLUSTER"
  docker network create $CLUSTER
fi

REPLICA_SET=my-mongo-set
MONGO_IMAGE=mongo:3.2

echo
echo "killing all runnig containers with 'mongo' in the name..."
docker rm -f $(docker ps -aq --filter name=mongo)

#TODO: convert these lines to a loop
#TODO: if there is error stop
echo
echo starting 5 mongo nodes
docker run -dt -p 30001:27017 --name mongo1 --net $CLUSTER $MONGO_IMAGE mongod --replSet $REPLICA_SET
docker run -dt -p 30002:27017 --name mongo2 --net $CLUSTER $MONGO_IMAGE mongod --replSet $REPLICA_SET
docker run -dt -p 30003:27017 --name mongo3 --net $CLUSTER $MONGO_IMAGE mongod --replSet $REPLICA_SET
docker run -dt -p 30004:27017 --name mongo4 --net $CLUSTER $MONGO_IMAGE mongod --replSet $REPLICA_SET
docker run -dt -p 30005:27017 --name mongo5 --net $CLUSTER $MONGO_IMAGE mongod --replSet $REPLICA_SET

echo
echo "waiting for mongo nodes to boot up" # TODO replace sleep with sth better
sleep 5

# setup replica set
echo
echo seting up replica set
docker exec -it mongo1 mongo --eval "config = {
    '_id' : 'my-mongo-set',
    'members' : [
    { '_id' : 1, 'host' : 'mongo1:27017' },
    { '_id' : 2, 'host' : 'mongo2:27017' },
    { '_id' : 3, 'host' : 'mongo3:27017' },                                                                             
    { '_id' : 4, 'host' : 'mongo4:27017' },
    { '_id' : 5, 'host' : 'mongo5:27017' }
    ]
  };
  rs.initiate(config);
"

echo
NETWORK_DELAY=250
echo "introducing a network delay of $NETWORK_DELAY ms for all secondary mongo nodes..."
docker run --name pumba_mongo -itd -v /var/run/docker.sock:/var/run/docker.sock gaiaadm/pumba pumba netem --interface eth0 --duration 5h delay --time $NETWORK_DELAY mongo2 mongo3 mongo4 mongo5

sleep 2
#logs from netem
docker logs pumba_mongo
echo ""

PS3="Please choose an option:"                                                            
select option in "Default write concern" "Majority write concern" "quit"
do
    case $option in
        "Default write concern")
            sbt run
            break
            ;;

        "Majority write concern")
            sbt run -Dwrite-concern=majority
            break
            ;;

        "quit")
            break;;

        *) echo "invalid option";;
     esac
done

