import json
import urllib.request

from env_loader import load_env, getenv

load_env()
URL = getenv('HTTP_URL', 'http://localhost:8080')
def set_state(target_state):
    form_data = {
    'adminUsername': getenv('ADMIN_USERNAME'),
    'adminPassword': getenv('ADMIN_PASSWORD'),
        'targetState': target_state
    }
    req = urllib.request.Request(URL + '/set_state', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    return json.loads(urllib.request.urlopen(req).read().decode('utf-8'))
def setPrices(prices_dict):
    form_data = {
    'adminUsername': getenv('ADMIN_USERNAME'),
    'adminPassword': getenv('ADMIN_PASSWORD'),
        'prices': prices_dict
    }
    req = urllib.request.Request(URL + '/set_price', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    return print(urllib.request.urlopen(req).read().decode('utf-8'))
if __name__ == "__main__":
    game_1 = {'SUMSUITS': 290}
    game_2 = {'SUMSUITSA' : 270, 'SUMSUITSB': 260}
    game_3 = {'SUMSUITSA': 210, 'SUMSUITSB': 300, 'SPREADSUITAB': 410}
    game_4 = {'SUMRANKA': 60, 'SUMRANKB': 80,  'SPREADRANKAB': 980}
    game_5 = {'POWERRED': 8}
    game_6 = {'POWERRED': 32, 'POWERBLACK': 32}
    game_7 = {'NUMPAIRS': 600}
    game_8 = {'POKER': 300}
    data_challenge= {'A': 50, 'B': 50, 'C': 50, 'D': 50, 'E': 50}
    #game_3 = {55, 82, 173}
    set_state(0)
    setPrices(data_challenge)
