import json
import urllib.request
import time
import csv

# Define the base URL
from env_loader import load_env, getenv

load_env()
URL = getenv("HTTP_URL", "http://localhost:8080")


def post_leaderboard():
    form_data = {
        "adminUsername": getenv("ADMIN_USERNAME"),
        "adminPassword": getenv("ADMIN_PASSWORD"),
    }

    # Create the request
    req = urllib.request.Request(
        URL + "/leaderboard", data=json.dumps(form_data).encode("utf-8"), method="POST"
    )
    req.add_header("Content-Type", "application/json")

    # Send the request and print the response
    try:
        response = urllib.request.urlopen(req)
        leaderboard_data = response.read().decode("utf-8")
        return leaderboard_data
    except urllib.error.HTTPError as e:
        print(f"HTTP Error: {e.code} - {e.reason}")
    except urllib.error.URLError as e:
        print(f"URL Error: {e.reason}")


def update_leaderboard_file():
    leaderboard_data = post_leaderboard()
    data = json.loads(leaderboard_data)
    if leaderboard_data:
        with open("leaderboard.json", "w") as file:
            file.write(leaderboard_data)
        with open("leaderboard.csv", "w", newline="") as csvfile:
            fieldnames = ["username", "balance"]
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            writer.writeheader()
            for entry in data.get("data", []):
                writer.writerow(entry)
        print("Leaderboard.csv saved")


def print_leaderboard():
    leaderboard_data = post_leaderboard()
    if leaderboard_data:
        print(leaderboard_data)


if __name__ == "__main__":
    while True:
        update_leaderboard_file()
        time.sleep(60)  # Wait 60 seconds before the next request
