import json
import urllib.request

# URL = 'http://localhost:8080'
URL = "http://ec2-18-220-60-154.us-east-2.compute.amazonaws.com:8080"
default = 0


def create_user(username, name, email):
    form_data = {
        "adminUsername": "trading_club_admin",
        "adminPassword": "ZY3yoQL5v8MahcmcWBnG",
        "username": username,
        "name": name,
        "email": email,
    }
    req = urllib.request.Request(
        URL + "/add_user", data=json.dumps(form_data).encode("utf-8"), method="POST"
    )
    req.add_header("Content-Type", "application/json")
    return json.loads(urllib.request.urlopen(req).read().decode("utf-8"))


def create_random_users(n=1):
    global default
    user_list = []
    for i in range(n):
        default += 1
        username = f"bot{default}"
        email = f"fulltester_bot{default}@example.com"
        session_token = create_user(username, email, email)
        # print(session_token)
        user_list.append((username, session_token))
    return user_list


def teardown(username, session_token):
    form_data = {"username": username, "sessionToken": session_token}
    req = urllib.request.Request(
        URL + "/teardown", data=json.dumps(form_data).encode("utf-8"), method="POST"
    )
    req.add_header("Content-Type", "application/json")
    return json.loads(urllib.request.urlopen(req).read().decode("utf-8"))


def set_state(target_state):
    form_data = {
        "adminUsername": "trading_club_admin",
        "adminPassword": "ZY3yoQL5v8MahcmcWBnG",
        "targetState": target_state,
    }
    req = urllib.request.Request(
        URL + "/set_state", data=json.dumps(form_data).encode("utf-8"), method="POST"
    )
    req.add_header("Content-Type", "application/json")
    return json.loads(urllib.request.urlopen(req).read().decode("utf-8"))


if __name__ == "__main__":
    # print(create_random_users())
    print(set_state(2))  # Example usage of set_state
