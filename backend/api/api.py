import logging
from datetime import datetime
from flask import Flask
from flask import jsonify
from flask import render_template
import datetime
from flask import request
import pyrebase
import uuid
from weasyprint import HTML

config = {
    'apiKey': "AIzaSyAOpVIsKy3EIQadiujcwKMvHTEjV6hQXBA",
    'authDomain': "mcc-fall-2019-g03.firebaseapp.com",
    'databaseURL': "https://mcc-fall-2019-g03.firebaseio.com",
    'projectId': "mcc-fall-2019-g03",
    'storageBucket': "mcc-fall-2019-g03.appspot.com",
    # 'messagingSenderId': "587873286216",
    # 'appId': "1:587873286216:web:618c6488c3f19de5e5007d",
    # 'measurementId': "G-WQVPYMM1WH"
}

logging.basicConfig(filename='error.log', level=logging.ERROR)
app = Flask(__name__)
firebase = pyrebase.initialize_app(config)
auth = firebase.auth()
db = firebase.database()
storage = firebase.storage()

from urllib.parse import quote

# Temporarily replace quote function, fix storage one
# Monkey patch pyrebase: replace quote function in pyrebase to workaround a bug.
# See https://github.com/thisbejim/Pyrebase/issues/294.
pyrebase.pyrebase.quote = lambda s, safe=None: s


# Monkey patch pyrebase: the Storage.get_url method does need quoting :|
def get_url(self, token=None):
    path = self.path
    self.path = None
    if path.startswith('/'):
        path = path[1:]
    if token:
        return "{0}/o/{1}?alt=media&token={2}".format(self.storage_bucket, quote(path, safe=''), token)
    return "{0}/o/{1}?alt=media".format(self.storage_bucket, quote(path, safe=''))


pyrebase.pyrebase.Storage.get_url = lambda self, token=None: \
    get_url(self, token)

# # Temporarily replace quote function, fix storage one
# def noquote(s):
#     return s


#pyrebase.pyrebase.quote = noquote


def log_error(error):
    logging.error(error)
    return {'error': error}


@app.route('/status', methods=['GET'])
def status():
    print("Status called..")
    return jsonify({'status': 'running'})


@app.route('/user/name')
def get_display_name():
    display_name = request.args.get('name')
    if not display_name and not isinstance(display_name, str):
        error = 'Invalid User name'
        return jsonify(log_error(error)), 400

    users_by_name = db.child("users").order_by_child('displayName').equal_to(display_name).get()
    is_value = users_by_name.each()
    return jsonify(users_by_name.val() if is_value else {})


@app.route('/user/projects')
def get_projects_by_user():
    user_id = request.args.get('userId')
    sort_by = request.args.get('sortBy')

    if not user_id:
        error = 'Invalid User name'
        return jsonify(log_error(error)), 400

    #project_view_details = get_projects_by_id(user_id)
    project_view_details = get_user_projects(user_id)
    if not project_view_details:
        error = 'Project not found'
        return jsonify(log_error(error)), 400

    #project_view_details = get_user_projects(user_id)

    for project_detail in project_view_details:
        try:
            del project_detail['attachments']
            del project_detail['description']
            del project_detail['tasks']
            # if sort_by != 'starred':
            #     del project_detail['members']
        except:
            pass

    if sort_by == 'deadline':
        results = get_week_deadlines(project_view_details)
        for project_detail in project_view_details:
            members = project_detail.get('members')
            for member in members:
                if user_id == member.get('userId'):
                    project_detail.update({'starred': member.get('starred')})
                    results.append(project_detail)

        results = sorted(results, key=lambda k:datetime.datetime.strptime(k['deadline'], '%d-%m-%Y'))
    elif sort_by == 'starred':
        results = []
        for project_detail in project_view_details:
            members = project_detail.get('members')
            for member in members:
                if user_id == member.get('userId') and member.get('starred'):
                    project_detail.update({'starred': True})
                    results.append(project_detail)

            del project_detail['members']
        results = sorted(results, key=lambda k: k['name'])
    elif sort_by == 'name':
        results = []
        for project_detail in project_view_details:
            members = project_detail.get('members')
            for member in members:
                if user_id == member.get('userId'):
                    project_detail.update({'starred': member.get('starred')})
                    results.append(project_detail)

        results = sorted(project_view_details, key=lambda k: k['name'])
    else:
        results = []
        for project_detail in project_view_details:
            members = project_detail.get('members')
            for member in members:
                if user_id == member.get('userId'):
                    project_detail.update({'starred': member.get('starred')})
                    results.append(project_detail)
        results = sorted(project_view_details, key=lambda k: datetime.datetime.strptime(k['lastModified'], '%d-%m-%Y'), reverse=True)

    return jsonify(results)


