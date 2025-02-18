import json
import urllib.request

#URL='http://ec2-13-59-143-196.us-east-2.compute.amazonaws.com:8080'
URL = 'http://localhost:8080'
#URL = 'http://ec2-3-16-107-184.us-east-2.compute.amazonaws.com:8080'
def setPrices(prices_dict):
    form_data = {
        'adminUsername': 'trading_club_admin',
        'adminPassword': 'abcxyz',
        'prices': prices_dict
    }
    req = urllib.request.Request(URL + '/set_price', data=json.dumps(form_data).encode('utf-8'), method='POST')
    req.add_header('Content-Type', 'application/json')
    return print(urllib.request.urlopen(req).read().decode('utf-8'))
if __name__ == "__main__":
    dict = {'A': 210, 'B': 50}
    setPrices(dict)