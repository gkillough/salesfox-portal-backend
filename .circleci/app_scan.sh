#!/usr/bin/env bash

scan --src . --type java,depscan,credscan -c -m ci

exit 0
