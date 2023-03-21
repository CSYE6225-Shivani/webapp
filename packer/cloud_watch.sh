#!/bin/bash

########Installing CloudWatch###########
sudo yum install amazon-cloudwatch-agent -y

########Transferring Jar File & changing permissions to read and execute#########
echo "Transferring Jar File & changing permissions to read and execute"
sudo mv /tmp/amazon-cloudwatch-agent.json /usr/bin/
sudo chmod 544 /usr/bin/amazon-cloudwatch-agent.json


######Configuring AWS CloudWatch#########
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -s -c file:/usr/bin/amazon-cloudwatch-agent.json
