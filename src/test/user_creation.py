import requests

URI = "http://ec2-13-59-143-196.us-east-2.compute.amazonaws.com:8080"

for i in range(100):
    requests.post(
        f"{URI}/add_user",
        json={
            "adminUsername": "trading_club_admin",
            "adminPassword": "abcxyz",
            "username": f"team{i}",
            "name": f"Team {i}",
            "email": "team@team.team",
        },
    )


requests.post(
    f"{URI}/shutdown",
    json={"adminUsername": "trading_club_admin", "adminPassword": "abcxyz"},
)
