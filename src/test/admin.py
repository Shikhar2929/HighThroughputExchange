import json
import urllib.request
import random
import time

#URL='http://ec2-13-59-143-196.us-east-2.compute.amazonaws.com:8080'
URL = 'http://localhost:8080'
#URL = 'http://ec2-3-16-107-184.us-east-2.compute.amazonaws.com:8080'
default = 0
def create_user(username, name, email):
    form_data = {
        'adminUsername': 'trading_club_admin',
        'adminPassword': 'abcxyz',
        'username': username,
        'name': name,
        'email': email
    }
    req = urllib.request.Request(URL + '/add_user', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    return json.loads(urllib.request.urlopen(req).read().decode('utf-8'))


def create_random_users(n = 1):
   global default
   l = []
   for i in range(n):
        default += 1
        username = f"bot{default}"
        name = f"Full Tester Trading Bot {default}"
        email = f"fulltester_bot{default}@example.com"
        session_token = create_user(username, email, email)
        #print(session_token)
        l.append((username, session_token))
   return l
def teardown(username, session_token):
    form_data = {
        'username': username,
        'sessionToken': session_token
    }
    req = urllib.request.Request(URL + '/teardown', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    return json.loads(urllib.request.urlopen(req).read().decode('utf-8'))
def set_state(target_state):
    form_data = {
        'adminUsername': 'trading_club_admin',
        'adminPassword': 'abcxyz',
        'targetState': target_state
    }
    req = urllib.request.Request(URL + '/set_state', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    return json.loads(urllib.request.urlopen(req).read().decode('utf-8'))

if __name__ == "__main__":
    #print(create_random_users())
    print(set_state(1))  # Example usage of set_state
