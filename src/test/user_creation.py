import requests

for i in range(100):
    requests.post('http://localhost:8080/add_user', json={
        "adminUsername": "trading_club_admin",
        "adminPassword": "abcxyz",
        "username": f"team{i}",
        "name": f"Team {i}",
        "email": "team@team.team"
    })
