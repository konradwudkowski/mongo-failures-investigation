# This repo documents my investigations re mongo reliability in different conditions

For now it will create a 5 nodes mongo replica set, start writing to mongo, kill the primary node and analyze if data was lost

In order for this app to work you need to have `docker` (in non-sudo mode) and `sbt` installed

Tested on ubuntu 16.04

To run the application execute

```
./main.sh
```

and chose one of the options that will follow e.g.

```
1) Default write concern   
2) Majority write concern
3) quit
Please choose an option:1 // runs simulation with a default write concern
```