# @app.route('/test')
# def test():
#     project_id = '-LuHSMEYDT4mol7p9qYJ'
#     try:
#         project_details = db.child("projects").child(project_id).get().val()
#         get_week_deadlines([project_details])
#     except Exception as e:
#         return jsonify(log_error('Unable to starred project {}'.format(project_id)))
#
#     return jsonify('ok')


@app.route('/isNameExist')
def is_name_exist():
    display_name = request.args.get('name')
    if not display_name and not isinstance(display_name, str):
        error = 'Invalid User name'
        return jsonify(log_error(error)), 400

    users_by_name = db.child("users").order_by_child('displayName').equal_to(display_name).get()
    return jsonify(True if users_by_name.each() else False)

@app.route('/project/name/get')
def get_project_details_by_name():

    project_list = []
    project_name = request.args.get('name')
    user_id = request.args.get('userId')
    if not project_name:
        error = 'Project name not provided'
        return jsonify(log_error(error)), 400

    project_details = get_user_projects(user_id)
    if not project_details:
        error = 'Project with name {} not found'.format(project_name)
        return jsonify(log_error(error)), 400

    for project in project_details:
        try:
            del project['attachments']
            del project['keywords']
            del project['description']
            del project['tasks']
            del project['members']
        except:
            pass

        if project.get('name', '').startswith(project_name):
            project_list.append(project)

    return jsonify(project_list)


@app.route('/project/keyword/get')
def get_project_details_by_keywords():

    project_list = []
    keywords = request.args.get('keywords')
    user_id = request.args.get('userId')

    if not keywords:
        error = 'keywords not provided'
        return jsonify(log_error(error)), 400

    project_details = get_user_projects(user_id)
    if project_details is None:
        error = 'Projects not found'
        return jsonify(log_error(error)), 400

    for project in project_details:
        try:
            del project['attachments']
            del project['description']
            del project['tasks']
            del project['members']
        except:
            pass

        for keyword in keywords.split(','):
            if keyword.replace(' ', '') in project.get('keywords', ''):
                del project['keywords']
                project_list.append(project)

    return jsonify(project_list)


@app.route('/project/get')
def get_project_details():
    project_id = request.args.get('projectId')
    if not project_id:
        error = 'Project ID not provided'
        return jsonify(log_error(error)), 400

    project_details = db.child("projects").child(project_id).get().val()
    if project_details is None:
        error = 'Project with id {} not found'.format(project_id)
        return jsonify(log_error(error)), 400

    return jsonify(project_details)

@app.route('/project/starred', methods=['PUT'])
def starred_project():
    project_id = request.args.get('projectId')
    user_id = request.args.get('userId')
    if not project_id:
        error = 'Project ID not provided'
        return jsonify(log_error(error)), 400

    members = db.child("projects").child(project_id).child('members').get().val()
    if not members:
        error = 'Members of project with id {} not found'.format(project_id)
        return jsonify(log_error(error)), 400

    updated = False
    for member in members:
        if user_id == member.get('userId'):
            updated = True
            if not member.get('starred'):
                is_starred = True
            else:
                is_starred = not member.get('starred', False)

            member['starred'] = is_starred

    if updated:
        try:
            db.child("projects").child(project_id).update({'members': members, 'lastModified': datetime.datetime.now().strftime('%d-%m-%Y') })
        except Exception as e:
            return jsonify(log_error('Unable to starred project {}'.format(project_id)))

    return jsonify({'starred': is_starred })

