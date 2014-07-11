#!/bin/bash


rsync -avz --exclude-from="rsync-exclude" -e "ssh -p8822" . nodeapps@fotisp.imuresearch.eu:play-searchproxy
