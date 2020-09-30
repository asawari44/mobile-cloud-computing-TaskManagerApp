API URLS:


Create New Project:
HTTP POST
https://mcc-fall-2019-g03.appspot.com/project/create
Post Body
```json
{
	"name":"project132",
	"deadline":"23-12-2019",
	"owner":"aashir",
	"type":"abc",
	"description":"New projkect",
	"keywords":"safas,fasfasdf,fasfasf,sdfdasfas"
}
```


Add Members to project:
HTTP PUT

https://mcc-fall-2019-g03.appspot.com/project/members/add?projectId=

Body -> 

```json
[{
"userId":"dsafasdfas",
"role":"participant"
},
{
"userId":"sadfasf",
"role":"partidsafasfacipant"
}]
```


Delete a project:
HTTP DELETE

https://mcc-fall-2019-g03.appspot.com/project/delete?projectId=<project ID>&userId=<User ID>

You must own the project in order to delete the project


Create a task:

https://mcc-fall-2019-g03.appspot.com/task/create?projectId=&userId=

HTTP POST

```json
{
"description":"Description of task 2",
"deadline":"21-12-2020",
"status":"pending"
}
```

Check display Name:

https://mcc-fall-2019-g03.appspot.com/user/name?name=<insner user name here>

HTTP GET


Get Project details:

https://mcc-fall-2019-g03.appspot.com/project/get?projectId=<insert project id here>

HTTP GET



List userID with profileImages of a project:

https://mcc-fall-2019-g03.appspot.com/project/users/profileImag?projectId=<insert project id here>

HTTP GET


Get all members of a project:

https://mcc-fall-2019-g03.appspot.com/project/members/get?projectId=<insert project id here>

HTTP GET


Get all  projects of a user w.r.t to sort/filters:

https://mcc-fall-2019-g03.appspot.com/user/projects?userId=<insert user id here>&sortBy=<starred/deadline/name>

HTTP GET

```json
[
    {
        "badge": "https://firebasestorage.googleapis.com/v0/b/mcc-fall-2019-g03.appspot.com/o/badges%2FAsawari%20Joshi-LuXRNoIYQhyh5wh8a1h?alt=media&token=ab9d5b55-eb6e-445a-a955-42848f8aad50",
        "creationDate": "03-12-2019",
        "deadline": "23-12-2019",
        "id": "-LuHSMEYDT4mol7p9qYJ",
        "isMedia": true,
        "lastModified": "",
        "name": "project132",
        "owner": "9ab679a9-a94c-43fd-8131-c77964aa24",
        "starred": true,
        "type": "group",
        "user_images": [
            {
                "profilePictureUrl": "https://firebasestorage.googleapis.com/v0/b/mcc-fall-2019-g03.appspot.com/o/badges%2FAsawari%20Joshi?alt=media&token=a4fb2482-1daf-4967-8290-392d5ca4f1eb",
                "userId": "0e6d02b6-2750-4d85-992f-9923cf4e5350"
            },
            {
                "profilePictureUrl": "picurl",
                "userId": "a0ba9ad3-bc66-448f-8fe4-03f099f0308c"
            }
        ]
    },
    {
        "badge": "https://firebasestorage.googleapis.com/v0/b/mcc-fall-2019-g03.appspot.com/o/badges%2FAsawari%20Joshi-LuXRNoIYQhyh5wh8a1h?alt=media&token=ab9d5b55-eb6e-445a-a955-42848f8aad50",
        "creationDate": "01-11-2019",
        "deadline": "02-12-2019",
        "id": "-LuHSMEYDT4mol7p9qYY",
        "isMedia": true,
        "lastModified": "",
        "name": "project132",
        "owner": "9ab679a9-a94c-43fd-8131-c77964aa24",
        "starred": false,
        "type": "personal",
        "user_images": [
            {
                "profilePictureUrl": "https://firebasestorage.googleapis.com/v0/b/mcc-fall-2019-g03.appspot.com/o/badges%2FAsawari%20Joshi?alt=media&token=a4fb2482-1daf-4967-8290-392d5ca4f1eb",
                "userId": "0e6d02b6-2750-4d85-992f-9923cf4e5350"
            }
        ]
    },
    {
        "badge": "",
        "creationDate": "01-11-2019",
        "deadline": "02-12-2019",
        "id": "-LuHSMEYDT4mol7p9qYZ",
        "isMedia": true,
        "lastModified": "",
        "name": "project132",
        "owner": "9ab679a9-a94c-43fd-8131-c77964aa24",
        "starred": false,
        "type": "group",
        "user_images": [
            {
                "profilePictureUrl": "https://firebasestorage.googleapis.com/v0/b/mcc-fall-2019-g03.appspot.com/o/badges%2FAsawari%20Joshi?alt=media&token=a4fb2482-1daf-4967-8290-392d5ca4f1eb",
                "userId": "0e6d02b6-2750-4d85-992f-9923cf4e5350"
            },
            {
                "profilePictureUrl": "picurl",
                "userId": "a0ba9ad3-bc66-448f-8fe4-03f099f0308c"
            }
        ]
    }
]
```


