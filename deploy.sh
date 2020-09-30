#!/bin/bash

# sudo gitlab-runner run
echo "----------------------------------------"
echo "XXXXXXXX                        XXXXXXXX"
echo "Initiating group 3 deployment"
echo "Setting project to group 3 project"
echo "----------------------------------------"
echo "XXXXXXXX                        XXXXXXXX"

echo >> date + " Deployment started" >> versions.txt

git add .
git commit -m "deploying version $date"
git push -u origin master

echo "Initiating gitlab ci pipeline"

gcloud config set project mcc-fall-2019-g03

echo "Deploying End Points"
gcloud endpoints services deploy backend/api/openapi-appengine.yaml

echo "Deploying App Engine"
cd backend/api/ && gcloud app deploy --quiet

echo "Deploying Cloud functions"

cd ../cloud_functions/functions && npm install

cd .. && firebase use mcc-fall-2019-g03 && firebase deploy