@app.route('/project/attachment', methods=['PUT'])
def set_project_attachment():
    project_id = request.args.get('projectId')
    try:
        attachment_detail = request.get_json(force=True)
    except Exception as e:
        return jsonify({'error': 'Something is wrong with the json body'}), 400

    attachment_url = attachment_detail.get('attachment_url')
    attachment_type = attachment_detail.get('attachment_type')
    attachment_name = attachment_detail.get('attachment_name', 'Some attachment')

    if not project_id:
        error = 'Project ID not provided'
        return jsonify(log_error(error)), 400

    project_details = db.child("projects").child(project_id).get().val()
    if project_details is None:
        error = 'Project with id {} not found'.format(project_id)
        return jsonify(log_error(error)), 400

    attachments = project_details.get('attachments', [])
    attachments.append({'name': attachment_name,'type':attachment_type, 'url':attachment_url, 'createdTime': datetime.datetime.now().strftime("%d-%m-%Y")})

    try:
        db.child("projects").child(project_id).update({ 'attachments': attachments, 'isMedia': True })
    except Exception as e:
        return jsonify(log_error('Unable to add attachment of project {}'.format(project_id)))

    return jsonify(project_details)


@app.route('/project/attachment', methods=['GET'])
def get_project_attachment():
    project_id = request.args.get('projectId')
    attachment_url = request.args.get('attachment_url')
    attachment_type = request.args.get('attachment_type')
    if not project_id:
        error = 'Project ID not provided'
        return jsonify(log_error(error)), 400

    project_details = db.child("projects").child(project_id).get().val()
    if project_details is None:
        error = 'Project with id {} not found'.format(project_id)
        return jsonify(log_error(error)), 400

    attachments = project_details.get('attachments', [])
    attachments.append({'type':attachment_type, 'url':attachment_url})

    try:
        db.child("projects").child(project_id).update({ 'attachments': attachments, 'isMedia': True })
    except Exception as e:
        return jsonify(log_error('Unable to add attachment of project {}'.format(project_id)))

    return jsonify(project_details)


@app.route('/project/users/profileImage')
def get_user_profile_images():
    project_id = request.args.get('projectId')
    if not project_id:
        error = 'Project ID not provided'
        return jsonify(log_error(error)), 400

    project_details = db.child("projects").child(project_id).get().val()
    if project_details is None:
        error = 'Project with id {} not found'.format(project_id)
        return jsonify(log_error(error)), 400

    members = project_details.get('members', [])
    users_images = []
    for id, user_object in members.items():
        user_detail =  db.child("users").child(user_object.get('userId')).get().val()
        if not user_detail:
            continue

        users_images.append({'userId': user_object.get('userId'), 'profilePictureUrl': user_detail.get('profilePictureUrl')})


    return jsonify({'users_images': users_images})

@app.route('/project/members/get')
def get_members_of_project():

    project_id = request.args.get('projectId')
    if not project_id:
        error = 'Project ID not provided'
        return jsonify(log_error(error)), 400

    project_details = db.child("projects").child(project_id).get().val()
    if project_details is None:
        error = 'Project with id {} not found'.format(project_id)
        return jsonify(log_error(error)), 400

    members = project_details.get('members')

    return jsonify(members)


@app.route('/project/report/')
def get_project_report():

    id = uuid.uuid4()
    project_id = request.args.get('projectId')
    if not project_id:
        error = 'Project ID not provided'
        return jsonify(log_error(error)), 400

    html_report = generate_report(project_id)
    if not html_report:
        return jsonify(log_error('Project not found')), 400

    try:
        with open("{}.html".format(id), "w") as html_file:
            html_file.write(html_report)

        HTML(filename="{}.html".format(id)).write_pdf("{}.pdf".format(id))
    except Exception as e:
        error = 'Unable to generate report'
        return jsonify(log_error(error)), 400

    storage_obj = storage.child("reports/{}.pdf".format(id)).put("{}.pdf".format(id))
    report_url = storage.child("reports/{}.pdf".format(id)).get_url(storage_obj.get('downloadTokens'))

    if not report_url:
        error = 'Report of user with id {} not found'.format(project_id)
        return jsonify(log_error(error)), 400

    return jsonify({'report_url': report_url })

