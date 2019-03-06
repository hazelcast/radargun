#!/bin/sh
###### This script is designed to be called from other scripts, to set environment variables including the bind
###### for cache products, as well as any JVM options.

### Set your bind address for the tests to use. Could be an IP, host name or a reference to an environment variable.

## Helper to grab the IP off a given interface.  In this case, eth0.  Replace eth0 with eth1, eth0:2, etc as desired
## and assign this to BIND_ADDRESS.

# ETH0_IP=`/sbin/ifconfig eth0 | grep inet | sed -e 's/^\s*//' -e 's/Bcast.*//' -e 's/inet addr://'`

BIND_ADDRESS=${MY_BIND_ADDRESS}
JPROFILER_HOME=${HOME}/jprofiler6
JPROFILER_CFG_ID=103

# Example slave definitions
w1_SLAVE_ADDRESS=10.0.0.235
w1_BIND_ADDRESS=10.0.0.235

w2_SLAVE_ADDRESS=10.0.0.135
w2_BIND_ADDRESS=10.0.0.135

w3_SLAVE_ADDRESS=10.0.0.49
w3_BIND_ADDRESS=10.0.0.49

w4_SLAVE_ADDRESS=10.0.0.117
w4_BIND_ADDRESS=10.0.0.117

w5_SLAVE_ADDRESS=10.0.0.166
w5_BIND_ADDRESS=10.0.0.166

w6_SLAVE_ADDRESS=10.0.0.81
w6_BIND_ADDRESS=10.0.0.81
