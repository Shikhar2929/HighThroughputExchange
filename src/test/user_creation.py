import urllib.request
import json
from env_loader import load_env, getenv

load_env()
URL = getenv('HTTP_URL', 'http://localhost:8080')
ADMIN_USERNAME = getenv('ADMIN_USERNAME', 'trading_club_admin')
ADMIN_PASSWORD = getenv('ADMIN_PASSWORD', 'abcxyz')


def create_user(username: str, name: str, email: str):
    form_data = {
        "adminUsername": ADMIN_USERNAME,
        "adminPassword": ADMIN_PASSWORD,
        "username": username,
        "name": name,
        "email": email,
    }
    req = urllib.request.Request(URL + '/add_user', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    with urllib.request.urlopen(req) as resp:
        return json.loads(resp.read().decode('utf-8'))


if __name__ == "__main__":
    for i in range(100):
        result = create_user(f"team{i}", f"Team {i}", "team@team.team")
        print(result)
