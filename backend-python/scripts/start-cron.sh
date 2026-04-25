#!/bin/sh
set -eu

printenv > /etc/environment
touch /var/log/aneel_perdas.log
cron -f
