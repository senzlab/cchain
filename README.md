# cchain

Blockchain implementation(to store digital cheque transfers) based on apache cassandra distributed database. We implemented all consensus 
based on cassandra cluster with scala language

# Set up and run

## 1. Run cassandra 

```
docker run -d -p 9160:9160 -p 9042:9042 erangaeb/cassandra:0.2
```

## 2. Connects to cassandra(cqlsh) 

```
# command
cqlsh <docker host> 9042 --cqlversion="3.4.4"

# example
cqlsh localhost 9042 --cqlversion="3.4.4"
```

## 3. Create keyspace

```
CREATE KEYSPACE cchain WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor': 1}
```

## 4. Run zchain

Run via docker compose