@app.route('/user/id/')
def get_user_id():

    user_name = request.args.get('userName')
    if not user_name:
        error = 'User Name not provided'
        return jsonify(log_error(error)), 400

    user = db.child("users").order_by_child('displayName').equal_to(user_name).get().val()
    if not user:
        error = 'User with name {} not found'.format(user_name)
        return jsonify(log_error(error)), 400

    return jsonify({'userId': list(user.keys())[0]})


@app.route('/user/name/list')
def get_user_name_list():

    user_ids = request.args.get('userIds')
    if not user_ids:
        error = 'User Name not provided'
        return jsonify(log_error(error)), 400

    user_ids = user_ids.split(',')
    user_names = []
    for user_id in user_ids:
        user = db.child("users").child(user_id).get().val()
        if not user:
            error = 'User with id {} not found'.format(user_id)
            return jsonify(log_error(error)), 400

        user_names.append({'user_id': user_id, 'name': user.get('displayName')})


    return jsonify(user_names)


@app.route('/project/create', methods=['POST'])
def create_project():
    try:
        req_data = request.get_json(force=True)
    except:
        return jsonify({'error': 'Something is wrong with the json body'}), 400

    name = req_data.get('name', None)
    deadline = req_data.get('deadline', None)
    owner = req_data.get('owner', None)
    type = req_data.get('type', None)
    description = req_data.get('description', None)
    keywords = req_data.get('keywords', None)
    badge = req_data.get('badge', None)

    if not all([name, deadline, owner, type]):
        error = log_error("Required parameters not provided")
        return jsonify(error), 400

    error, response = add_project(deadline, owner, name, type, description, badge, keywords)

    if error:
        error = log_error("Unable to add project")
        return jsonify(error), 400

    return jsonify({'id': response['name']})


@app.route('/project/members/add', methods=['PUT'])
def add_members():
    try:
        members = request.get_json(force=True)
    except:
        return jsonify({'error': 'Something is wrong with the json body'}), 400

    project_id = request.args.get('projectId')

    if not all([project_id, members]):
        error = log_error("Required parameters not provided")
        return jsonify(error), 400

    if not add_members_to_project(project_id, members):
        error = log_error("Unable to add members")
        return jsonify(error), 400

    return jsonify({'status': 'success'})


@app.route('/project/delete', methods=['DELETE'])
def delete_project():
    project_id = request.args.get('projectId')
    user_id = request.args.get('userId')
    if not project_id or not user_id:
        return jsonify({"error": "Missing required parameters"}), 400
    else:
        if db.child("projects").child(project_id).get().val() is None:
            return jsonify({"error": "Project doesnt exist with provided id"}), 400
        else:
            owner = db.child("projects").child(project_id).child('owner').get().val()
            print(owner)
            if owner == user_id:
                db.child("projects").child(project_id).remove()
                return jsonify({'status': 'success'})
            else:
                return jsonify({'error': 'User doesnt own the project'}), 400


@app.route('/task/create', methods=['POST'])
def create_task():
    project_id = request.args.get('projectId')
    user_id = request.args.get('userId')
    if not project_id or not user_id:
        return jsonify({"error": "Missing required parameters"}), 400
    else:
        if db.child("projects").child(project_id).get().val() is None:
            return jsonify({"error": "Project doesnt exist with provided id"}), 400
        else:
            try:
                req_data = request.get_json(force=True)
                status = req_data.get('status', None)
                deadline = req_data.get('deadline', None)
                description = req_data.get('description', None)

                if not all([status, deadline, description]):
                    error = log_error("Required parameters not provided")
                    return jsonify(error), 400
                else:
                    responseData = db.child("projects").child(project_id).child("tasks").push(req_data)
                    db.child("projects").child(project_id).child("tasks").child(responseData['name']).child(
                        "users").push({
                        'userId': user_id
                    })
                    return jsonify({
                        "taskId": responseData['name']
                    })
            except Exception as e:
                print (e)
                return jsonify({'error': 'Ops something went wrong!'}), 500
            else:
                return jsonify({'error': 'User doesnt own the project'}), 400


