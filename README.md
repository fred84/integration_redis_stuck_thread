Example how to get blocked redis queue listener

### How to reproduce

- start redis
- launch application
- `redis-cli lpush 'queue' 'adbasdfasdfasdfsdaf'`
- check thread `stuck_thread` state