#!/bin/bash

sudo apt install virtualenv
mkdir /home/$USER/virtual_env
cd /home/$USER/virtual_env/
virtualenv -p python3 truecaller_test
. truecaller_test/bin/activate
cd /home/$USER/TruecallerTestE/
python3 setup.py install
python3 api.py
