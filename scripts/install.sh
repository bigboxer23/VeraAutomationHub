#!/usr/bin/env bash
host=homecontroller

scp -o StrictHostKeyChecking=no -r automation-hub.service pi@$host:~/
ssh -t pi@$host -o StrictHostKeyChecking=no "sudo mv ~/automation-hub.service /lib/systemd/system"
ssh -t pi@$host -o StrictHostKeyChecking=no "sudo systemctl daemon-reload"
ssh -t pi@$host -o StrictHostKeyChecking=no "sudo systemctl enable automation-hub.service"
ssh -t pi@$host -o StrictHostKeyChecking=no "sudo systemctl start automation-hub.service"