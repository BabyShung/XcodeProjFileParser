# XcodeProjFileParser
Parse xcodeproj file to get duplicated file names in your targets, etc

This is a beta version since it needs to be cleaned up and to be fixed for some other features.
Right now it can only search duplicated file names in each target.


##Xcode Project Tool INSTRUCTION:

###STEPS:

Copy your “[Project].xcodeproj” into this folder(XcodeProjFileParser).

- If you import this folder as a project then you can just run.

- If you want to use command line to execute:
1.Open terminal and go to this folder.

2.type this command (the below example is used most often):

java -classpath . Test

4.Filtered result shown in the console (usually this is enough information).
Also there is a “result” directory generated with detailed results.