@app.route('/task/')
def get_all_tasks():
    tasks = []
    project_id = request.args.get('projectId')
    if not project_id:
        error = 'Project ID not provided'
        return jsonify(log_error(error)), 400

    project_details = db.child("projects").child(project_id).get().val()
    if project_details is None:
        error = 'Project with id {} not found'.format(project_id)
        return jsonify(log_error(error)), 400

    for k,v in project_details.get('tasks', {}).items():
        v['id'] = k
        tasks.append(v)

    return jsonify(sorted(tasks, key=lambda k: k['deadline']))

@app.route('/project/attachments')
def get_project_media():
    attachments = []
    project_id = request.args.get('projectId')
    attachment_type = request.args.get('attachmentType')
    if not project_id:
        error = 'Project ID not provided'
        return jsonify(log_error(error)), 400

    project_details = db.child("projects").child(project_id).get().val()
    if project_details is None:
        error = 'Project with id {} not found'.format(project_id)
        return jsonify(log_error(error)), 400

    for attachment in project_details.get('attachments', []):
        if attachment_type == attachment.get('type'):
            attachments.append(attachment)

    return jsonify(sorted(attachments, key=lambda k: k['createdTime']))

@app.route('/task/status', methods=['PUT'])
def set_task_status():
    project_id = request.args.get('projectId')
    task_id = request.args.get('taskId')
    status = request.args.get('status')
    if not all([project_id, task_id, status]):
        return jsonify({"error": "Missing required parameters"}), 400

    try:
        db.child("projects").child(project_id).child("tasks").child(task_id).update({'status': status})
    except Exception:
        return jsonify({'error': 'Ops something went wrong!'}), 500

    return jsonify({'status': 'success'})



@app.route('/task/assign', methods=['PUT'])
def assign_task():
    project_id = request.args.get('projectId')
    task_id = request.args.get('taskId')
    user_id = request.args.get('userId')
    if not project_id or not task_id or not user_id:
        return jsonify({"error": "Missing required parameters"}), 400

    db.child("projects").child(project_id).update({'lastModified': datetime.utcnow().strftime('%d-%m-%Y')})
    db.child("notifications").update({user_id: 'Project'})
    db.child("projects").child(project_id).child("tasks").child(task_id).child(
        "users").push({
        'userId': user_id
    })
    return jsonify({'status': 'success'})


@app.errorhandler(404)
def http_error_handler(error):
    return render_template('404.html'), 404


def add_project(deadline, owner, name, type, description="", badge='' ,keywords={}):
    project = {
        'attachments': [
        ],
        'badge': badge,
        'creationDate': datetime.datetime.now().strftime("%d-%m-%Y"),
        'deadline': deadline,
        'description': description,
        'keywords': keywords,
        'lastModified': datetime.datetime.now().strftime("%d-%m-%Y"),
        'members': [
            {
                'userId': owner,
                'userRole': 'participant'
            }
        ],
        'name': name,
        'owner': owner,
        'type': type,
        'isMedia': False
    }

    try:
        response = db.child("projects").push(project)
    except Exception as e:
        log_error("Unable to add project due to: {}".format(e))
        return True, None

    return False, response


