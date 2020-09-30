# MCC Group 3 fall 2019 Project

#### The repository contains the source code for the project. 

## Dependencies

- Google Cloud Sdk
- Firebase cli sdk

### Deployment 

In order to run the deployment, you need to have admin access to the google cloud
project `mcc-fall-2019-g03`.

```shell script
bash deploy.sh
```

## Gitlab CI
The directory `android/.gitlab-ci.yml` contains the gitlab ci 
configuration to build the API. After successful push go to the 
gitlab pipeline and download the artifact (.apk)

This will deploy the whole 
backend infrastructure 
`Google Cloud App Engine`, `Google Cloud End Points`
, `Firebase database`, `Firebase cloud functions`.

## Cloud End Points
In backend/api directory `openapi-appengine.yaml` file contains
the openapi specification for cloud end points.

## Google App Engine

In backend/api directory `app.yaml` file contains the configuration to 
deploy app engine with the backend `python - flask ` based API. Along with that
there is a `Dockerfile` contains the custom flex app engine 
docker image required to build this project. 

## Firebase cloud functions

In backend/cf_notifications directory cloud function contains the 
image resizer, schedule_notifications, notifications functions.


##
```shell script

runtime: custom
  env: flex

  resources:
    cpu: 1
    memory_gb: 0.5
    disk_size_gb: 10
  endpoints_api_service:
    ##  # The following values are to be replaced by information from the output of
    ##  # 'gcloud endpoints services deploy openapi-appengine.yaml' command.
    name: mcc-fall-2019-g03.appspot.com
    rollout_strategy: managed
```



## Android App Build

Gitlab CI is configured with the project. After the successful
commit you can download the latest artifact *.apk file from the
github pipelines. 




