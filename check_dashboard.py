import urllib.request
import urllib.error
import json

try:
    url = "http://127.0.0.1:8000/api/dashboard/?user_id=1"
    req = urllib.request.Request(url)
    response = urllib.request.urlopen(req)
    print("STATUS 200:")
    data = json.loads(response.read().decode())
    for k, v in data.items():
        if isinstance(v, list):
            print(f"{k}: [{len(v)} items]")
        else:
            print(f"{k}: {v}")
except urllib.error.HTTPError as e:
    print(f"STATUS {e.code}:")
    print(e.read().decode())
