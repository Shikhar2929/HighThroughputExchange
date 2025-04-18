import urllib.request
import json

URL = "http://ec2-3-16-107-184.us-east-2.compute.amazonaws.com:8080"

def create_bot(username, name, email):
    form_data = {
        'adminUsername': 'trading_club_admin',
        'adminPassword': 'abcxyz',
        'username': username,
        'name': name,
    }
    req = urllib.request.Request(URL + '/add_bot', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    return json.loads(urllib.request.urlopen(req).read().decode('utf-8'))
print(create_bot("makerbot", "makerbot", "")['apiKey'])