def add_members_to_project(project_id, members):
    try:
        project_detail = db.child("projects").child(project_id).get().val()
        if project_detail.get('type', '').lower() == 'personal':
            log_error('Can not add member in a personal project')
            return False

        if project_detail is None:
            return False
        else:
            db.child("projects").child(project_id).update({'lastModified': datetime.datetime.now().strftime('%d-%m-%Y')})
            past_members = project_detail.get('members', [])
            for item in members:

                item.update({'starred': False})
                past_members.append(item)
                db.child("notifications").update({item.get('userId', ''): 'Project'})
                # Please dont remove this line this is for creating notification instance
            db.child("projects").child(project_id).update({'members': past_members})

    except Exception as e:
        log_error("Unable to add members due to: {}".format(e))
        return False

    return True

def get_projects_by_id(user_id):
    project_details_filter = []
    if not user_id and not isinstance(user_id, str):
        error = 'Invalid User name'
        log_error(error)
        return False

    project_details = db.child("projects").get().val()

    if not project_details:
        error = 'User id {} did not create any project'.format(user_id)
        log_error(error)
        return False

    for projectId, project_detail in project_details.items():
        members = project_detail.get('members', {})

        if user_id not in [x.get('userId') for x in members]:
            continue

        project_detail.update({'id': projectId})
        project_details_filter.append(project_detail)

    return project_details_filter


def generate_report(project_id):

    html_body_start = '<!DOCTYPE html><html><head><title>Poject Report</title></head><body>'
    html_body_end = '</body></html>'
    members = []

    project_detail = db.child("projects").child(project_id).get().val()
    if not project_detail:
        return ''

    project_members = project_detail.get('members', [])
    for user_object in project_members:
        user_detail = db.child("users").child(user_object.get('userId')).get().val()
        if user_detail:
            members.append(user_detail.get('displayName'))

    result_html = ''
    project_header= '<h1>Project Report</h1><br><h2>{}</h2><br><h4>Owner: {}</h4><br><h4>Members</h4>'.format(project_detail.get('name', ''), project_detail.get('owner', ''))
    members_tag = '<ul>{}</ul>'
    member_list = []
    for member in members:
        member_list.append('<li>{}</li>'.format(member))

    member_html = members_tag.format(''.join(member_list)) + '<br><h4>Tasks</h4><br>'
    task_html = ''
    for k, v in project_detail.get('tasks', {}).items():
        task_initial = '<ul><li>{}'.format(k)
        tasks_data = '<ul><li><strong>Deadline: </strong>{}</li><li><strong>Status: </strong>{}</li><li><strong>Description: </strong>{}</li></ul></li></ul>'.format(v.get('deadline', ''), v.get('status', ''), v.get('description', ''))
        task_html = task_html + task_initial + tasks_data

    result_html = html_body_start + result_html + project_header + member_html + task_html + html_body_end
    print(result_html)
    return result_html

def get_user_projects(user_id, project_ids=[]):
    project_view_details = []
    project_details = get_projects_by_id(user_id)
    if not project_details:
        error = 'Project not found'
        log_error(error)
        return None

    for project_detail in project_details:
        if project_ids:
            if project_detail.get('id') not in project_ids:
                continue

        members = project_detail.get('members', [])
        users_images = []
        is_starred = False
        for user_object in members:
            if user_object.get('starred', False):
                is_starred = True

            user_detail = db.child("users").child(user_object.get('userId')).get().val()
            if not user_detail:
                continue

            users_images.append(
                {'userId': user_object.get('userId'), 'profilePictureUrl': user_detail.get('profilePictureUrl')})

            if len(users_images) == 3:
                break

            project_detail.update({'user_images': users_images})

        project_detail.update({'starred': is_starred})

        project_view_details.append(project_detail)

    return project_view_details


def get_week_deadlines(project_details):
    filtered_results = []
    for project_detail in project_details:
        deadline = project_detail.get('deadline')
        if not deadline:
            continue

        deadline = datetime.datetime.strptime(deadline, '%d-%m-%Y')
        diff_date = deadline - datetime.datetime.now()
        days = diff_date.days

        if days > -1 and days < 7:
            filtered_results.append(project_detail)

    return filtered_results

if __name__ == '__main__':
    app.run(debug=True, port=8080, host='0.0.0.0', threaded=True)
