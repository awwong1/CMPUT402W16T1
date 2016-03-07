#!/bin/bash

/usr/local/bin/enableAutoUpdate

sudo apt-get update -y
sudo apt-get upgrade -y

sudo sysctl vm.swappiness=10
echo "vm.swappiness = 10" | sudo tee -a /etc/sysctl.conf

printf "\n%s\n%s\n%s\n%s\n" "10.2.1.50 mapcore.cloud.cybera.ca" "10.2.3.9 mapnode-1.cloud.cybera.ca" "10.2.2.129 mapnode-2.cloud.cybera.ca" "10.2.1.114 mapnode-3.cloud.cybera.ca" | sudo tee -a /etc/hosts
