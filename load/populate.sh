#!/bin/bash
mv x00 x0
mv x01 x1
mv x02 x2
mv x03 x3
mv x04 x4
mv x05 x5
mv x06 x6
mv x07 x7
mv x08 x8
mv x09 x9
for ((i = 0; i <= 24; i++)) do
nohup java -Xms250M -Xmx250M -Djdk.xml.entityExpansionLimit=0 -jar AuthorQueuePopulator.jar  /home/ec2-user/x$i a 0 > /home/ec2-user/x$i.out &
done