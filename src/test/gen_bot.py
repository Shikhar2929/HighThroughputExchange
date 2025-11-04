import urllib.request
import json
from env_loader import load_env, getenv

load_env()
URL = getenv('HTTP_URL', 'http://localhost:8080')

def create_bot(username, name, email):
    form_data = {
    'adminUsername': getenv('ADMIN_USERNAME'),
        'adminPassword': getenv('ADMIN_PASSWORD'),
        'username': username,
        'name': name,
    }
    req = urllib.request.Request(URL + '/add_bot', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    return json.loads(urllib.request.urlopen(req).read().decode('utf-8'))
print(create_bot("makerbot", "makerbot", "")['apiKey'])
