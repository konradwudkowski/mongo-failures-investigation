# This repo documents my investigations re mongo reliability in different conditions

For now it will create a 5 nodes mongo replica set, start writing to mongo, kill the primary node and analyze if data was lost

To run the application execute

```
./main.sh
```

and chose one of the options that will follow e.g.

```
1) Default write concern   
2) Majority write concern
3) quit
Please choose an option:1 // runs simulation witha default write concern
```
