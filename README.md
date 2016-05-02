# Duplicate File Finder
This Java command line utility prints out files of the same byte contents. It performs an MD5 checksum on the files to compare contents and lists the duplicates on std out.
## Build

Make sure you have Java 8 and Maven installed.
```sh
$ mvn compile assembly:single
```
## Run
```sh
$ cd target
$ java -jar duplicate-finder.jar <directory>
```