Set  projects attachments:

https://mcc-fall-2019-g03.appspot.com/project/attachment?projectId=<insert project id here>&attachment_url=<attachment_url>&attachment_type=<pdf>

HTTP PUT


Get user ID with user name:

https://mcc-fall-2019-g03.appspot.com/user/id?userName=<user name>

HTTP GET


Get user report:

https://mcc-fall-2019-g03.appspot.com/project/report/?projectId=<projectId>

HTTP GET

Output:
```
{
    "report_url": "https://firebasestorage.googleapis.com/v0/b/mcc-fall-2019-g03.appspot.com/o/reports%2Fsample.pdf?alt=media&token=6247db49-85eb-49c4-8688-db415e8158d1"
}
```

Get all attachment with type filter:

https://mcc-fall-2019-g03.appspot.com/project/attachments/?projectId=<project id>&attachmentType=<pdf,media,audio>

HTTP GET

Get all tasks of a project:

https://mcc-fall-2019-g03.appspot.com/task/?projectId=<project id>

HTTP GET

Set tasks status of a project:

https://mcc-fall-2019-g03.appspot.com/task/status?projectId=<project Id>&taskId=<tassk Id>&status=<complete/pending/onGoing>

HTTP PUT

Get list of user names:

https://mcc-fall-2019-g03.appspot.com/user/name/list?userId&userIds=<user id comma seperated string>

HTTP GET


```json
[
    {
        "name": "Asawari",
        "user_id": "m6SbNHs3PURA15RFom0j14xGNb52"
    },
    {
        "name": "omer01",
        "user_id": "aa1dc405-40ba-4450-84d6-0bf96b315652"
    },
    {
        "name": "omer012",
        "user_id": "7f0e607c-93f4-46ee-af74-bd639fee5109"
    },
    {
        "name": "omer0123",
        "user_id": "e569eab1-61d8-4eb8-a333-f2928bc3a98a"
    }
]
```


Starred project by a user:

https://mcc-fall-2019-g03.appspot.com/project/starred?projectId=<projectid>&userId=<userId>

HTTP PUT

```json
{ "starred": true/false }
```

Search project by project name:

https://mcc-fall-2019-g03.appspot.com/project/name/get?name=<project name partial or full>&userId=<userId>

HTTP GET

```json
[
    {
        "badge": "https://firebasestorage.googleapis.com/v0/b/mcc-fall-2019-g03.appspot.com/o/badges%2FAsawari%20Joshi-LuXRNoIYQhyh5wh8a1h?alt=media&token=ab9d5b55-eb6e-445a-a955-42848f8aad50",
        "creationDate": "03-12-2019",
        "deadline": "23-12-2019",
        "id": "-LuHSMEYDT4mol7p9qYJ",
        "isMedia": true,
        "lastModified": "05-12-2019",
        "name": "cproject132",
        "owner": "9ab679a9-a94c-43fd-8131-c77964aa24",
        "type": "group",
        "user_images": [
            {
                "profilePictureUrl": "https://firebasestorage.googleapis.com/v0/b/mcc-fall-2019-g03.appspot.com/o/badges%2FAsawari%20Joshi?alt=media&token=a4fb2482-1daf-4967-8290-392d5ca4f1eb",
                "userId": "0e6d02b6-2750-4d85-992f-9923cf4e5350"
            },
            {
                "profilePictureUrl": "picurl",
                "userId": "a0ba9ad3-bc66-448f-8fe4-03f099f0308c"
            }
        ]
    }
]
```

Search project by project keywords:

https://mcc-fall-2019-g03.appspot.com/project/keyword/get?keywords=<project keywords comma seperated string>&userId=<userId>

HTTP GET

```json
[
    {
        "badge": "https://firebasestorage.googleapis.com/v0/b/mcc-fall-2019-g03.appspot.com/o/badges%2FAsawari%20Joshi-LuXRNoIYQhyh5wh8a1h?alt=media&token=ab9d5b55-eb6e-445a-a955-42848f8aad50",
        "creationDate": "03-12-2019",
        "deadline": "23-12-2019",
        "id": "-LuHSMEYDT4mol7p9qYJ",
        "isMedia": true,
        "lastModified": "05-12-2019",
        "name": "cproject132",
        "owner": "9ab679a9-a94c-43fd-8131-c77964aa24",
        "type": "group",
        "user_images": [
            {
                "profilePictureUrl": "https://firebasestorage.googleapis.com/v0/b/mcc-fall-2019-g03.appspot.com/o/badges%2FAsawari%20Joshi?alt=media&token=a4fb2482-1daf-4967-8290-392d5ca4f1eb",
                "userId": "0e6d02b6-2750-4d85-992f-9923cf4e5350"
            },
            {
                "profilePictureUrl": "picurl",
                "userId": "a0ba9ad3-bc66-448f-8fe4-03f099f0308c"
            }
        ]
    }
]
```


Check if displayName exist or not:

https://mcc-fall-2019-g03.appspot.com/isNameExist?name=<displayName>

HTTP GET

```json
True
```