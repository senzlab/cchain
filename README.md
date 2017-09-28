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

## 4. Create tables

```
CREATE TYPE IF NOT EXISTS cchain.cheque (
  bank_id TEXT,
  id UUID,
  amount INT,
  img TEXT
);

CREATE TYPE IF NOT EXISTS cchain.transaction (
  bank_id TEXT,
  id UUID,
  cheque_bank_id UUID,
  cheque_id TEXT,
  cheque_amount INT,
  cheque_img TEXT,
  from_acc TEXT,
  to_acc TEXT,
  timestamp BIGINT,
  digsig TEXT
);

CREATE TYPE IF NOT EXISTS cchain.signature (
  bank_id TEXT,
  digsig TEXT
);

CREATE TABLE IF NOT EXISTS cchain.cheques (
  bank_id TEXT,
  id UUID,
  amount INT,
  img TEXT,

  PRIMARY KEY(bank_id, id)
);

CREATE TABLE IF NOT EXISTS cchain.transactions (
  bank_id TEXT,
  id UUID,
  cheque_bank_id UUID,
  cheque_id TEXT,
  cheque_amount INT,
  cheque_img TEXT,
  from_acc TEXT,
  to_acc TEXT,
  timestamp BIGINT,
  digsig TEXT,

  PRIMARY KEY(bank_id, id)
);

CREATE TABLE IF NOT EXISTS cchain.blocks (
  bank_id TEXT,
  id UUID,
  transactions SET<frozen <transaction>>,
  timestamp BIGINT,
  merkle_root TEXT,
  pre_hash TEXT,
  hash TEXT,
  signatures SET<frozen <signature>>,

  PRIMARY KEY(bank_id, id)
);

CREATE TABLE IF NOT EXISTS cchain.keys (
  bank_id TEXT,
  id UUID,
  transactions SET<frozen <transaction>>,
  signatures SET<frozen <signature>>,
  timestamp BIGINT,

  PRIMARY KEY(bank_id, id)
);

CREATE CUSTOM INDEX IF NOT EXISTS transactions_index ON cchain.transactions ()
USING 'com.stratio.cassandra.lucene.Index'
WITH OPTIONS = {
  'refresh_seconds': '1',
  'schema': '{
    fields: {
      bank_id: {type: "string"},
      id: {type: "uuid"},
      from_acc: {type: "string"},
      to_acc: {type: "string"},
      cheque_id: {type: "string"}
    }
  }'
};
```

## 5. Run zchain

Run via docker compose

