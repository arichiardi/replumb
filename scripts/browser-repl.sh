#!/bin/bash

echo "Now you can connect to http://localhost:9090"
cd dev-resources/private/browser
python -m SimpleHTTPServer 9090
